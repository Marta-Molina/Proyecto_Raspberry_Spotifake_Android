package com.example.appmusica.presentation

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.appmusica.R
import com.example.appmusica.domain.repository.AuthRepository
import com.example.appmusica.databinding.ActivityMainBinding
import com.example.appmusica.presentation.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var authManager: com.example.appmusica.data.local.AuthManager

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
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
        binding.bottomNavigationView.setupWithNavController(navController)

        // Configurar visibilidad del menú Admin
        val adminVisible = authManager.isAdmin()
        binding.bottomNavigationView.menu.findItem(R.id.adminFragment)?.isVisible = adminVisible
        binding.navigationView.menu.findItem(R.id.adminFragment)?.isVisible = adminVisible

        // Usuario en el header
        val header = binding.navigationView.getHeaderView(0)
        // Intentar obtener el nombre si estuviera guardado, si no, uno genérico
        header.findViewById<TextView>(R.id.txtUser).text = "Usuario Spotifake"
        
        val ivUserThumb = header.findViewById<android.widget.ImageView>(R.id.ivUserThumb)
        authManager.getUrlImagen()?.let { url ->
            val baseUrl = com.example.appmusica.di.NetworkModule.BASE_URL.removeSuffix("/")
            com.bumptech.glide.Glide.with(this)
                .load(baseUrl + url)
                .error(android.R.drawable.ic_menu_report_image)
                .circleCrop()
                .into(ivUserThumb)
        }
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