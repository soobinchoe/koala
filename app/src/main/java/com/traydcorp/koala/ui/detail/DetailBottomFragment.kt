package com.traydcorp.koala.ui.detail

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.traydcorp.koala.R
import com.traydcorp.koala.databinding.FragmentDetailBottomBinding
import com.traydcorp.koala.ui.home.HomeActivity


class DetailBottomFragment : BottomSheetDialogFragment() {

    private var viewBinding : FragmentDetailBottomBinding? = null
    private val bind get() = viewBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(
            STYLE_NORMAL,
            R.style.TransparentBottomSheetDialogFragment
        )

    }

    override fun onStart() {
        super.onStart()

        if (dialog != null) {
            val bottomSheet: View = dialog!!.findViewById(R.id.design_bottom_sheet)
            bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }

        val view = view
        view!!.post{
            val parent = view.parent as View
            val params = parent.layoutParams as CoordinatorLayout.LayoutParams
            val behavior = params.behavior
            val bottomSheetBehavior = behavior as BottomSheetBehavior<*>?
            bottomSheetBehavior!!.peekHeight = (view.measuredHeight * 0.38).toInt()
            bottomSheetBehavior.isFitToContents = false
            bottomSheetBehavior.expandedOffset = (view.measuredHeight * 0.62).toInt()
            parent.setBackgroundColor(Color.TRANSPARENT)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentDetailBottomBinding.inflate(inflater, container, false)

        val homeDetailList = (activity as HomeActivity).homeDetailList

        for (i in homeDetailList.indices) {
            if (arguments?.getString("category") == homeDetailList[i].category) {
                bind.characterImg.setImageResource(homeDetailList[i].characterImgProfile!!)
                bind.characterInfo.text = homeDetailList[i].characterInfo
                bind.characterName.text = homeDetailList[i].characterName
            }
        }


        return bind.root
    }


}