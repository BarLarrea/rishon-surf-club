package com.example.surf_club_android

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.surf_club_android.databinding.FragmentLoginBinding
import com.example.surf_club_android.model.Model

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
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
            (activity as? AuthActivity)?.navigateToRegister()
        }
    }

    private fun performSignIn() {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()

        if (email.isNotBlank() && password.isNotBlank()) {
            // Call Model.shared.signIn which takes a callback with FirebaseUser? and error message.
            Model.shared.signIn(email, password) { firebaseUser, error ->
                activity?.runOnUiThread {
                    if (firebaseUser != null) {
                        (activity as? AuthActivity)?.navigateToMainActivity()
                    } else {
                        Toast.makeText(requireContext(), error ?: "Sign in failed", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
