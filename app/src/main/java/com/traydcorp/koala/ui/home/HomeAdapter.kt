package com.traydcorp.koala.ui.home

import android.animation.AnimatorInflater
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.traydcorp.koala.R
import com.traydcorp.koala.dataModel.HomeDetail
import com.traydcorp.koala.databinding.RecyclerHomeBinding


class HomeAdapter(private var homeDetail: List<HomeDetail>) : RecyclerView.Adapter<HomeAdapter.HomeViewHolder>() {

    private lateinit var context : Context



    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val binding = RecyclerHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HomeViewHolder(binding)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        context = recyclerView.context
    }

    interface OnItemClickListener {
        fun onClick(v: View, position: Int, playBtn: Boolean, isPlaying: Boolean)
    }

    private lateinit var itemClickListener : OnItemClickListener

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    // 무한 스크롤
    private val actualItemCount get() = homeDetail.size

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        homeDetail[position % actualItemCount].let(holder::bindHomeDetail)
    }

    override fun getItemCount(): Int {
        return if (homeDetail.isEmpty()) 0 else Integer.MAX_VALUE // item count를 max value로
    }

    var playingIndex = -1

    inner class HomeViewHolder(private val bind : RecyclerHomeBinding) : RecyclerView.ViewHolder(bind.root) {

        @RequiresApi(Build.VERSION_CODES.M)
        @SuppressLint("ResourceType", "ClickableViewAccessibility")
        fun bindHomeDetail(homeDetail : HomeDetail) {
            if (homeDetail.index != 0) {
                bind.playAllImgCont.visibility = View.GONE
                val characterName = homeDetail.characterName
                val reporter = characterName?.replaceFirst(" ", "\n")
                bind.category.text = reporter
                val colorList: ColorStateList = when (homeDetail.index) {
                    1 -> ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_koala))
                    2 -> ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_bambi))
                    3 -> ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_gomi))
                    4 -> ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_neogul))
                    5 -> ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_tory))
                    6 -> ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_mocha))
                    7 -> ColorStateList.valueOf(ContextCompat.getColor(context, R.color.color_hosu))
                    else -> { return}
                }
                bind.recyclerCont.backgroundTintList = colorList
                bind.playBtn.backgroundTintList = colorList
                bind.category.setTextColor(colorList)
                bind.characterImg.setImageResource(homeDetail.characterImgHome)
            }

            // 재생중인 방 플레이 표시
            if (homeDetail.index == playingIndex) {
                bind.playBtnBgr.setImageResource(R.drawable.ic_home_stop)
            }

            // 애니메이션 시작 위치 설정
            if (homeDetail.index % 2 != 0) {
                val upAnim = ObjectAnimator.ofFloat(bind.recyclerHome, "translationY", 100F, 100F)
                upAnim.duration = 2000
                upAnim.start()
            }

            // 플레이 버튼 touch area 설정
            val delegateArea = Rect()
            bind.playBtn.getHitRect(delegateArea)
            delegateArea.top -= 1500
            delegateArea.bottom += 1500
            delegateArea.left -= 1500
            delegateArea.right += 1500
            val expandedArea = TouchDelegate(delegateArea, bind.playBtn)
            if (View::class.java.isInstance(bind.playBtn.parent)) {
                (bind.playBtn.parent as View).touchDelegate = expandedArea
            }


            // 플레이 버튼
            bind.playBtn.setOnClickListener {
                Log.d("playBtn", "call")
                homeDetail.isPlaying = !homeDetail.isPlaying
                itemClickListener.onClick(it, adapterPosition, true, homeDetail.isPlaying)
                if (homeDetail.isPlaying) {
                    playingIndex = homeDetail.index
                    bind.playBtnBgr.setImageResource(R.drawable.ic_home_stop)
                } else {
                    playingIndex = -1
                    bind.playBtnBgr.setImageResource(R.drawable.ic_home_play)
                }

            }


            // 방 클릭
            bind.characterImg.setOnClickListener {
                Log.d("characterImg", "call")
                itemClickListener.onClick(it, adapterPosition, false, homeDetail.isPlaying)
            }

            bind.playAllImgCont.setOnClickListener {
                itemClickListener.onClick(it, adapterPosition, false, homeDetail.isPlaying)
            }

        }
    }


}