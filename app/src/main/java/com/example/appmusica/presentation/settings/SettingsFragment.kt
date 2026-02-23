package com.example.appmusica.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.appmusica.R
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.appmusica.data.local.AuthManager
import com.example.appmusica.presentation.login.AuthViewModel
import com.example.appmusica.retrofit.ApiCancionesService
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

    private lateinit var ivProfile: ImageView
    private var selectedImageUri: Uri? = null

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            ivProfile.setImageURI(selectedImageUri)
            uploadImage()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        
        ivProfile = view.findViewById(R.id.ivProfile)
        val btnChangeProfile = view.findViewById<Button>(R.id.btnChangeProfile)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        btnChangeProfile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            selectImageLauncher.launch(intent)
        }

        btnLogout.setOnClickListener {
            authViewModel.logout()
            activity?.finish()
            startActivity(Intent(requireContext(), com.example.appmusica.presentation.login.LoginActivity::class.java))
        }

        // Cargar imagen de perfil si existe
        authManager.getUrlImagen()?.let { url ->
            val baseUrl = com.example.appmusica.di.NetworkModule.BASE_URL.replace("/api/", "")
            val fullUrl = baseUrl + url
            Glide.with(this)
                .load(fullUrl)
                .error(android.R.drawable.ic_menu_report_image)
                .circleCrop()
                .into(ivProfile)
        }

        return view
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
                        val baseUrl = com.example.appmusica.di.NetworkModule.BASE_URL.replace("/api/", "")
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