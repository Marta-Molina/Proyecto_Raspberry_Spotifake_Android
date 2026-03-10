package com.example.appmusica.di

import com.example.appmusica.data.repository.AuthRepositoryImpl
import com.example.appmusica.data.repository.CancionRepositoryImpl
import com.example.appmusica.data.repository.PlaylistRepositoryImpl
import com.example.appmusica.domain.repository.AuthRepository
import com.example.appmusica.domain.repository.CancionRepository
import com.example.appmusica.domain.repository.PlaylistRepository
import com.example.appmusica.domain.repository.ArtistaRepository
import com.example.appmusica.data.repository.ArtistaRepositoryImpl
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
}
