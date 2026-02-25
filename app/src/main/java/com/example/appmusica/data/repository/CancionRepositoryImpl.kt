package com.example.appmusica.data.repository

import android.util.Log
import com.example.appmusica.domain.model.Cancion
import com.example.appmusica.domain.repository.CancionRepository
import com.example.appmusica.retrofit.ApiCancionesService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CancionRepositoryImpl @Inject constructor(
    private val api: ApiCancionesService
) : CancionRepository {

    override suspend fun getCanciones(nombre: String?, artista: String?, album: String?): List<Cancion> {
        return try {
            val response = api.getCanciones(nombre, artista, album)
            if (response.isSuccessful) {
                Log.d("API_TEST", response.body().toString())
                response.body() ?: emptyList()
            } else {
                Log.e("API_TEST", "Error fetching canciones: ${response.code()} ${response.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("API_TEST", "Exception fetching canciones: ${e.message}", e)
            emptyList()
        }
    }


    override suspend fun getCancion(id: Int): Cancion? {
        return try {
            val response = api.getCancionById(id)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("API_TEST", "Error fetching cancion $id: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("API_TEST", "Exception fetching cancion $id: ${e.message}")
            null
        }
    }

    override suspend fun addCancion(cancion: Cancion) {
        try {
            val response = api.addCancion(cancion)
            if (!response.isSuccessful) {
                Log.e("API_TEST", "Error adding cancion: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("API_TEST", "Exception adding cancion: ${e.message}")
        }
    }

    override suspend fun updateCancion(id: Int, cancion: Cancion) {
        try {
            val mediaType = "text/plain".toMediaTypeOrNull()
            val nombre = cancion.nombre.toRequestBody(mediaType)
            val artista = cancion.artista.toRequestBody(mediaType)
            val album = cancion.album.toRequestBody(mediaType)
            val genero = cancion.genero.toString().toRequestBody(mediaType)
            val likes = cancion.likes.toString().toRequestBody(mediaType)

            val response = api.updateCancion(id, nombre, artista, album, genero, likes)
            if (!response.isSuccessful) {
                Log.e("API_TEST", "Error updating cancion $id: ${response.code()} ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("API_TEST", "Exception updating cancion $id: ${e.message}")
        }
    }

    override suspend fun deleteCancion(id: Int): Boolean {
        return try {
            val response = api.deleteCancion(id)
            if (!response.isSuccessful) {
                Log.e("API_TEST", "Error deleting cancion $id: ${response.code()}")
            }
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("API_TEST", "Exception deleting cancion $id: ${e.message}")
            false
        }
    }

    override suspend fun getGeneros(): List<com.example.appmusica.domain.model.Genero> {
        return try {
            val response = api.getGeneros()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                Log.e("API_TEST", "Error fetching generos: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("API_TEST", "Exception fetching generos: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getArtistas(): List<com.example.appmusica.domain.model.Artista> {
        return try {
            val response = api.getArtistas()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                Log.e("API_TEST", "Error fetching artistas: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("API_TEST", "Exception fetching artistas: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getAlbumsByArtist(artistId: Int): List<com.example.appmusica.domain.model.Album> {
        return try {
            val response = api.getAlbumsByArtist(artistId)
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                Log.e("API_TEST", "Error fetching albums for artistId $artistId: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("API_TEST", "Exception fetching albums for artistId $artistId: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getCancionesByAlbum(albumId: Int): List<Cancion> {
        return try {
            val response = api.getCancionesByAlbum(albumId)
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                Log.e("API_TEST", "Error fetching canciones for albumId $albumId: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("API_TEST", "Exception fetching canciones for albumId $albumId: ${e.message}", e)
            emptyList()
        }
    }
}
