package com.example.appmusica.models

class Cancion(
    var nombre: String,
    var artista: String,
    var album: String,
    var duracion: String,
    var imagen: String
) {
    override fun toString(): String {
        return "Cancion(" +
                "nombre='$nombre', " +
                "artista='$artista', " +
                "album='$album', " +
                "duracion='$duracion', " +
                "imagen='$imagen')"
    }
}