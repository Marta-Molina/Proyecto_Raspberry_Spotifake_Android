package com.example.appmusica.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.appmusica.R
import com.example.appmusica.di.NetworkModule
import com.example.appmusica.domain.repository.AuthRepository
import com.example.appmusica.databinding.ActivityMainBinding
import com.example.appmusica.presentation.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var authManager: com.example.appmusica.data.local.AuthManager

    @Inject
    lateinit var mascotaRepository: com.example.appmusica.domain.repository.MascotaRepository

    @Inject
    lateinit var apiService: com.example.appmusica.retrofit.ApiCancionesService

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var bottomSheetBehavior: com.google.android.material.bottomsheet.BottomSheetBehavior<android.widget.FrameLayout>

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        val themeManager = com.example.appmusica.util.ThemeManager(this)
        setTheme(themeManager.getThemeResId())
        
        super.onCreate(savedInstanceState)
        
        checkNotificationPermission()
        
        // Verificar sesión
        if (!authRepository.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        // Obtener NavController de forma segura
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val topLevelDestinations = mutableSetOf(R.id.cancionesFragment, R.id.playlistsFragment, R.id.settingsFragment)
        if (authManager.isAdmin()) {
            topLevelDestinations.add(R.id.adminFragment)
        }

        appBarConfiguration = AppBarConfiguration(topLevelDestinations, binding.drawerLayout)

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navigationView.setupWithNavController(navController)

        // Custom navigation listener for Side Drawer to handle "Canciones" and "Comunidad" correctly
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.cancionesFragment -> {
                    navController.navigate(R.id.cancionesFragment)
                    syncBottomNavSelection(R.id.cancionesFragment)
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.playlistsFragment -> {
                    navController.navigate(R.id.playlistsFragment)
                    syncBottomNavSelection(R.id.playlistsFragment)
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.settingsFragment -> {
                    navController.navigate(R.id.settingsFragment)
                    syncBottomNavSelection(R.id.settingsFragment)
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.socialFragment -> {
                    navController.navigate(R.id.socialFragment)
                    // Sync social if added to bottom nav later
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.adminFragment -> {
                    navController.navigate(R.id.adminFragment)
                    syncBottomNavSelection(R.id.adminFragment)
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.menu_logout -> {
                    authRepository.logout()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        // Setup CurvedBottomNavigation
        setupCurvedBottomNavigation(navController)
        
        // Auto-minimize player on navigation
        navController.addOnDestinationChangedListener { _, _, _ ->
            if (binding.playerContainer.visibility == View.VISIBLE && 
                bottomSheetBehavior.state == com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
            }
        }
        
        // Drawer admin visibility
        binding.navigationView.menu.findItem(R.id.adminFragment)?.isVisible = authManager.isAdmin()

        // Usuario en el header
        val header = binding.navigationView.getHeaderView(0)
        header.findViewById<TextView>(R.id.txtUser).text = authManager.getUsername() ?: "Usuario Spotifake"
        val ivUserThumb = header.findViewById<ImageView>(R.id.ivUserThumb)

        // Observe profile image reactively — updates instantly when changed in Settings
        lifecycleScope.launch {
            authManager.profileImageUrl.collectLatest { url ->
                loadNavAvatar(ivUserThumb, url)
            }
        }

        setupBottomSheet()
        setupAds()
        setupMascot()
        setupSleepScreen()
        setupConnectivityListener()

        // Control del botón atrás del sistema
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 1. Si el reproductor está expandido, minimizarlo
                if (binding.playerContainer.visibility == View.VISIBLE && 
                    bottomSheetBehavior.state == com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED) {
                    
                    bottomSheetBehavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
                    
                    // Si estamos en una pantalla secundaria, volvemos atrás. 
                    // Si estamos en la principal, solo hemos minimizado el player.
                    if (navController.currentDestination?.id != R.id.cancionesFragment) {
                        checkAndNavigateBack(navController)
                    }
                    return
                }

                // 2. Lógica de navegación general
                checkAndNavigateBack(navController)
            }
        })
    }

    private fun checkAndNavigateBack(navController: androidx.navigation.NavController) {
        val currentId = navController.currentDestination?.id ?: return

        // Pantallas "Raíz" o de primer nivel
        val topLevelDestinations = setOf(
            R.id.cancionesFragment, 
            R.id.playlistsFragment, 
            R.id.settingsFragment, 
            R.id.adminFragment
        )

        // Si estamos en una pantalla profunda (Detalles, Gestión técnica, etc.), volvemos al nivel anterior
        if (currentId !in topLevelDestinations) {
            navController.popBackStack()
            return
        }

        // Si estamos en una de las pestañas principales (que no es Home), volvemos a Home
        if (currentId != R.id.cancionesFragment) {
            navController.navigate(R.id.cancionesFragment)
            syncBottomNavSelection(R.id.cancionesFragment)
            return
        }

        // Si ya estamos en Home, pedimos confirmación para salir
        showExitConfirmation()
    }

    private fun showExitConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Cerrar Spotifake")
            .setMessage("¿Estás seguro de que deseas salir?")
            .setPositiveButton("Sí") { _, _ -> finish() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun syncBottomNavSelection(destinationId: Int) {
        val bottomNav = binding.bottomNavigation as? np.com.susanthapa.curved_bottom_navigation.CurvedBottomNavigationView
        bottomNav?.let {
            val items = it.getMenuItems()
            val index = items.indexOfFirst { item -> item.destinationId == destinationId }
            if (index != -1) {
                it.setMenuItems(items, index)
            }
        }
    }

    private fun setupCurvedBottomNavigation(navController: androidx.navigation.NavController) {
        val bottomNav = binding.bottomNavigation as np.com.susanthapa.curved_bottom_navigation.CurvedBottomNavigationView
        val themeManager = com.example.appmusica.util.ThemeManager(this)
        
        // Transparencia total del fondo para el efecto flotante
        bottomNav.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        
        // Colores de la barra extraídos del tema
        val primaryColor = ContextCompat.getColor(this, R.color.spotify_green)
        
        bottomNav.navBackgroundColor = primaryColor
        bottomNav.fabBackgroundColor = primaryColor
        
        // Mejora de colores según el tema para evitar iconos invisibles
        val currentTheme = themeManager.getTheme()
        if (currentTheme == com.example.appmusica.util.ThemeManager.THEME_LIGHT) {
            bottomNav.unSelectedColor = android.graphics.Color.parseColor("#666666")
            bottomNav.selectedColor = android.graphics.Color.WHITE
        } else {
            bottomNav.unSelectedColor = android.graphics.Color.WHITE
            bottomNav.selectedColor = android.graphics.Color.BLACK
        }
        
        // Items definition (icon, avdIcon, destinationId)
        val menuItems = mutableListOf(
            np.com.susanthapa.curved_bottom_navigation.CbnMenuItem(
                R.drawable.ic_nav_music,
                R.drawable.avd_nav_music,
                R.id.cancionesFragment
            ),
            np.com.susanthapa.curved_bottom_navigation.CbnMenuItem(
                R.drawable.ic_nav_playlist,
                R.drawable.avd_nav_playlist,
                R.id.playlistsFragment
            ),
            np.com.susanthapa.curved_bottom_navigation.CbnMenuItem(
                R.drawable.ic_nav_social,
                R.drawable.ic_nav_social,
                R.id.socialFragment
            ),
            np.com.susanthapa.curved_bottom_navigation.CbnMenuItem(
                R.drawable.ic_nav_settings,
                R.drawable.avd_nav_settings,
                R.id.settingsFragment
            )
        )
        
        if (authManager.isAdmin()) {
            menuItems.add(
                np.com.susanthapa.curved_bottom_navigation.CbnMenuItem(
                    R.drawable.ic_nav_admin,
                    R.drawable.avd_nav_admin,
                    R.id.adminFragment
                )
            )
        }

        // Determinar el índice actual para evitar que el icono "salte" a Home al cambiar de tema
        val currentIndex = when(navController.currentDestination?.id) {
            R.id.cancionesFragment -> 0
            R.id.playlistsFragment -> 1
            R.id.socialFragment -> 2
            R.id.settingsFragment -> 3
            R.id.adminFragment -> if (authManager.isAdmin()) 4 else 0
            else -> 0
        }

        // Delay slightly or use post to ensure view is measured, preventing displacement
        bottomNav.post {
            bottomNav.setMenuItems(menuItems.toTypedArray(), currentIndex)
            bottomNav.setupWithNavController(navController)
        }
    }

    private fun setupAds() {
        val adView = binding.adBannerInclude.root
        if (authManager.isPremium()) {
            adView.visibility = View.GONE
            return
        }
        
        adView.visibility = View.VISIBLE
        adView.setOnClickListener { showPremiumInfo() }
        
        // Listener para cerrar el anuncio
        binding.adBannerInclude.btnMinimizeAd.setOnClickListener {
            adView.visibility = View.GONE
        }
        
        // Load a "random" ad or just show the default one
        // Ideally we fetch from API: /ads/random
        lifecycleScope.launch {
            try {
                val response = apiService.getRandomAd()
                if (response.isSuccessful && response.body() != null) {
                    val ad = response.body()!!
                    binding.adBannerInclude.tvAdTitle.text = ad.titulo
                    binding.adBannerInclude.tvAdDescription.text = ad.descripcion
                    
                    val baseUrl = NetworkModule.BASE_API_URL.removeSuffix("/")
                    val fullUrl = if (ad.urlImagen.startsWith("http")) ad.urlImagen else baseUrl + ad.urlImagen
                    
                    Glide.with(this@MainActivity)
                        .load(fullUrl)
                        .placeholder(R.drawable.placeholder)
                        .into(binding.adBannerInclude.ivAdImage)
                }
            } catch (e: Exception) {
                // Keep default ad content
            }
        }
    }

    fun showPremiumInfo() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("¡Pásate a Spotifake Premium!")
            .setMessage("Disfruta de música sin anuncios, saltos ilimitados de canciones, temas personalizados exclusivos y mucho más.")
            .setPositiveButton("Ver Planes") { _, _ ->
                // TODO: Redirect to payment or premium settings
                Toast.makeText(this, "Funcionalidad de pago en desarrollo", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Más tarde", null)
            .show()
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(binding.playerContainer)
        bottomSheetBehavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
        
        bottomSheetBehavior.addBottomSheetCallback(object : com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: android.view.View, newState: Int) {
                if (newState == com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN) {
                    binding.playerContainer.visibility = android.view.View.GONE
                    // Detener reproducción al ocultar el reproductor
                    val fragment = supportFragmentManager.findFragmentById(R.id.playerContainer) as? com.example.appmusica.presentation.canciones.DetalleFragment
                    fragment?.stopPlayback()
                }
                
                // Notify fragment of state change
                val fragment = supportFragmentManager.findFragmentById(R.id.playerContainer) as? com.example.appmusica.presentation.canciones.DetalleFragment
                fragment?.onBottomSheetStateChanged(newState)
            }

            override fun onSlide(bottomSheet: android.view.View, slideOffset: Float) {
                // Transition between mini and full UI
                val fragment = supportFragmentManager.findFragmentById(R.id.playerContainer) as? com.example.appmusica.presentation.canciones.DetalleFragment
                fragment?.onBottomSheetSlide(slideOffset)
            }
        })
    }

    fun expandPlayer(position: Int) {
        binding.playerContainer.visibility = android.view.View.VISIBLE
        val fragment = supportFragmentManager.findFragmentById(R.id.playerContainer) as? com.example.appmusica.presentation.canciones.DetalleFragment
        
        if (fragment == null) {
            val bundle = Bundle().apply { putInt("position", position) }
            val newFragment = com.example.appmusica.presentation.canciones.DetalleFragment().apply {
                arguments = bundle
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.playerContainer, newFragment)
                .commitNow()
        } else {
            fragment.updatePlaylistPosition(position)
            // Manual sync in case the state doesn't change (e.g. already expanded)
            fragment.onBottomSheetStateChanged(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED)
        }
        
        bottomSheetBehavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
    }

    fun minimizePlayer() {
        bottomSheetBehavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
    }

    fun navigateToArtist(artistId: Int) {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bundle = Bundle().apply { putInt("artistId", artistId) }
        navController.navigate(R.id.artistaDetalleFragment, bundle)
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun loadNavAvatar(iv: ImageView, url: String?) {
        if (url == null) return
        val baseUrl = NetworkModule.BASE_API_URL.removeSuffix("/")
        Glide.with(this)
            .load(baseUrl + url)
            .placeholder(android.R.drawable.ic_menu_report_image)
            .error(android.R.drawable.ic_menu_report_image)
            .circleCrop()
            .into(iv)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_logout) {
            authRepository.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private var sleepTimer: java.util.Timer? = null
    private var sleepMinutesSet: Int = 0

    fun startSleepTimer(minutes: Int) {
        stopSleepTimer()
        sleepMinutesSet = minutes
        sleepTimer = java.util.Timer()
        sleepTimer?.schedule(object : java.util.TimerTask() {
            override fun run() {
                runOnUiThread {
                    // Parar la música
                    val fragment = supportFragmentManager.findFragmentById(R.id.playerContainer) as? com.example.appmusica.presentation.canciones.DetalleFragment
                    fragment?.pausePlayback()
                    
                    // Mostrar pantalla de sueño (YouTube Style)
                    binding.tvSleepSummary.text = "Has pasado $sleepMinutesSet minutos dentro de la app."
                    binding.sleepScreenOverlay.visibility = View.VISIBLE
                }
            }
        }, minutes * 60 * 1000L)
    }

    private fun setupSleepScreen() {
        binding.btnExitSleep.setOnClickListener {
            binding.sleepScreenOverlay.visibility = View.GONE
        }
        
        binding.btnRemindAgain.setOnClickListener {
            binding.sleepScreenOverlay.visibility = View.GONE
            Toast.makeText(this, "Se volverá a recordar en 15 minutos", Toast.LENGTH_SHORT).show()
            startSleepTimer(15) // Recordar en 15 minutos
        }
    }

    fun stopSleepTimer() {
        sleepTimer?.cancel()
        sleepTimer = null
    }

    private lateinit var connectivityManager: ConnectivityManager
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = runOnUiThread {
            binding.offlineScreenOverlay.visibility = View.GONE
        }
        override fun onLost(network: Network) = runOnUiThread {
            binding.offlineScreenOverlay.visibility = View.VISIBLE
        }
    }

    private fun setupConnectivityListener() {
        connectivityManager = getSystemService(ConnectivityManager::class.java)
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        
        // Initial check
        val activeNetwork = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
        val isOnline = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        binding.offlineScreenOverlay.visibility = if (isOnline) View.GONE else View.VISIBLE
    }

    private fun setupMascot() {
        if (!authManager.isPremium()) {
            binding.ivMascot.visibility = View.GONE
            return
        }

        lifecycleScope.launch {
            try {
                val activeMascot = mascotaRepository.getActiveMascota()
                if (activeMascot != null) {
                    binding.ivMascot.visibility = View.VISIBLE
                    val baseUrl = NetworkModule.BASE_API_URL.removeSuffix("/")
                    val fullUrl = if (activeMascot.urlSprite.startsWith("http")) activeMascot.urlSprite else baseUrl + activeMascot.urlSprite
                    
                    Glide.with(this@MainActivity)
                        .load(fullUrl)
                        .into(binding.ivMascot)
                    
                    startMascotAnimation()
                } else {
                    binding.ivMascot.visibility = View.GONE
                }
            } catch (e: Exception) {
                binding.ivMascot.visibility = View.GONE
            }
        }
    }

    private fun startMascotAnimation() {
        val mascot = binding.ivMascot
        val random = java.util.Random()
        
        lifecycleScope.launch {
            while (true) {
                // Movimiento aleatorio suavizado
                val nextX = random.nextInt(maxOf(1, binding.root.width - mascot.width)).toFloat()
                val nextY = random.nextInt(maxOf(1, binding.root.height - mascot.height)).toFloat()
                
                mascot.animate()
                    .x(nextX)
                    .y(nextY)
                    .setDuration(5000)
                    .withEndAction {
                        // Pequeña animación de salto o escala al llegar
                        mascot.animate()
                            .scaleX(1.2f)
                            .scaleY(1.2f)
                            .setDuration(500)
                            .withEndAction {
                                mascot.animate()
                                    .scaleX(1.0f)
                                    .scaleY(1.0f)
                                    .setDuration(500)
                                    .start()
                            }.start()
                    }
                    .start()
                
                kotlinx.coroutines.delay(8000) // Esperar antes del siguiente movimiento
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}