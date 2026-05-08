package com.example.appmusica.data.repository

import android.util.Log
import com.example.appmusica.domain.model.Artista
import com.example.appmusica.domain.repository.ArtistaRepository
import com.example.appmusica.retrofit.ApiCancionesService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtistaRepositoryImpl @Inject constructor(
    private val api: ApiCancionesService
) : ArtistaRepository {

    override suspend fun getArtistaById(id: Int): Artista? {
        return try {
            val response = api.getArtistaById(id)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("API_TEST", "Error fetching artista $id: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("API_TEST", "Exception fetching artista $id: ${e.message}")
            null
        }
    }

    override suspend fun followArtista(id: Int): Boolean {
        return try {
            val response = api.socialFollowArtista(id)
            if (!response.isSuccessful) {
                Log.e("API_TEST", "Error following artista $id: ${response.code()}")
            }
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("API_TEST", "Exception following artista $id: ${e.message}")
            false
        }
    }

    override suspend fun unfollowArtista(id: Int): Boolean {
        return try {
            val response = api.socialUnfollowArtista(id)
            if (!response.isSuccessful) {
                Log.e("API_TEST", "Error unfollowing artista $id: ${response.code()}")
            }
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("API_TEST", "Exception unfollowing artista $id: ${e.message}")
            false
        }
    }

    override suspend fun createArtista(nombre: String, foto: File?): Artista? {
        return try {
            val nameBody = nombre.toRequestBody("text/plain".toMediaTypeOrNull())
            val fotoPart = foto?.let {
                val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("foto", it.name, requestFile)
            }
            val response = api.createArtista(nameBody, fotoPart)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            Log.e("API_TEST", "Error creating artista: ${e.message}")
            null
        }
    }

    override suspend fun updateArtista(id: Int, nombre: String?, foto: File?): Artista? {
        return try {
            val nameBody = nombre?.toRequestBody("text/plain".toMediaTypeOrNull())
            val fotoPart = foto?.let {
                val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("foto", it.name, requestFile)
            }
            val response = api.updateArtista(id, nameBody, fotoPart)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            Log.e("API_TEST", "Error updating artista $id: ${e.message}")
            null
        }
    }

    override suspend fun deleteArtista(id: Int): Boolean {
        return try {
            api.deleteArtista(id).isSuccessful
        } catch (e: Exception) {
            Log.e("API_TEST", "Error deleting artista $id: ${e.message}")
            false
        }
    }

    override suspend fun incrementFollowers(id: Int): Boolean {
        return try {
            api.incrementArtistaFollowers(id).isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun decrementFollowers(id: Int): Boolean {
        return try {
            api.decrementArtistaFollowers(id).isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
