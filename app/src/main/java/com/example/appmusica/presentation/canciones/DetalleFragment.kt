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
import com.example.appmusica.util.FormatUtils
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
    @javax.inject.Inject lateinit var authManager: com.example.appmusica.data.local.AuthManager
    private var isTonearmDragging = false
    private lateinit var lyricsAdapter: com.example.appmusica.presentation.canciones.adapter.LyricsAdapter
    private lateinit var queueAdapter: com.example.appmusica.presentation.canciones.adapter.QueueAdapter

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
                viewModel.loadLyrics(it.id)
                // Solo reiniciar el reproductor si la canción realmente cambió
                if (lastCancionId != it.id) {
                    trySetupPlayerWithCurrentList()
                }
                lastCancionId = it.id
            }
        }

        viewModel.playbackQueue.observe(viewLifecycleOwner) { queue ->
            updatePlayerQueue(queue)
        }

        setupManualControls()
        setupMiniPlayerControls()
        setupTonearm()
        setupLyrics()
        setupQueue()
        observeLyricsAndMascota()
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
        binding.txtArtista.text = cancion.artista ?: ""
        binding.txtAlbum.text = cancion.album ?: ""

        // Styling for interactivity
        binding.txtArtista.paintFlags = binding.txtArtista.paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
        binding.txtAlbum.paintFlags = binding.txtAlbum.paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG

        binding.txtArtista.setOnClickListener {
            cancion.artistaIds?.firstOrNull()?.let { id ->
                (activity as? MainActivity)?.minimizePlayer()
                val bundle = Bundle().apply { putInt("artistId", id) }
                findNavController().navigate(R.id.artistaDetalleFragment, bundle)
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
        binding.txtLikesCount.text = "${FormatUtils.formatCount(cancion.likes)} likes"
        
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
        binding.txtMiniArtista.text = cancion.artista ?: ""

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

        binding.btnPrev.setOnClickListener {
            if (!authManager.canSkip()) {
                android.widget.Toast.makeText(requireContext(), "Límite de saltos alcanzado. ¡Hazte Premium!", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            authManager.incrementSkip()
            player?.seekToPrevious() 
        }
        binding.btnPrev.setClickAnimation()
        binding.btnNext.setOnClickListener {
            if (!authManager.canSkip()) {
                android.widget.Toast.makeText(requireContext(), "Límite de saltos alcanzado. ¡Hazte Premium!", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            authManager.incrementSkip()
            player?.seekToNext() 
        }
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
        binding.btnMiniPrev.setOnClickListener { 
            if (!authManager.canSkip()) {
                android.widget.Toast.makeText(requireContext(), "Límite de saltos alcanzado. ¡Hazte Premium!", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            authManager.incrementSkip()
            player?.seekToPrevious() 
        }
        binding.btnMiniNext.setOnClickListener { 
            if (!authManager.canSkip()) {
                android.widget.Toast.makeText(requireContext(), "Límite de saltos alcanzado. ¡Hazte Premium!", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            authManager.incrementSkip()
            player?.seekToNext() 
        }
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
            controller.setMediaItems(mediaItems, initialPosition, 0)
            controller.prepare()
            controller.play()
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
                        updateProgress()
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
                            // Incrementar reproducciones en la API
                            viewModel.incrementReproducciones(it.id)
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
                    val parent = v.parent as View
                    val pivotX = v.left + v.pivotX
                    val pivotY = v.top + v.pivotY
                    
                    val dx = event.rawX - (parent.left + pivotX)
                    val dy = event.rawY - (parent.top + pivotY)
                    
                    var angle = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
                    angle -= 90f // Base is at top, pointing down is 90 degrees in atan2 math. We want down to be 0 degrees rotation.
                    
                    // Allow dragging between 0 (parked) and 40 (end of record)
                    val clampedAngle = angle.coerceIn(0f, 40f)
                    v.rotation = clampedAngle
                    
                    if (clampedAngle > 10f) {
                        player?.let { p ->
                            // Map 8f..28f to 0..1 progress
                            val progress = ((clampedAngle - 8f) / 20f).coerceIn(0f, 1f)
                            val seekTo = (progress * p.duration).toLong()
                            p.seekTo(seekTo)
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    isTonearmDragging = false
                    val angle = v.rotation
                    player?.let { p ->
                        if (angle <= 10f) {
                            p.pause()
                            p.seekTo(0)
                        } else {
                            if (!p.isPlaying) p.play()
                        }
                    }
                    
                    // If pausing, immediately animate back to 0
                    if (angle <= 10f) {
                        ObjectAnimator.ofFloat(v, "rotation", v.rotation, 0f).apply {
                            duration = 300
                            start()
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

    private var tonearmAnimator: ObjectAnimator? = null

    private fun setupLyrics() {
        lyricsAdapter = com.example.appmusica.presentation.canciones.adapter.LyricsAdapter()
        binding.rvLyrics.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            adapter = lyricsAdapter
        }

        binding.btnLyrics.setOnClickListener {
            binding.lyricsOverlay.visibility = View.VISIBLE
            binding.lyricsOverlay.animate().alpha(1f).duration = 300
        }

        binding.btnCloseLyrics.setOnClickListener {
            binding.lyricsOverlay.animate().alpha(0f).setDuration(300).withEndAction {
                binding.lyricsOverlay.visibility = View.GONE
            }.start()
        }
    }

    private fun setupQueue() {
        queueAdapter = com.example.appmusica.presentation.canciones.adapter.QueueAdapter(
            onRemove = { pos -> viewModel.removeFromQueue(pos) }
        )
        binding.rvQueue.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            adapter = queueAdapter
        }

        binding.btnQueue.setOnClickListener {
            binding.queueOverlay.visibility = View.VISIBLE
            binding.queueOverlay.animate().alpha(1f).duration = 300
        }

        binding.btnCloseQueue.setOnClickListener {
            binding.queueOverlay.animate().alpha(0f).setDuration(300).withEndAction {
                binding.queueOverlay.visibility = View.GONE
            }.start()
        }

        viewModel.playbackQueue.observe(viewLifecycleOwner) { queue ->
            queueAdapter.update(queue)
        }
    }

    private fun observeLyricsAndMascota() {
        viewModel.lyrics.observe(viewLifecycleOwner) { letra ->
            letra?.lineas?.let { lyricsAdapter.updateLyrics(it) }
        }

        viewModel.activeMascota.observe(viewLifecycleOwner) { mascota ->
            if (mascota != null) {
                binding.imgMascota.visibility = View.VISIBLE
                val baseUrl = com.example.appmusica.di.NetworkModule.BASE_API_URL.removeSuffix("/")
                val fullUrl = if (mascota.urlSprite.startsWith("http")) mascota.urlSprite else baseUrl + mascota.urlSprite
                Glide.with(this).load(fullUrl).into(binding.imgMascota)
                animateMascota()
            } else {
                binding.imgMascota.visibility = View.GONE
            }
        }
    }

    private fun animateMascota() {
        val animX = ObjectAnimator.ofFloat(binding.imgMascota, "translationX", -10f, 10f).apply {
            duration = 1000
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
        }
        val animY = ObjectAnimator.ofFloat(binding.imgMascota, "translationY", -5f, 5f).apply {
            duration = 1200
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
        }
        animX.start()
        animY.start()
    }

    private fun updateProgress() {
        player?.let {
            val current = it.currentPosition
            val duration = it.duration
            if (duration > 0) {
                binding.sliderProgress.value = (current.toFloat() / duration.toFloat() * 100).coerceIn(0f, 100f)
                binding.txtCurrentTime.text = formatTime(current)
                binding.txtTotalTime.text = formatTime(duration)
                
                // Update lyrics sync
                val index = lyricsAdapter.setActiveLine(current)
                if (index != -1) {
                    binding.rvLyrics.computeVerticalScrollOffset()
                    binding.rvLyrics.smoothScrollToPosition(index)
                    // Centrar la línea activa si es posible
                    (binding.rvLyrics.layoutManager as? androidx.recyclerview.widget.LinearLayoutManager)?.scrollToPositionWithOffset(index, binding.rvLyrics.height / 2)
                }

                if (!isTonearmDragging) {
                    val isPlaying = it.isPlaying
                    val targetAngle = if (isPlaying) {
                        // Map progress to 8 degrees (start) to 28 degrees (end)
                        8f + (current.toFloat() / duration.toFloat() * 20f).coerceIn(0f, 20f)
                    } else {
                        0f // Parked vertically
                    }
                    
                    val currentAngle = binding.imgTonearm.rotation
                    // Si se acaba de pausar o el cambio es grande, animamos suavemente
                    if (!isPlaying && currentAngle > 2f) {
                        tonearmAnimator?.cancel()
                        tonearmAnimator = ObjectAnimator.ofFloat(binding.imgTonearm, "rotation", currentAngle, 0f).apply {
                            this.duration = 1000 // Slow return when pausing
                            start()
                        }
                    } else if (isPlaying) {
                        // Durante la reproducción, seguimos el progreso
                        binding.imgTonearm.rotation = targetAngle
                    }
                }
            }
        }
    }

    fun pausePlayback() {
        player?.pause()
    }

    private fun formatTime(ms: Long): String {
        val seconds = (ms / 1000) % 60
        val minutes = (ms / (1000 * 60)) % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    private fun updatePlayerQueue(queue: List<com.example.appmusica.domain.model.Cancion>) {
        val controller = mediaController ?: return
        if (queue.isEmpty()) return

        // Esta es una implementación simple: cuando cambia la cola, la "inyectamos" en el player
        // Pero para no romper la reproducción actual, solo añadimos lo que no esté ya
        val currentMediaItems = mutableListOf<androidx.media3.common.MediaItem>()
        for (i in 0 until controller.mediaItemCount) {
            currentMediaItems.add(controller.getMediaItemAt(i))
        }

        queue.forEach { song ->
            val uri = getFullUrl(song.urlAudio ?: "")
            if (currentMediaItems.none { it.localConfiguration?.uri?.toString() == uri }) {
                controller.addMediaItem(createMediaItem(song))
            }
        }
    }

    private fun createMediaItem(song: com.example.appmusica.domain.model.Cancion): androidx.media3.common.MediaItem {
        val fullAudioUrl = getFullUrl(song.urlAudio ?: "")
        val fullPortadaUrl = getFullUrl(song.urlPortada ?: "")

        val metadata = MediaMetadata.Builder()
            .setTitle(song.nombre)
            .setArtist(song.artista)
            .setAlbumTitle(song.album)
            .setArtworkUri(android.net.Uri.parse(fullPortadaUrl))
            .build()

        return androidx.media3.common.MediaItem.Builder()
            .setUri(fullAudioUrl)
            .setMediaMetadata(metadata)
            .build()
    }

    private fun getFullUrl(path: String): String {
        val baseUrl = com.example.appmusica.di.NetworkModule.BASE_API_URL.removeSuffix("/")
        return if (path.startsWith("http")) path else baseUrl + path
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tonearmAnimator?.cancel()
        _binding = null
    }
}
