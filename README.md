# 🎵 SpotiFake Android

Aplicación Android de reproducción musical desarrollada como parte del proyecto final de Desarrollo de Aplicaciones Multiplataforma (DAM).

SpotiFake es una plataforma de música en streaming compuesta por una aplicación Android, una aplicación web y una API propia, permitiendo la gestión y reproducción de contenido musical desde una infraestructura autogestionada.

---

## 📱 Características principales

### 🎶 Reproducción musical

- Reproducción de canciones por streaming.
- Pausar y reanudar reproducción.
- Avanzar a la siguiente canción.
- Volver a la canción anterior.
- Repetir canción.
- Cola de reproducción dinámica.
- Opción "Reproducir a continuación".

### 📚 Gestión de contenido

- Visualización de artistas.
- Visualización de álbumes.
- Visualización de canciones.
- Búsqueda de canciones.
- Búsqueda de artistas.
- Filtrado por géneros musicales.

### 📋 Listas de reproducción

- Crear listas de reproducción.
- Añadir canciones a listas.
- Eliminar canciones de listas.
- Gestión completa de playlists personales.

### 👤 Funciones sociales

- Buscar usuarios
- Envío de solicitudes de amistad.
- Aceptación de solicitudes de amistad.
- Compartir listas de reproducción mediante enlaces externos.
- Base preparada para futuras funcionalidades de compartición interna entre usuarios.

### ⏰ Funciones adicionales

- Creación de alarmas utilizando canciones de la plataforma.
- Edición y eliminación de alarmas.
- Temporizador de apagado automático ("Sleep Timer").
- Gestión de foto de perfil.
- Cambio de contraseña.
- Inicio y cierre de sesión.

### ⭐ Sistema Premium

Los usuarios Premium disponen de:

- Saltos ilimitados entre canciones.
- Personalización de temas visuales.
- Experiencia libre de anuncios.

---

## 🏗️ Arquitectura

La aplicación Android se comunica con una API REST desarrollada en Kotlin, encargada de gestionar la lógica de negocio y el acceso a la base de datos.

```text
Android App
      │
      ▼
   API REST
      │
      ▼
 Base de Datos
```

La API centraliza la autenticación de usuarios, la gestión de contenido musical y los permisos según el tipo de cuenta.

---

## 🛠️ Tecnologías utilizadas

### Desarrollo Android

- Kotlin
- XML
- Android SDK

### Librerías

- Retrofit
- Glide
- RecyclerView
- Material Design Components

### Backend

- API REST en Kotlin
- Docker
- MariaDB / MySQL

### Herramientas

- Android Studio
- Git
- GitHub

---

## 🚀 Instalación

### Requisitos

- Android Studio Hedgehog o superior
- JDK 17
- Android SDK
- Conexión con la API de SpotiFake

### Clonar el repositorio

```bash
git clone https://github.com/Marta-Molina/Proyecto_Raspberry_Spotifake_Android.git
```

### Abrir el proyecto

1. Abrir Android Studio.
2. Seleccionar **Open Project**.
3. Elegir la carpeta clonada.
4. Esperar a que Gradle sincronice las dependencias.

### Configuración

Antes de ejecutar la aplicación es necesario configurar la URL de la API utilizada por el proyecto.

---

## 🔮 Funcionalidades futuras

- Sistema de mascotas virtuales.
- Visualización de letras de canciones.
- Ampliación del sistema de personalización.
- Mejoras en las recomendaciones musicales.

---

## 👥 Equipo de desarrollo

### Aplicación Android

Marta Molina Escalona

### API

Rubén Bailén Castillo

### Aplicación Web

Beatriz Cobo García

---

## 📄 Licencia

Proyecto desarrollado con fines educativos como Trabajo Final de Ciclo Formativo de Desarrollo de Aplicaciones Multiplataforma (DAM).
