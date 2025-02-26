package com.example.surf_club_android

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.surf_club_android.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSignIn.setOnClickListener {
            performSignIn()
        }

        binding.tvSignUpHere.setOnClickListener {
            navigateToSignUp()
        }
    }

    private fun performSignIn() {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()

        // TODO: Implement actual sign-in logic
        println("Signing in with:")
        println("Email: $email")
        println("Password: $password")
    }

    private fun navigateToSignUp() {
        (activity as? MainActivity)?.navigateToRegister()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}