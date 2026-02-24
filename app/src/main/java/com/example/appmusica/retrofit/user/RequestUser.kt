package com.example.appmusica.retrofit.user

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class RequestUser(
    @SerializedName("idUser")
    @Expose
    var idUser : String,

    @SerializedName("username")
    @Expose
    var username : String?,

    @SerializedName("correo")
    @Expose
    val correo : String?,

    @SerializedName("password")
    @Expose
    var password : String?,

    @SerializedName("admin")
    @Expose
    var admin : Boolean = false,

    @SerializedName("premium")
    @Expose
    var premium : Boolean = false,

    @SerializedName("token")
    @Expose
    var token : String,

    @SerializedName("urlImage")
    @Expose
    var urlImagen : String? = null
)