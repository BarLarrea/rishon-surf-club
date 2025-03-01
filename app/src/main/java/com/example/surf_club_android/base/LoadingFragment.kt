package com.example.surf_club_android.base

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.example.surf_club_android.R

abstract class LoadingFragment : Fragment() {

    private lateinit var loadingLayout: FrameLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingLayout = view.findViewById(R.id.loadingLayout)
    }

    protected fun showLoading() {
        loadingLayout.visibility = View.VISIBLE
    }

    protected fun hideLoading() {
        loadingLayout.visibility = View.GONE
    }
}