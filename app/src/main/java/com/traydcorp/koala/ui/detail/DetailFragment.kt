package com.traydcorp.koala.ui.detail

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.traydcorp.koala.R
import com.traydcorp.koala.data.homeList
import com.traydcorp.koala.dataModel.NewsDetail
import com.traydcorp.koala.databinding.FragmentDetailBinding
import com.traydcorp.koala.databinding.FragmentHomeBinding
import com.traydcorp.koala.ui.home.HomeActivity
import com.traydcorp.koala.ui.home.HomeAdapter
import com.traydcorp.newdio.utils.retofitService.RetrofitService
import com.traydcorp.newdio.utils.retrofitAPI.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.util.*
import kotlin.collections.ArrayList


class DetailFragment : Fragment() {

    private var viewBinding : FragmentDetailBinding? = null
    private val bind get() = viewBinding!!

    private lateinit var retrofit: Retrofit
    private lateinit var supplementService: RetrofitService

    private var lastId = 0
    private var lastPosition = 0
    private lateinit var reporter : String
    private var category : String? = null
    private var newsList = ArrayList<NewsDetail>()
    private lateinit var adapter : DetailAdapter
    private lateinit var callback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(RetrofitService::class.java)

    }

    // 로딩시 뒤로가기 막기
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentDetailBinding.inflate(inflater, container, false)

        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        // home에서 넘어온 category
        category = arguments?.getString("category").toString()
        var character = arguments?.getString("character")
        if (category == "playAll") { // 실시간 뉴스
            character = getString(R.string.home_live)
            category = null
        }

        // 뉴스 리스트 api 요청
        getNewsList(supplementService.getNewsList(category, null), false)

        bind.category.text = character

        // 뒤로가기
        bind.backBtn.setOnClickListener {
            if (requireActivity().supportFragmentManager.backStackEntryCount != 0){
                requireActivity().supportFragmentManager.popBackStack()
            }
        }

        return bind.root
    }


    // 뉴스 리스트 api 요청
    private fun getNewsList(service: Call<List<NewsDetail>>, loadMore: Boolean) {

        Handler(Looper.getMainLooper()).postDelayed({
            service.enqueue(object : Callback<List<NewsDetail>> {
                override fun onResponse(
                    call: Call<List<NewsDetail>>,
                    response: Response<List<NewsDetail>>
                ) {

                    if (response.code() == 200){
                        val result = response.body() as ArrayList<NewsDetail>
                        newsList = result

                        if (!loadMore) { // loadMore 아니면 recyclerView로 연결
                            detailListRecyclerView(result)
                        } else {
                            // reload list에 추가 후 adapter에 setData
                            for (i in result.indices) {
                                newsList.add(result[i])
                            }
                            adapter.setData(result)
                        }

                        if (newsList.isNotEmpty()){ // lastId 업데이트
                            lastId = newsList[newsList.lastIndex].id!!
                        }
                    } else {
                        // response 코드가 200이 아닐 때
                    }
                }

                override fun onFailure(call: Call<List<NewsDetail>>, t: Throwable) {
                }

            })
        }, 1000)
    }

    // 상세보기 recyclerView
    private fun detailListRecyclerView(result: List<NewsDetail>) {
        // 역순으로 그리기
        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, true)
        var homeDetailList = (activity as HomeActivity?)?.homeDetailList

        if (homeDetailList == null) {
            homeDetailList = homeList(requireContext().resources)
        }
        adapter = DetailAdapter(result as java.util.ArrayList<NewsDetail>, homeDetailList, category)
        bind.detailRcy.layoutManager = layoutManager
        bind.detailRcy.adapter = adapter
        bind.detailRcy.scrollToPosition(0)

        callback.remove()
        activity?.window?.clearFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        // 맨 위 기사로 가면 기사 더보기
        bind.detailRcy.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                lastPosition =
                    layoutManager.findLastCompletelyVisibleItemPosition()

                if (!bind.detailRcy.canScrollVertically(-1)){
                    getNewsList(supplementService.getNewsList(category, lastId), true)
                }
            }
        })

        // 아이템 이미지 클릭 후 기자 프로필 보기
        adapter.setItemClickListener(object : DetailAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int, category: String) {
                val bundle = Bundle()
                val detailBottomFragment = DetailBottomFragment()

                bundle.putString("category", category)
                detailBottomFragment.arguments = bundle
                detailBottomFragment.show((context as AppCompatActivity).supportFragmentManager, "detailBottom")
            }

        })

    }


}