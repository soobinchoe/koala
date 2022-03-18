package com.traydcorp.koala.ui.search

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.traydcorp.koala.dataModel.NewsDetail
import com.traydcorp.koala.databinding.FragmentSearchResultBinding
import com.traydcorp.koala.ui.detail.DetailBottomFragment
import com.traydcorp.koala.ui.home.HomeActivity
import com.traydcorp.koala.utils.SharedPreference
import com.traydcorp.newdio.utils.retofitService.RetrofitService
import com.traydcorp.newdio.utils.retrofitAPI.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


class SearchResultFragment : Fragment() {

    private var viewBinding : FragmentSearchResultBinding? = null
    private val bind get() = viewBinding!!

    private lateinit var retrofit: Retrofit
    private lateinit var supplementService: RetrofitService

    private var newsList = ArrayList<NewsDetail>()
    private lateinit var adapter : SearchResultAdapter

    private lateinit var searchWord : String

    private lateinit var layoutManager : LinearLayoutManager
    private lateinit var intent : Intent
    private var speechRecognizer: SpeechRecognizer? = null
    private var recognitionListener: RecognitionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(RetrofitService::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentSearchResultBinding.inflate(inflater, container, false)

        searchWord = arguments?.getString("searchWord").toString()

        // 첫 검색어
        val newsDetail = NewsDetail()
        newsDetail.isFirst = true
        newsDetail.searchWord = searchWord

        newsList.add(newsDetail)

        searchResultRecyclerView(newsList)

        getNewsList(supplementService.getSearchNews(null, searchWord, null), null)

        // 뒤로가기
        bind.backBtn.setOnClickListener {
            if (requireActivity().supportFragmentManager.backStackEntryCount != 0){
                requireActivity().supportFragmentManager.popBackStack()
            }
        }

        // 검색창 listener
        bind.chatEditBox.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                bind.chatEditBox.setQuery("", false)
                searchWord = query!!
                searchWord.let {
                    val newsDetail = NewsDetail()
                    newsDetail.searchWord = searchWord
                    newsList.add(newsDetail)
                    adapter.notifyItemInserted(newsList.size-1)
                    bind.searchResultRcy.scrollToPosition(newsList.lastIndex)
                }
                getNewsList(supplementService.getSearchNews(null, query, null), query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        bind.micBtn.setOnClickListener {
            bind.sttCont.visibility = View.VISIBLE

            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                // 음성 인식 권한
                requestPermission()
            } else {
                startListening()
            }

        }

        return bind.root
    }

    // 검색 api 요청
    private fun getNewsList(service: Call<NewsDetail>, newSearchWord : String?) {

        Handler(Looper.getMainLooper()).postDelayed({
            service.enqueue(object : Callback<NewsDetail> {
                override fun onResponse(
                    call: Call<NewsDetail>,
                    response: Response<NewsDetail>
                ) {
                    val searchFragment : SearchFragment = requireActivity().supportFragmentManager.findFragmentByTag("search") as SearchFragment
                    searchFragment.setRecentSearch(searchWord)

                    if (response.code() == 200){
                        // 검색 결과가 있을 때
                        val result = response.body()

                        newsList.add(result!!)


                        adapter.notifyItemInserted(newsList.size-1)
                        bind.searchResultRcy.scrollToPosition(newsList.lastIndex)

                    } else if (response.code() == 404) {

                        // 검색 결과가 없을 때
                        searchWord.let {
                            val newsDetail = NewsDetail()
                            newsDetail.searchWord = it
                            newsDetail.isNoResult = true
                            newsList.add(newsDetail)
                        }

                        adapter.notifyItemInserted(newsList.size-1)
                        bind.searchResultRcy.scrollToPosition(newsList.lastIndex)

                    }
                }

                override fun onFailure(call: Call<NewsDetail>, t: Throwable) {
                }

            })
        }, 1000)
    }

    // 검색 결과 recyclerView
    private fun searchResultRecyclerView(result: List<NewsDetail>) {
        layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        val homeDetailList = (activity as HomeActivity).homeDetailList

        adapter = SearchResultAdapter(result as java.util.ArrayList<NewsDetail>, homeDetailList)
        bind.searchResultRcy.layoutManager = layoutManager
        bind.searchResultRcy.adapter = adapter
        bind.searchResultRcy.scrollToPosition(0)

        adapter.setItemClickListener(object : SearchResultAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int, category: String?, lastId: Int?) {
                if (category != null) { // 기자 프로필
                    val bundle = Bundle()
                    val detailBottomFragment = DetailBottomFragment()

                    bundle.putString("category", category)
                    detailBottomFragment.arguments = bundle
                    detailBottomFragment.show((context as AppCompatActivity).supportFragmentManager, "detailBottom")
                }

                if (lastId != null) { // 더보기 버튼
                    getNewsList(supplementService.getSearchNews(null, searchWord, lastId), null)
                }
            }
        })

    }

    private val permReqLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted  ->
            if (isGranted) {
                startListening()
            } else {
                // 권한 획득 거부 시
                Toast.makeText(context, "마이크 권한을 허용해 주세요.", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        }




    // 음성 인식 listener
    private fun setListener() {
        recognitionListener = object: RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Toast.makeText(context, "듣는중", Toast.LENGTH_SHORT).show()
            }
            override fun onBeginningOfSpeech() {
            }
            override fun onRmsChanged(rmsdB: Float) { }
            override fun onBufferReceived(buffer: ByteArray?) { }
            override fun onEndOfSpeech() {
            }

            override fun onError(error: Int) {
            }

            override fun onResults(results: Bundle?) {
                val matches: ArrayList<String> = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) as ArrayList<String>

                for (i in 0 until matches.size) {
                    bind.stt.listeningText.text = matches[i]
                }

                searchWord = bind.stt.listeningText.text as String

                searchWord.let {
                    val newsDetail = NewsDetail()
                    newsDetail.searchWord = searchWord
                    newsList.add(newsDetail)
                    adapter.notifyItemInserted(newsList.size-1)
                    bind.searchResultRcy.scrollToPosition(newsList.lastIndex)
                }
                bind.sttCont.visibility = View.GONE
                getNewsList(supplementService.getSearchNews(null, searchWord, null), searchWord)
            }

            override fun onPartialResults(partialResults: Bundle?) { }
            override fun onEvent(eventType: Int, params: Bundle?) { } }
    }

    fun requestPermission() {
        permReqLauncher.launch(Manifest.permission.RECORD_AUDIO) // 음성 녹음 권한 요청
    }

    fun startListening() {
        intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, requireActivity().packageName)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")

        // 음성 인식 listener
        setListener()

        // 음성 인식 speechRecognizer
        speechRecognizer =
            SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer!!.setRecognitionListener(recognitionListener)
        Handler(Looper.getMainLooper()).postDelayed({
            speechRecognizer!!.startListening(intent)
        }, 1000)
    }

    override fun onResume() {
        super.onResume()
        if(bind.sttCont.visibility == View.VISIBLE){
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                // 음성 인식 권한
                requestPermission()
            } else {
                startListening()
            }
        }
    }

}