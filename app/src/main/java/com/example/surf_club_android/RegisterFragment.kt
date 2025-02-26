package com.example.surf_club_android

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.surf_club_android.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRoleDropdown()
        setupClickListeners()
    }

    private fun setupRoleDropdown() {
        val roles = arrayOf("Student", "Teacher", "Parent", "Administrator")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, roles)
        binding.actvRole.setAdapter(adapter)
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        // Assuming you have a "Back to Login" button or text in your register layout
//        binding.tvBackToLogin.setOnClickListener {
//            navigateToLogin()
//        }
    }

    private fun registerUser() {
        val firstName = binding.etFirstName.text.toString()
        val lastName = binding.etLastName.text.toString()
        val role = binding.actvRole.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()

        // TODO: Implement actual user registration logic
        println("Registering user:")
        println("First Name: $firstName")
        println("Last Name: $lastName")
        println("Role: $role")
        println("Email: $email")
        println("Password: $password")
    }

    private fun navigateToLogin() {
        (activity as? MainActivity)?.navigateToLogin()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}