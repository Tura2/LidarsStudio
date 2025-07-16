package com.ot.lidarsstudio

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ot.lidarsstudio.databinding.FragmentMenuBottomSheetBinding

class MenuBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentMenuBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBottomSheetBinding.inflate(inflater, container, false)

        binding.buttonHome.setOnClickListener {
            startActivity(Intent(requireContext(), HomeActivity::class.java))
            dismiss()
        }

        binding.buttonProfile.setOnClickListener {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
            dismiss()
        }

        binding.buttonGallery.setOnClickListener {
            startActivity(Intent(requireContext(), GalleryActivity::class.java))
            dismiss()
        }

        binding.buttonPriceList.setOnClickListener {
            startActivity(Intent(requireContext(), PriceListActivity::class.java))
            dismiss()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
