package com.example.appmusica.presentation.canciones

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.example.appmusica.presentation.MainActivity
import com.example.appmusica.service.PlaybackService
import android.content.ComponentName
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.appmusica.R
import com.example.appmusica.databinding.FragmentDetalleBinding
import com.example.appmusica.presentation.canciones.viewmodel.CancionesViewModel
import com.example.appmusica.util.setClickAnimation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetalleFragment : Fragment() {

    private var _binding: FragmentDetalleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CancionesViewModel by activityViewModels()
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private val player: Player? get() = mediaController
    private var isTonearmDragging = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleBinding.inflate(inflater, container, false)
        return binding.root
    }

    private var updateProgressRunnable = object : Runnable {
        override fun run() {
            updateProgress()
            binding.root.postDelayed(this, 1000)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val position = arguments?.getInt("position") ?: -1

        // Default: show full player immediately (we're always opened with STATE_EXPANDED)
        binding.fullPlayerLayout.visibility = View.VISIBLE
        binding.fullPlayerLayout.alpha = 1f
        binding.miniPlayerLayout.visibility = View.INVISIBLE
        binding.miniPlayerLayout.alpha = 0f

        if (position != -1) {
            viewModel.selectCancion(position)
        }

        var lastCancionId: Int? = null
        viewModel.selectedCancion.observe(viewLifecycleOwner) { cancion ->
            cancion?.let {
                updateUI(it)
                // Solo reiniciar el reproductor si la canción realmente cambió
                if (lastCancionId != it.id) {
                    trySetupPlayerWithCurrentList()
                }
                lastCancionId = it.id
            }
        }

        setupManualControls()
        setupMiniPlayerControls()
        setupTonearm()
    }

    fun updatePlaylistPosition(position: Int) {
        if (position == -1) return // Just expand, don't change song
        
        viewModel.selectCancion(position)
        val controller = mediaController ?: return
        val cancionList = viewModel.canciones.value ?: return
        if (position >= 0 && position < cancionList.size) {
            controller.seekTo(position, 0)
            controller.play()
        }
    }

    fun stopPlayback() {
        mediaController?.let {
            it.stop()
            it.clearMediaItems()
        }
    }

    @javax.inject.Inject
    lateinit var likedSongsManager: com.example.appmusica.data.local.LikedSongsManager

    private fun updateUI(cancion: com.example.appmusica.domain.model.Cancion) {
        // Full player
        binding.txtNombre.text = cancion.nombre
        binding.txtArtista.text = cancion.artista
        binding.txtAlbum.text = cancion.album

        // Styling for interactivity
        binding.txtArtista.paintFlags = binding.txtArtista.paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
        binding.txtAlbum.paintFlags = binding.txtAlbum.paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG

        binding.txtArtista.setOnClickListener {
            cancion.artistaId?.let { id ->
                (activity as? MainActivity)?.minimizePlayer()
                val bundle = Bundle().apply { putInt("artistId", id) }
                findNavController().navigate(R.id.albumsFragment, bundle)
            }
        }

        binding.txtAlbum.setOnClickListener {
            cancion.albumId?.let { id ->
                (activity as? MainActivity)?.minimizePlayer()
                val bundle = Bundle().apply { putInt("albumId", id) }
                findNavController().navigate(R.id.albumSongsFragment, bundle)
            }
        }

        // Like button and counts
        updateLikeIcon(cancion.id)
        binding.txtLikesCount.text = "${cancion.likes} likes"
        
        binding.btnLike.setOnClickListener {
            val isCurrentlyLiked = likedSongsManager.isLiked(cancion.id)
            val nowLiked = likedSongsManager.toggleLike(cancion.id)
            
            // Sync with backend and update global state via ViewModel
            viewModel.toggleLike(cancion, isCurrentlyLiked)
            
            if (nowLiked) {
                showLikeConfetti()
            }
            updateLikeIcon(cancion.id)
            binding.btnLike.setClickAnimation()
        }

        // Mini player
        binding.txtMiniNombre.text = cancion.nombre
        binding.txtMiniArtista.text = cancion.artista

        val portadaPath = cancion.urlPortada ?: ""
        val baseUrl = com.example.appmusica.di.NetworkModule.BASE_API_URL.removeSuffix("/")
        val fullPortadaUrl = if (portadaPath.startsWith("http")) portadaPath else baseUrl + portadaPath

        Glide.with(this)
            .load(fullPortadaUrl)
            .centerCrop()
            .circleCrop()
            .placeholder(R.drawable.portada_generica)
            .into(binding.imgCancion)

        Glide.with(this)
            .load(fullPortadaUrl)
            .centerCrop()
            .placeholder(R.drawable.portada_generica)
            .into(binding.imgMiniCancion)
    }

    private fun updateLikeIcon(cancionId: Int) {
        val isLiked = likedSongsManager.isLiked(cancionId)
        val icon = if (isLiked) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off
        binding.btnLike.setImageResource(icon)
        val color = if (isLiked) resources.getColor(R.color.spotify_green, null) else android.graphics.Color.WHITE
        binding.btnLike.setColorFilter(color)
    }

    private fun showLikeConfetti() {
        val konfettiView = (activity as? MainActivity)?.findViewById<nl.dionsegijn.konfetti.xml.KonfettiView>(R.id.konfettiView)
        konfettiView?.start(
            nl.dionsegijn.konfetti.core.Party(
                speed = 0f,
                maxSpeed = 25f,
                damping = 0.9f,
                spread = 360,
                colors = listOf(
                    android.graphics.Color.parseColor("#1DB954"),
                    android.graphics.Color.parseColor("#FFE137"),
                    android.graphics.Color.parseColor("#FF5C5C")
                ),
                shapes = listOf(nl.dionsegijn.konfetti.core.models.Shape.Circle, nl.dionsegijn.konfetti.core.models.Shape.Square),
                size = listOf(nl.dionsegijn.konfetti.core.models.Size.SMALL, nl.dionsegijn.konfetti.core.models.Size.LARGE),
                timeToLive = 2000L,
                emitter = nl.dionsegijn.konfetti.core.emitter.Emitter(duration = 150, java.util.concurrent.TimeUnit.MILLISECONDS).max(100),
                position = nl.dionsegijn.konfetti.core.Position.Relative(0.5, 0.4)
            )
        )
    }

    private fun setupManualControls() {
        binding.btnPlayPause.setOnClickListener { togglePlayPause() }
        binding.btnPlayPause.setClickAnimation()

        binding.sliderProgress.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                player?.let {
                    val duration = it.duration
                    if (duration > 0) {
                        it.seekTo((value * duration / 100).toLong())
                    }
                }
            }
        }

        binding.btnPrev.setOnClickListener { player?.seekToPrevious() }
        binding.btnPrev.setClickAnimation()
        binding.btnNext.setOnClickListener { player?.seekToNext() }
        binding.btnNext.setClickAnimation()

        binding.btnRepeat.setOnClickListener {
            player?.let {
                it.repeatMode = if (it.repeatMode == Player.REPEAT_MODE_OFF) {
                    Player.REPEAT_MODE_ONE
                } else {
                    Player.REPEAT_MODE_OFF
                }
                updateRepeatIcon()
            }
        }
        binding.btnRepeat.setClickAnimation()

        binding.btnMinimize.setOnClickListener {
            (activity as? MainActivity)?.minimizePlayer()
        }
    }

    private fun setupMiniPlayerControls() {
        binding.miniPlayerLayout.setOnClickListener {
            // Expand on click
            val activity = activity as? MainActivity
            // Using a large position just to trigger expansion without reloading
            activity?.expandPlayer(-1) 
        }

        binding.btnMiniPlayPause.setOnClickListener { togglePlayPause() }
        binding.btnMiniPrev.setOnClickListener { player?.seekToPrevious() }
        binding.btnMiniNext.setOnClickListener { player?.seekToNext() }
    }

    private fun togglePlayPause() {
        player?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
            updatePlayPauseIcon()
        }
    }

    private fun trySetupPlayerWithCurrentList() {
        val controller = mediaController ?: return
        val cancionList = viewModel.canciones.value ?: return
        val selectedCancion = viewModel.selectedCancion.value
        val initialPosition = cancionList.indexOfFirst { it.id == selectedCancion?.id }.takeIf { it >= 0 } ?: 0

        if (cancionList.isEmpty() || initialPosition >= cancionList.size) return

        val mediaItems = cancionList.map { song ->
            val audioUrl = song.urlAudio ?: ""
            val portadaPath = song.urlPortada ?: ""
            val baseUrl = com.example.appmusica.di.NetworkModule.BASE_API_URL.removeSuffix("/")
            val fullAudioUrl = if (audioUrl.startsWith("http")) audioUrl else baseUrl + audioUrl
            val fullPortadaUrl = if (portadaPath.startsWith("http")) portadaPath else baseUrl + portadaPath

            val metadata = MediaMetadata.Builder()
                .setTitle(song.nombre)
                .setArtist(song.artista)
                .setAlbumTitle(song.album)
                .setArtworkUri(android.net.Uri.parse(fullPortadaUrl))
                .build()

            androidx.media3.common.MediaItem.Builder()
                .setUri(fullAudioUrl)
                .setMediaMetadata(metadata)
                .build()
        }

        // Check if we already have this list loaded to avoid restarting
        val currentPlayingUri = controller.currentMediaItem?.localConfiguration?.uri?.toString()
        val targetUri = mediaItems[initialPosition].localConfiguration?.uri?.toString()

        if (currentPlayingUri == targetUri) {
            return
        }

        controller.setMediaItems(mediaItems, initialPosition, 0)
        controller.prepare()
        controller.playWhenReady = true
    }

    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(requireContext(), ComponentName(requireContext(), PlaybackService::class.java))
        controllerFuture = MediaController.Builder(requireContext(), sessionToken).buildAsync()
        controllerFuture?.addListener({
            val controller = controllerFuture?.get() ?: return@addListener
            mediaController = controller
            binding.playerView.player = controller
            controller.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    updatePlayPauseIcon()
                    updateVinylAnimation(isPlaying)
                    if (isPlaying) {
                        binding.root.post(updateProgressRunnable)
                    } else {
                        binding.root.removeCallbacks(updateProgressRunnable)
                    }
                }

                override fun onPlaybackStateChanged(state: Int) {
                    updatePlayPauseIcon()
                }

                override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                    super.onMediaItemTransition(mediaItem, reason)
                    mediaItem?.mediaMetadata?.let { metadata ->
                        binding.txtNombre.text = metadata.title
                        binding.txtArtista.text = metadata.artist
                        binding.txtAlbum.text = metadata.albumTitle

                        binding.txtMiniNombre.text = metadata.title
                        binding.txtMiniArtista.text = metadata.artist

                        metadata.artworkUri?.let { uri ->
                            val glideUrl = com.bumptech.glide.load.model.GlideUrl(uri.toString(), com.bumptech.glide.load.model.LazyHeaders.Builder()
                                .addHeader("ngrok-skip-browser-warning", "true")
                                .build())
                            Glide.with(this@DetalleFragment).load(glideUrl).centerCrop().circleCrop().into(binding.imgCancion)
                            Glide.with(this@DetalleFragment).load(glideUrl).centerCrop().into(binding.imgMiniCancion)
                        }

                        // Actualizar selectedCancion en el ViewModel para refrescar likes y datos
                        val canciones = viewModel.canciones.value
                        val nuevaCancion = canciones?.find { it.nombre == metadata.title && it.artista == metadata.artist }
                        nuevaCancion?.let {
                            viewModel.selectCancion(canciones.indexOf(it))
                        }
                    }
                }

                override fun onRepeatModeChanged(repeatMode: Int) {
                    updateRepeatIcon()
                }
            })
            updatePlayPauseIcon()
            updateRepeatIcon()
            updateVinylAnimation(controller.isPlaying)
            if (controller.isPlaying) {
                binding.root.post(updateProgressRunnable)
            }
            
            trySetupPlayerWithCurrentList()
        }, requireContext().mainExecutor)

        // Initial sync with BottomSheet state
        (activity as? MainActivity)?.let { activity ->
            if (activity.findViewById<View>(R.id.playerContainer) != null) {
                val state = com.google.android.material.bottomsheet.BottomSheetBehavior.from(activity.findViewById<View>(R.id.playerContainer)).state
                onBottomSheetStateChanged(state)
            }
        }
    }

    private var vinylAnimator: android.animation.ObjectAnimator? = null

    private fun updateVinylAnimation(isPlaying: Boolean) {
        if (_binding == null) return
        
        if (isPlaying) {
            if (vinylAnimator == null) {
                vinylAnimator = android.animation.ObjectAnimator.ofFloat(binding.vinylContainer, "rotation", 0f, 360f).apply {
                    duration = 15000 // Even slower rotation
                    repeatCount = android.animation.ValueAnimator.INFINITE
                    interpolator = android.view.animation.LinearInterpolator()
                }
                vinylAnimator?.start()
            } else if (vinylAnimator?.isPaused == true) {
                vinylAnimator?.resume()
            }
        } else {
            vinylAnimator?.pause()
        }
    }

    @android.annotation.SuppressLint("ClickableViewAccessibility")
    private fun setupTonearm() {
        binding.imgTonearm.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isTonearmDragging = true
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    // Calculate angle relative to the pivot
                    // Pivot is at binding.imgTonearm.x + transformPivotX, etc.
                    val parent = v.parent as View
                    val pivotX = v.left + v.pivotX
                    val pivotY = v.top + v.pivotY
                    
                    val dx = event.rawX - (parent.left + pivotX)
                    val dy = event.rawY - (parent.top + pivotY)
                    
                    var angle = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
                    
                    // Adjust angle: our asset is vertical, pivot is near bottom.
                    // -90 is pointing straight up. We want 0 to be parked (-30 in XML).
                    angle += 90f
                    
                    val clampedAngle = angle.coerceIn(-30f, 45f)
                    v.rotation = clampedAngle
                    
                    // If over vinyl (angle > 0), seek
                    if (clampedAngle > 0) {
                        player?.let { p ->
                            val progress = (clampedAngle / 45f).coerceIn(0f, 1f)
                            p.seekTo((progress * p.duration).toLong())
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    isTonearmDragging = false
                    val angle = v.rotation
                    player?.let { p ->
                        if (angle <= 0) {
                            if (p.isPlaying) p.pause()
                        } else {
                            if (!p.isPlaying) p.play()
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }

    fun onBottomSheetStateChanged(newState: Int) {
        if (_binding == null) return
        
        val isExpanded = newState == com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
        val isDragging = newState == com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_DRAGGING
        val isSettling = newState == com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_SETTLING

        if (isExpanded) {
            binding.miniPlayerLayout.alpha = 0f
            binding.fullPlayerLayout.alpha = 1f
            binding.miniPlayerLayout.visibility = View.INVISIBLE
            binding.fullPlayerLayout.visibility = View.VISIBLE
        } else if (newState == com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED) {
            binding.miniPlayerLayout.alpha = 1f
            binding.fullPlayerLayout.alpha = 0f
            binding.miniPlayerLayout.visibility = View.VISIBLE
            binding.fullPlayerLayout.visibility = View.INVISIBLE
        }
    }

    fun onBottomSheetSlide(slideOffset: Float) {
        if (_binding == null) return
        
        // slideOffset: 0 (collapsed) -> 1 (expanded)
        binding.miniPlayerLayout.alpha = (1 - slideOffset * 2).coerceIn(0f, 1f)
        binding.fullPlayerLayout.alpha = (slideOffset * 2 - 1).coerceIn(0f, 1f)
        
        if (slideOffset > 0.5f) {
            binding.miniPlayerLayout.visibility = View.INVISIBLE
            binding.fullPlayerLayout.visibility = View.VISIBLE
        } else {
            binding.miniPlayerLayout.visibility = View.VISIBLE
            binding.fullPlayerLayout.visibility = View.INVISIBLE
        }
    }

    private fun updateRepeatIcon() {
        val repeatMode = player?.repeatMode ?: Player.REPEAT_MODE_OFF
        val iconRes = if (repeatMode == Player.REPEAT_MODE_ONE) {
            R.drawable.ic_repeat_one
        } else {
            R.drawable.ic_repeat
        }
        binding.btnRepeat.setImageResource(iconRes)
        
        val color = if (repeatMode == Player.REPEAT_MODE_ONE) {
            resources.getColor(R.color.spotify_green, null)
        } else {
            resources.getColor(R.color.white, null)
        }
        binding.btnRepeat.setColorFilter(color)
    }

    override fun onStop() {
        super.onStop()
        // DO NOT release controller here because it's persistent!
        // But we should stop the progress runnable
        binding.root.removeCallbacks(updateProgressRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release here as it's the final destruction
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        controllerFuture = null
        mediaController = null
    }

    private fun updatePlayPauseIcon() {
        val isPlaying = player?.isPlaying ?: false
        val iconRes = if (isPlaying) {
            androidx.media3.ui.R.drawable.exo_ic_pause_circle_filled
        } else {
            androidx.media3.ui.R.drawable.exo_ic_play_circle_filled
        }
        binding.btnPlayPause.setImageResource(iconRes)
        binding.btnMiniPlayPause.setImageResource(iconRes)
    }

    private fun updateProgress() {
        player?.let {
            val current = it.currentPosition
            val duration = it.duration
            if (duration > 0) {
                binding.sliderProgress.value = (current.toFloat() / duration.toFloat() * 100).coerceIn(0f, 100f)
                binding.txtCurrentTime.text = formatTime(current)
                binding.txtTotalTime.text = formatTime(duration)
                
                // Update tonearm rotation if not dragging
                if (!isTonearmDragging) {
                    val angle = (current.toFloat() / duration.toFloat() * 45f).coerceIn(0f, 45f)
                    binding.imgTonearm.rotation = if (it.isPlaying) angle else -30f
                }
            }
        }
    }

    private fun formatTime(ms: Long): String {
        val seconds = (ms / 1000) % 60
        val minutes = (ms / (1000 * 60)) % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
