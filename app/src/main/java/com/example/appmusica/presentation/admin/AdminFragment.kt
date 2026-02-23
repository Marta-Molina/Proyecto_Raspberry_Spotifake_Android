package com.example.appmusica.presentation.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.appmusica.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdminFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin, container, false)

        view.findViewById<Button>(R.id.btnManageSongs).setOnClickListener {
            androidx.navigation.fragment.findNavController().navigate(R.id.action_adminFragment_to_manageSongsFragment)
        }

        view.findViewById<Button>(R.id.btnManageUsers).setOnClickListener {
            androidx.navigation.fragment.findNavController().navigate(R.id.action_adminFragment_to_manageUsersFragment)
        }

        view.findViewById<Button>(R.id.btnManageGenres).setOnClickListener {
            androidx.navigation.fragment.findNavController().navigate(R.id.action_adminFragment_to_manageGenresFragment)
        }

        return view
    }
}
