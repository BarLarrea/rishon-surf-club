//package com.example.surf_club_android.view.fragments//package com.example.surf_club_android.view.fragments
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Toast
//import androidx.lifecycle.ViewModelProvider
//import com.bumptech.glide.Glide
//import com.example.surf_club_android.R
//import com.example.surf_club_android.base.LoadingFragment
//import com.example.surf_club_android.databinding.FragmentUserProfileBinding
//import com.example.surf_club_android.viewmodel.UserProfileViewModel
//import com.google.firebase.auth.FirebaseAuth
//
//class UserProfileFragment : LoadingFragment() {
//    private var _binding: FragmentUserProfileBinding? = null
//    private val binding get() = _binding!!
//    private lateinit var viewModel: UserProfileViewModel
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
//        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        viewModel = ViewModelProvider(this)[UserProfileViewModel::class.java]
//
//        FirebaseAuth.getInstance().currentUser?.let { firebaseUser ->
//            viewModel.loadUser(firebaseUser.uid)
//        }
//
//        setupObservers()
//
//        binding.editProfileButton.setOnClickListener {
//            // TODO: Navigate to edit profile screen
//            // For example:
//            // findNavController().navigate(R.id.action_userProfileFragment_to_editProfileFragment)
//        }
//    }
//
//    private fun setupObservers() {
//        viewModel.user.observe(viewLifecycleOwner) { user ->
//            user?.let {
//                binding.nameTextView.text = getString(R.string.full_name_format, it.firstName, it.lastName)
//                binding.emailValueTextView.text = it.email
//                binding.teamValueTextView.text = it.role
//                binding.aboutMeValueTextView.text = it.aboutMe ?: getString(R.string.no_info_provided)
//
//                Glide.with(this)
//                    .load(it.profileImageUrl)
//                    .placeholder(R.drawable.ic_profile_placeholder)
//                    .into(binding.profileImage)
//            }
//        }
//
//        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
//            if (isLoading) showLoading() else hideLoading()
//        }
//
//        viewModel.error.observe(viewLifecycleOwner) { error ->
//            error?.let {
//                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
//            }
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}