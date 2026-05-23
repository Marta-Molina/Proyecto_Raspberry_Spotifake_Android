package com.example.appmusica.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.appmusica.R
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.Manifest
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.example.appmusica.util.ThemeManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.appmusica.data.local.AuthManager
import com.example.appmusica.presentation.login.AuthViewModel
import com.example.appmusica.retrofit.ApiCancionesService
import com.example.appmusica.util.setClickAnimation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    @Inject
    lateinit var authManager: AuthManager

    @Inject
    lateinit var apiService: ApiCancionesService

    private val authViewModel: AuthViewModel by viewModels()
    private val sessionViewModel: SessionViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    private lateinit var ivProfile: ImageView
    private lateinit var rvHistory: RecyclerView
    private lateinit var rvMascotas: RecyclerView
    private lateinit var sessionAdapter: SessionAdapter
    private lateinit var mascotaAdapter: MascotaAdapter
    private var selectedImageUri: Uri? = null

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            ivProfile.setImageURI(selectedImageUri)
            uploadImage()
        }
    }

    // Toma foto con la cámara y devuelve un Bitmap (preview / NFC)
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) {
            // Guardar bitmap en un archivo temporal y usar el mismo flujo de subida
            val file = saveBitmapToCache(bitmap)
            if (file != null) {
                selectedImageUri = Uri.fromFile(file)
                ivProfile.setImageBitmap(bitmap)
                uploadImage()
            } else {
                Toast.makeText(context, "Error al guardar la imagen de la cámara", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            takePictureLauncher.launch(null)
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        ivProfile = view.findViewById(R.id.ivUserDetailPhoto)
        rvHistory = view.findViewById(R.id.rvHistory)
        val btnChangeProfile = view.findViewById<Button>(R.id.btnChangeProfile)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        val btnChangeAccount = view.findViewById<Button>(R.id.btnChangeAccount)
        val btnDeleteHistory = view.findViewById<TextView>(R.id.btnDeleteHistory)
        val txtUsername = view.findViewById<TextView>(R.id.txtSettingsUsername)
        val txtAccountType = view.findViewById<TextView>(R.id.txtAccountType)

        // Populate user info from AuthManager
        txtUsername.text = authManager.getUsername() ?: ""
        txtAccountType.text = if (authManager.isPremium()) "Usuario Premium" else "Usuario Estándar"

        setupThemeSelection(view)
        setupRecyclerView()
        observeSessionHistory()

        btnChangeProfile.setOnClickListener {
            // Mostrar opciones: Galería o Cámara
            val options = arrayOf("Galería", "Cámara")
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Seleccionar imagen")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> {
                            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                            selectImageLauncher.launch(intent)
                        }
                        1 -> {
                            // Comprobar permiso de cámara
                            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                takePictureLauncher.launch(null)
                            } else {
                                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    }
                }
                .show()
        }
        btnChangeProfile.setClickAnimation()

        btnLogout.setOnClickListener {
            authViewModel.logout()
            activity?.finish()
            startActivity(Intent(requireContext(), com.example.appmusica.presentation.login.LoginActivity::class.java))
        }
        btnLogout.setClickAnimation()

        btnChangeAccount.setOnClickListener {
            // El comportamiento de cambiar cuenta es similar al logout,
            // pero podríamos añadir un flag o simplemente ir al login
            authViewModel.logout()
            activity?.finish()
            startActivity(Intent(requireContext(), com.example.appmusica.presentation.login.LoginActivity::class.java))
        }
        btnChangeAccount.setClickAnimation()

        btnDeleteHistory.setOnClickListener {
            sessionViewModel.clearHistory()
        }
        btnDeleteHistory.setClickAnimation()

        setupMascotas(view)
        setupUtilities(view)

        // Cargar imagen de perfil si existe
        authManager.getUrlImagen()?.let { url ->
            val baseUrl = com.example.appmusica.di.NetworkModule.BASE_API_URL.removeSuffix("/")
            val fullUrl = if (url.startsWith("http")) url else baseUrl + url

            Glide.with(this)
                .load(fullUrl)
                .error(android.R.drawable.ic_menu_report_image)
                .circleCrop()
                .into(ivProfile)
        }

        return view
    }

    private fun setupThemeSelection(view: View) {
        val themeManager = ThemeManager(requireContext())
        val isPremium = authManager.isPremium()

        val switchLight = view.findViewById<SwitchMaterial>(R.id.switchLightTheme)
        val premiumLayout = view.findViewById<LinearLayout>(R.id.premiumThemesLayout)

        // Initial state
        switchLight.isChecked = themeManager.getTheme() == ThemeManager.THEME_LIGHT

        if (isPremium) {
            premiumLayout.visibility = View.VISIBLE
            // Setup clicks for colored views
            view.findViewById<View>(R.id.themeDark).setOnClickListener { changeTheme(ThemeManager.THEME_DARK) }
            view.findViewById<View>(R.id.themeGold).setOnClickListener { changeTheme(ThemeManager.THEME_GOLD) }
            view.findViewById<View>(R.id.themePink).setOnClickListener { changeTheme(ThemeManager.THEME_PINK) }
            view.findViewById<View>(R.id.themeBlue).setOnClickListener { changeTheme(ThemeManager.THEME_BLUE) }
            view.findViewById<View>(R.id.themeEmerald).setOnClickListener { changeTheme(ThemeManager.THEME_EMERALD) }

            // Add click animations to these views too
            view.findViewById<View>(R.id.themeDark).setClickAnimation()
            view.findViewById<View>(R.id.themeGold).setClickAnimation()
            view.findViewById<View>(R.id.themePink).setClickAnimation()
            view.findViewById<View>(R.id.themeBlue).setClickAnimation()
            view.findViewById<View>(R.id.themeEmerald).setClickAnimation()
        }

        switchLight.setOnCheckedChangeListener { _, isChecked ->
            val newTheme = if (isChecked) ThemeManager.THEME_LIGHT else ThemeManager.THEME_DARK
            changeTheme(newTheme)
        }
    }

    private fun changeTheme(theme: String) {
        val themeManager = ThemeManager(requireContext())
        if (themeManager.getTheme() != theme) {
            themeManager.setTheme(theme)
            activity?.recreate() // Restart activity to apply theme
        }
    }

    private fun setupRecyclerView() {
        sessionAdapter = SessionAdapter()
        rvHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sessionAdapter
        }
    }

    private fun observeSessionHistory() {
        lifecycleScope.launch {
            sessionViewModel.sessions.collect { sessions ->
                sessionAdapter.submitList(sessions)
            }
        }
    }

    private fun uploadImage() {
        val uri = selectedImageUri ?: return
        val file = getFileFromUri(uri) ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("imagen", file.name, requestFile)
                val userId = authManager.getUserId()

                val response = apiService.uploadProfileImage(userId, body)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val user = response.body()!!
                        authManager.saveUrlImagen(user.urlImagen)
                        Toast.makeText(context, "Imagen de perfil actualizada", Toast.LENGTH_SHORT).show()

                        // Cargar la imagen usando la URL devuelta por el servidor
                        val baseUrl = com.example.appmusica.di.NetworkModule.BASE_API_URL.removeSuffix("/")
                        val fullUrl = baseUrl + user.urlImagen

                        Glide.with(this@SettingsFragment)
                            .load(fullUrl)
                            .placeholder(R.drawable.user)
                            .error(android.R.drawable.ic_menu_report_image)
                            .circleCrop()
                            .into(ivProfile)
                    } else {
                        Toast.makeText(context, "Error al subir imagen: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupMascotas(view: View) {
        val isPremium = authManager.isPremium()
        val layout = view.findViewById<LinearLayout>(R.id.mascotaSelectionLayout)
        if (!isPremium) {
            layout.visibility = View.GONE
            return
        }
        layout.visibility = View.VISIBLE

        rvMascotas = view.findViewById(R.id.rvMascotas)
        val btnToggleMascotas = view.findViewById<View>(R.id.btnToggleMascotas)
        val ivMascotaArrow = view.findViewById<ImageView>(R.id.ivMascotaArrow)

        btnToggleMascotas.setOnClickListener {
            if (rvMascotas.visibility == View.VISIBLE) {
                rvMascotas.visibility = View.GONE
                ivMascotaArrow.rotation = 0f
            } else {
                rvMascotas.visibility = View.VISIBLE
                ivMascotaArrow.rotation = 180f
            }
        }

        mascotaAdapter = MascotaAdapter { mascota ->
            if (mascota.premiumDefault || mascota.esComprada) {
                settingsViewModel.selectMascota(mascota)
                Toast.makeText(context, "Mascota seleccionada", Toast.LENGTH_SHORT).show()
            } else {
                showBuyMascotaDialog(mascota)
            }
        }
        rvMascotas.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = mascotaAdapter
        }

        settingsViewModel.mascotas.observe(viewLifecycleOwner) {
            mascotaAdapter.update(it)
        }
        settingsViewModel.loadMascotas()
    }

    private fun setupUtilities(view: View) {
        view.findViewById<Button>(R.id.btnSleepTimer).setOnClickListener {
            showSleepTimerDialog()
        }
        view.findViewById<Button>(R.id.btnManageAlarms).setOnClickListener {
            findNavController().navigate(R.id.alarmsFragment)
        }
        view.findViewById<Button>(R.id.btnResumenAnual).setOnClickListener {
            findNavController().navigate(R.id.resumenAnualFragment)
        }
    }

    private fun showBuyMascotaDialog(mascota: com.example.appmusica.domain.model.Mascota) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Desbloquear mascota")
            .setMessage("¿Quieres desbloquear a ${mascota.nombre} por ${mascota.precio} monedas?")
            .setPositiveButton("Desbloquear") { _, _ ->
                settingsViewModel.buyMascota(mascota.id)
                Toast.makeText(context, "Mascota desbloqueada!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showSleepTimerDialog() {
        val options = arrayOf("5 minutos", "10 minutos", "15 minutos", "30 minutos", "45 minutos", "1 hora", "Desactivar")
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Temporizador de apagado")
            .setItems(options) { _, which ->
                val minutes = when (which) {
                    0 -> 5
                    1 -> 10
                    2 -> 15
                    3 -> 30
                    4 -> 45
                    5 -> 60
                    else -> 0
                }
                if (minutes > 0) {
                    (activity as? com.example.appmusica.presentation.MainActivity)?.startSleepTimer(minutes)
                    Toast.makeText(context, "Temporizador activado: $minutes min", Toast.LENGTH_SHORT).show()
                } else {
                    (activity as? com.example.appmusica.presentation.MainActivity)?.stopSleepTimer()
                    Toast.makeText(context, "Temporizador desactivado", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun getFileFromUri(uri: Uri): File? {
        val fileName = getFileName(uri) ?: "temp_image.jpg"
        val tempFile = File(requireContext().cacheDir, fileName)

        return try {
            requireContext().contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveBitmapToCache(bitmap: Bitmap): File? {
        val fileName = "camera_${System.currentTimeMillis()}.jpg"
        val tempFile = File(requireContext().cacheDir, fileName)
        return try {
            tempFile.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = it.getString(index)
                }
            }
        }
        return name
    }
}