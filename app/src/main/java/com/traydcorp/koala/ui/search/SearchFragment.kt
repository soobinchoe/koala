package com.traydcorp.koala.ui.search

import android.Manifest
import android.R.attr
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.traydcorp.koala.R
import com.traydcorp.koala.databinding.FragmentSearchBinding
import com.traydcorp.koala.utils.SharedPreference
import androidx.core.app.ActivityCompat

import android.os.Build
import com.traydcorp.koala.MainActivity

import android.speech.SpeechRecognizer

import android.R.attr.button
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener

import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

import androidx.core.content.ContextCompat.getSystemService





class SearchFragment : Fragment() {

    private var viewBinding : FragmentSearchBinding? = null
    private val bind get() = viewBinding!!

    private val sharedPreferences = SharedPreference()

    private var searchWord : String? = null
    private var recentSearchList : ArrayList<String>? = null
    private lateinit var adapter : SearchAdapter

    private lateinit var intent : Intent
    private var speechRecognizer: SpeechRecognizer? = null
    private var recognitionListener: RecognitionListener? = null

    private lateinit var callback: OnBackPressedCallback


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    // 검색 화면에서 뒤로가기 처리
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (bind.sttCont.visibility == View.VISIBLE) { // 음성인식 화면일 때
                    bind.sttCont.visibility = View.GONE
                    setSearchView()
                } else {
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentSearchBinding.inflate(inflater, container, false)

        // 최근 검색어
        getRecentSearch()
        bind.searchBar.isFocusable = true
        bind.searchBar.requestFocus()
        bind.searchBar.isIconified = false
        bind.searchBar.requestFocusFromTouch()

        // 검색창 listener
        bind.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                // 검색 버튼 눌렀을 때
                if (query != null) {
                    searchWord = query
                    getSearch()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }


        })

        // 음성 인식 버튼
        bind.micBtn.setOnClickListener(View.OnClickListener {

            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                // 음성 인식 권한
                requestPermission()
            } else {
                startListening()
            }

            // view 전환
            bind.noSearchHistory.visibility = View.GONE
            bind.recentSearch.visibility = View.GONE
            bind.sttCont.visibility = View.VISIBLE
            bind.searchBarCont.visibility = View.GONE

        })



        // 뒤로가기 버튼
        bind.backBtn.setOnClickListener {
            if (requireActivity().supportFragmentManager.backStackEntryCount != 0){
                requireActivity().supportFragmentManager.popBackStack()
            }
        }

        // 최근 검색어 전체 삭제
        bind.deleteAll.setOnClickListener {
            recentSearchList?.clear()
            sharedPreferences.sharedClear(requireContext(), "recentSearch")
            bind.recentSearch.visibility = View.GONE
            bind.noSearchHistory.visibility = View.VISIBLE
        }

        return bind.root
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

                getSearch()
            }

            override fun onPartialResults(partialResults: Bundle?) { }
            override fun onEvent(eventType: Int, params: Bundle?) { } }

    }

    // 최근 검색어 recyclerView
    private fun recentSearchRecyclerView(it: ArrayList<String>) {
        adapter = SearchAdapter(it)
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        bind.recentSearchRcy.adapter = adapter
        bind.recentSearchRcy.layoutManager = layoutManager

        // 최근 검색어 click
        adapter.setItemClickListener(object : SearchAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int, search: String?) {
                if (search != null) { // 삭제 아이콘 클릭
                    recentSearchList!!.remove(search)
                    adapter.notifyItemRemoved(position)
                } else { // 검색어 클릭
                    searchWord = it[position]


                    getSearch()
                }
                // 최근 검색어 업데이트
                sharedPreferences.setRecentSearch("recentSearch", recentSearchList!!, requireContext())

                // 최근 검색어가 없으면 view 전환
                if (recentSearchList!!.isEmpty()) {
                    bind.recentSearch.visibility = View.GONE
                    bind.noSearchHistory.visibility = View.VISIBLE
                }
            }
        })
    }

    // 최근 검색어 저장
    fun setRecentSearch(searchWord : String) {
        if (recentSearchList == null) {
            val recentSearch = ArrayList<String>()
            recentSearch.add(searchWord)
            recentSearchList = recentSearch
        } else {
            if (recentSearchList!!.contains(searchWord)) recentSearchList!!.remove(searchWord)
            if (recentSearchList!!.lastIndex == 15) { // 15개까지 저장
                recentSearchList!!.removeLast()
            }
            recentSearchList!!.add(0, searchWord)

        }
        bind.recentSearchRcy.removeAllViewsInLayout()
        recentSearchRecyclerView(recentSearchList!!)

        sharedPreferences.setRecentSearch("recentSearch", recentSearchList!!, requireContext())
        getRecentSearch()
    }

    // 검색 화면으로 이동
    private fun getSearch() {
        bind.noSearchHistory.visibility = View.GONE
        bind.recentSearch.visibility = View.VISIBLE

        val bundle = Bundle()
        bundle.putString("searchWord", searchWord)
        val searchResultFragment = SearchResultFragment()
        searchResultFragment.arguments = bundle
        requireActivity().supportFragmentManager.beginTransaction().addToBackStack(null)
            .add(R.id.homeView, searchResultFragment, "searchResult").commit()

        Handler(Looper.getMainLooper()).postDelayed({
            setSearchView()
        }, 2000)
    }

    // 검색 초기 화면으로
    fun setSearchView() {
        bind.searchBar.setQuery("", false)
        bind.searchBar.clearFocus()
        bind.searchBarCont.visibility = View.VISIBLE
        getRecentSearch()
        searchWord?.let { setRecentSearch(it) }
    }

    // 최근 검색어 가져오기
    private fun getRecentSearch() {
        if (sharedPreferences.getRecentSearch(requireContext(), "recentSearch")?.isNullOrEmpty() == false){
            recentSearchList = sharedPreferences.getRecentSearch(requireContext(), "recentSearch")
            recentSearchList?.let { recentSearchRecyclerView(it) }
            bind.recentSearch.visibility = View.VISIBLE
        } else {
            bind.recentSearch.visibility = View.GONE
            bind.noSearchHistory.visibility = View.VISIBLE
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (speechRecognizer != null) {
            speechRecognizer!!.destroy()
        }
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