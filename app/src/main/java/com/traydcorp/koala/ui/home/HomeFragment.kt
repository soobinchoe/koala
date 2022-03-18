package com.traydcorp.koala.ui.home

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.traydcorp.koala.R
import com.traydcorp.koala.dataModel.HomeDetail
import com.traydcorp.koala.databinding.FragmentHomeBinding
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import com.traydcorp.koala.ui.detail.DetailFragment
import com.traydcorp.koala.ui.search.SearchFragment
import kotlin.collections.ArrayList
import android.widget.ImageView
import com.traydcorp.newdio.utils.retofitService.RetrofitService
import retrofit2.Retrofit


class HomeFragment : Fragment() {

    private var viewBinding: FragmentHomeBinding? = null
    private val bind get() = viewBinding!!

    private lateinit var retrofit: Retrofit
    private lateinit var supplementService: RetrofitService

    var homeDetailList: List<HomeDetail> = ArrayList()
    private var adapter: HomeAdapter? = null
    private var layoutManager: LinearLayoutManager? = null

    private lateinit var smoothScroller: LinearSmoothScroller
    private lateinit var runnable: Runnable
    val handler = Handler(Looper.getMainLooper())
    private var count = 0
    private var category: String? = null
    var playingIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retrofit = (activity as HomeActivity).retrofit
        supplementService = (activity as HomeActivity).supplementService

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentHomeBinding.inflate(inflater, container, false)

        // home data recyclerview
        homeDetailList = (activity as HomeActivity).homeDetailList
        homeRecyclerView(homeDetailList)

        // 검색 버튼 클릭
        bind.searchBtn.setOnClickListener {
            activity?.supportFragmentManager!!.beginTransaction().addToBackStack(null)
                .add(R.id.homeView, SearchFragment(), "search").commit()
        }

        return bind.root
    }

    // recyclerView
    private fun homeRecyclerView(it: List<HomeDetail>) {
        adapter = HomeAdapter(it)

        // 자동 스크롤을 위한 smoothScroller
        layoutManager =
            object : LinearLayoutManager(context) {
                override fun smoothScrollToPosition(
                    recyclerView: RecyclerView,
                    state: RecyclerView.State,
                    position: Int
                ) {
                    smoothScroller =
                        object : LinearSmoothScroller(context) {
                            private val SPEED = 4000f
                            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                                return SPEED / displayMetrics.densityDpi
                            }
                        }
                    smoothScroller.targetPosition = position
                    startSmoothScroll(smoothScroller)
                }
            }
        layoutManager?.orientation = LinearLayoutManager.HORIZONTAL

        bind.homeRecyclerView.layoutManager = layoutManager
        bind.homeRecyclerView.adapter = adapter

        adapter!!.setItemClickListener(object : HomeAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int, playBtn: Boolean, isPlaying: Boolean) {
                val bundle = Bundle()
                for (i in homeDetailList.indices) {
                    if (position % 8 == 0) {
                        if (!playBtn) bundle.putString("category", "playAll") // 실시간 뉴스
                        category = null
                    } else if (position % 8 == homeDetailList[i].index) {
                        if (!playBtn) {
                            bundle.putString("category", homeDetailList[i].category)
                            bundle.putString("character", homeDetailList[i].characterName)
                        }
                        category = homeDetailList[i].category
                    }
                }

                if (playBtn) { // 플레이 버튼 클릭
                    if (playingIndex != position % 8) { // 현재 재생중인 인덱스가 아닐 때
                        playingIndex = position % 8
                        resetPlayBtn(null) // 플레이 버튼 리셋
                        (activity as HomeActivity).getNewsList( // 해당 카테고리 api 호출
                            supplementService.getNewsList(
                                category,
                                null
                            ), false
                        )
                        (activity as HomeActivity).category = category
                    } else { // 현재 재생중인 인덱스일 때 재생, 일시정지 처리
                        (activity as HomeActivity).playClicked()
                    }


                    autoScroll()
                } else {
                    // 상세보기로 이동
                    val detailFragment = DetailFragment()
                    detailFragment.arguments = bundle
                    activity?.supportFragmentManager!!.beginTransaction().addToBackStack(null)
                        .add(R.id.homeView, detailFragment).commit()

                    autoScroll()
                }
            }
        })

        // 리스트의 중간으로 scroll
        bind.homeRecyclerView.scrollToPosition(Integer.MAX_VALUE / 2 + 1)
        count = Integer.MAX_VALUE / 2 + 1

        // 위아래 이동 애니메이션
        Handler(Looper.getMainLooper()).postDelayed({
            setAnimation()
        }, 100)
        handler.postDelayed(setAnim, 100)

        // 자동 스크롤
        autoScroll()


        // 수동 스크롤 감지
        bind.homeRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    SCROLL_STATE_IDLE -> {
                        autoScroll()
                    } // 자동 스크롤 다시 시작
                    SCROLL_STATE_DRAGGING -> {
                        handler.removeCallbacks(runnable)
                    } // 수동 스크롤 시 자동 스크롤 멈춤
                }
            }
        })
    }

    // index 체크 후 위아래 애니메이션 적용
    @SuppressLint("ResourceType")
    fun startAnimation(view: View, position: Int) {
        if (position % 2 == 0) {
            val downAnim = ObjectAnimator.ofFloat(view, "translationY", 0F, 100F)
            downAnim.duration = 1000
            downAnim.repeatCount = ValueAnimator.INFINITE
            downAnim.repeatMode = ValueAnimator.REVERSE
            downAnim.start()
        } else {
            val upAnim = ObjectAnimator.ofFloat(view, "translationY", 100F, 0F)
            upAnim.duration = 1000
            upAnim.repeatCount = ValueAnimator.INFINITE
            upAnim.repeatMode = ValueAnimator.REVERSE
            upAnim.start()
        }
    }

    // 현재 그려진 view에 startAnimation 적용
    private fun setAnimation() {
        if (layoutManager != null) {
            val firstPosition = layoutManager!!.findFirstVisibleItemPosition()
            val lastPosition = layoutManager!!.findLastVisibleItemPosition()
            for (i in firstPosition..lastPosition + 1) {
                val itemView = layoutManager!!.findViewByPosition(i)
                if (itemView != null) {
                    startAnimation(itemView, i)
                }
            }
        }
    }

    fun resetPlayBtn(playStatus: String?) {
        if (layoutManager != null) {
            val firstPosition = layoutManager!!.findFirstVisibleItemPosition()
            val lastPosition = layoutManager!!.findLastVisibleItemPosition()
            for (i in firstPosition..lastPosition + 1) {
                Log.d("i value", i.toString())
                val itemView = layoutManager!!.findViewByPosition(i)
                if (itemView != null) {
                    val playBtn = itemView.findViewById<ImageView>(R.id.playBtnBgr)

                    if (i % 8 != playingIndex || playStatus == "pause") {
                        playBtn.setImageResource(R.drawable.ic_home_play)
                        return
                    }

                    playBtn.setImageResource(R.drawable.ic_home_stop)
                }
            }
        }
    }

    // 자동 스크롤
    private fun autoScroll() {
        val speedScroll = 0
        runnable = object : Runnable {
            override fun run() {
                if (count == adapter?.itemCount) count = 0
                if (count < adapter?.itemCount!!) {
                    bind.homeRecyclerView.smoothScrollToPosition(++count)
                    handler.postDelayed(this, speedScroll.toLong())
                }
            }
        }
        handler.postDelayed(runnable, speedScroll.toLong())
    }

    // 애니메이션 종료 시점 마다 업데이트 (새로 그려지는 view와 sync)
    private val setAnim: Runnable = object : Runnable {
        override fun run() {
            setAnimation()
            handler.postDelayed(this, 2000)
        }
    }


}