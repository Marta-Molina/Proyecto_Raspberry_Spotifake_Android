package com.example.appmusica.data.datasource

import com.example.appmusica.domain.model.Cancion
import javax.inject.Inject

class FakeCancionesDataSource @Inject constructor() {
    val canciones: MutableList<Cancion> = mutableListOf(
        Cancion(
            1,
            "Shape of You",
            "Ed Sheeran",
            "Divide",
            "https://cdn-images.dzcdn.net/images/cover/107c2b43f10c249077c1f7618563bb63/1900x1900-000000-81-0-0.jpg",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
        ),
        Cancion(
            2,
            "Blinding Lights",
            "The Weeknd",
            "After Hours",
            "https://cdn-images.dzcdn.net/images/cover/cf22674710be326f668dfb27d5af9576/1900x1900-000000-81-0-0.jpg",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
        ),
        Cancion(
            3,
            "Bad Guy",
            "Billie Eilish",
            "When We All Fall Asleep, Where Do We Go?",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ84nWhIG9Sj0PDeXUmo0CPvVmUUU0tXwMwvk2bJTKeE6dWUWhE-SDOfKirBoXjbdkoOWc&usqp=CAU",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"
        )
    )
}
