package com.example.appmusica.models

import android.os.Parcel
import android.os.Parcelable

data class Cancion(
    var nombre: String,
    var artista: String,
    var album: String,
    var duracion: String,
    var imagen: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nombre)
        parcel.writeString(artista)
        parcel.writeString(album)
        parcel.writeString(duracion)
        parcel.writeString(imagen)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Cancion> {
        override fun createFromParcel(parcel: Parcel): Cancion {
            return Cancion(parcel)
        }

        override fun newArray(size: Int): Array<Cancion?> {
            return arrayOfNulls(size)
        }
    }
}
