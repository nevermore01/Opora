package com.example.opora

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.viewpager2.widget.ViewPager2

class ImageDialogFragment : DialogFragment() {

    private lateinit var imageUrls: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageUrls = arguments?.getStringArrayList(ARG_IMAGE_URLS) ?: emptyList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_image_viewer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewPager = view.findViewById<ViewPager2>(R.id.viewPager)
        viewPager.adapter = ImagePagerAdapter(requireContext(), imageUrls)
    }

    companion object {
        private const val ARG_IMAGE_URLS = "image_urls"

        fun newInstance(imageUrls: List<String>): ImageDialogFragment {
            val fragment = ImageDialogFragment()
            val args = Bundle().apply {
                putStringArrayList(ARG_IMAGE_URLS, ArrayList(imageUrls))
            }
            fragment.arguments = args
            return fragment
        }
    }
}
