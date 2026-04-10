package com.example.appmusica.di

import com.example.appmusica.data.repository.*
import com.example.appmusica.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCancionRepository(
        impl: CancionRepositoryImpl
    ): CancionRepository

    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(
        impl: PlaylistRepositoryImpl
    ): PlaylistRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindArtistaRepository(
        impl: ArtistaRepositoryImpl
    ): ArtistaRepository

    @Binds
    @Singleton
    abstract fun bindSocialRepository(
        impl: SocialRepositoryImpl
    ): SocialRepository

    @Binds
    @Singleton
    abstract fun bindMascotaRepository(
        impl: MascotaRepositoryImpl
    ): MascotaRepository

    @Binds
    @Singleton
    abstract fun bindAlarmaRepository(
        impl: AlarmaRepositoryImpl
    ): AlarmaRepository
}
