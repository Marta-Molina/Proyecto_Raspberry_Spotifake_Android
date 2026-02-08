package com.example.appmusica.data.datasource

import com.example.appmusica.domain.model.Cancion
import javax.inject.Inject

class FakeCancionesDataSource @Inject constructor() {
    val canciones: MutableList<Cancion> = mutableListOf(
        Cancion(
            "Shape of You",
            "Ed Sheeran",
            "Divide",
            "3:53",
            "https://cdn-images.dzcdn.net/images/cover/107c2b43f10c249077c1f7618563bb63/1900x1900-000000-81-0-0.jpg"
        ),
        Cancion(
            "Blinding Lights",
            "The Weeknd",
            "After Hours",
            "3:20",
            "https://cdn-images.dzcdn.net/images/cover/cf22674710be326f668dfb27d5af9576/1900x1900-000000-81-0-0.jpg"
        ),
        Cancion(
            "Bad Guy",
            "Billie Eilish",
            "When We All Fall Asleep, Where Do We Go?",
            "3:14",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ84nWhIG9Sj0PDeXUmo0CPvVmUUU0tXwMwvk2bJTKeE6dWUWhE-SDOfKirBoXjbdkoOWc&usqp=CAU"
        ),
        Cancion(
            "Uptown Funk",
            "Mark Ronson ft. Bruno Mars",
            "Uptown Special",
            "4:30",
            "https://cdn-images.dzcdn.net/images/cover/3734366a73152d0367a83a4b09fd163f/1900x1900-000000-80-0-0.jpg"
        ),
        Cancion(
            "Rolling in the Deep",
            "Adele",
            "21",
            "3:48",
            "https://cdn-images.dzcdn.net/images/cover/d66197eb2ff199a77b8c6b9387fa1143/0x1900-000000-80-0-0.jpg"
        ),
        Cancion(
            "Someone Like You",
            "Adele",
            "21",
            "4:45",
            "https://cdn-images.dzcdn.net/images/cover/59b785f1a20017f81e300053ab34fd11/1900x1900-000000-80-0-0.jpg"
        ),
        Cancion(
            "Believer",
            "Imagine Dragons",
            "Evolve",
            "3:24",
            "https://i.scdn.co/image/ab67616d0000b2735675e83f707f1d7271e5cf8a"
        ),
        Cancion(
            "Counting Stars",
            "OneRepublic",
            "Native",
            "4:17",
            "https://i.scdn.co/image/ab67616d0000b2739e2f95ae77cf436017ada9cb"
        ),
        Cancion(
            "Can't Stop the Feeling!",
            "Justin Timberlake",
            "Trolls (Soundtrack)",
            "3:56",
            "https://m.media-amazon.com/images/I/710Toa9ka+L._UF894,1000_QL80_.jpg"
        ),
        Cancion(
            "Havana",
            "Camila Cabello ft. Young Thug",
            "Camila",
            "3:36",
            "https://i.scdn.co/image/ab67616d0000b273d93cf4d07ba50d7b32ca7c44"
        ),
        Cancion(
            "Sunflower",
            "Post Malone & Swae Lee",
            "Spider-Man: Into the Spider-Verse (Soundtrack)",
            "2:38",
            "https://pics.filmaffinity.com/Post_Malone_Swae_Lee_Sunflower_Vaideo_musical-836039064-large.jpg"
        ),
        Cancion(
            "Dance Monkey",
            "Tones and I",
            "The Kids Are Coming",
            "3:29",
            "https://cdn-images.dzcdn.net/images/cover/3d7b540eb85c84a37cd5bf53740991cb/1900x1900-000000-81-0-0.jpg"
        )
    )
}