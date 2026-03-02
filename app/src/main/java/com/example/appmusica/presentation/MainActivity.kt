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

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var bottomSheetBehavior: com.google.android.material.bottomsheet.BottomSheetBehavior<android.widget.FrameLayout>

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
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
        
        // Transparencia total del fondo para el efecto flotante
        bottomNav.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        
        // Colores de la barra
        val greenColor = androidx.core.content.ContextCompat.getColor(this, R.color.spotify_green)
        bottomNav.navBackgroundColor = greenColor
        bottomNav.fabBackgroundColor = greenColor
        bottomNav.unSelectedColor = android.graphics.Color.WHITE
        bottomNav.selectedColor = android.graphics.Color.BLACK
        
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

        // Must pass as Array, not List
        bottomNav.setMenuItems(menuItems.toTypedArray(), 0)
        bottomNav.setupWithNavController(navController)
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
        val baseUrl = NetworkModule.BASE_URL.removeSuffix("/")
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

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}