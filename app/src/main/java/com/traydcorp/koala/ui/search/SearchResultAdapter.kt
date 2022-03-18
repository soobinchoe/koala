package com.traydcorp.koala.ui.search

import android.content.Context
import android.os.Build
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.marginTop
import androidx.recyclerview.widget.RecyclerView
import com.traydcorp.koala.R
import com.traydcorp.koala.dataModel.HomeDetail
import com.traydcorp.koala.dataModel.NewsDetail
import com.traydcorp.koala.databinding.RecyclerDetailBinding
import com.traydcorp.koala.databinding.RecyclerSearchResultBinding
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class SearchResultAdapter(private val newsDetail: ArrayList<NewsDetail>, private val homeDetailList: List<HomeDetail>) : RecyclerView.Adapter<SearchResultAdapter.SearchResultViewHolder>() {

    private lateinit var context : Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val binding = RecyclerSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        newsDetail[position].let {  holder.bindSearchResult(it) }
    }

    override fun getItemCount(): Int {
        return newsDetail.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        context = recyclerView.context
    }

    interface OnItemClickListener {
        fun onClick(v: View, position: Int, category: String?, lastId: Int?)
    }

    private lateinit var itemClickListener : OnItemClickListener

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    inner class SearchResultViewHolder(private val bind : RecyclerSearchResultBinding) : RecyclerView.ViewHolder(bind.root) {

        @RequiresApi(Build.VERSION_CODES.O)
        fun bindSearchResult(newsDetail: NewsDetail) {
            // 날짜 형식 변환 formatter
            val formatterFrom = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ", Locale.getDefault())
            formatterFrom.timeZone = TimeZone.getTimeZone("UTC")
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 EEE")
            val defaultFormatter = DateTimeFormatter.ofPattern("a kk:mm")
            val currentDate = current.format(formatter)
            val defaultDate = current.format(defaultFormatter)

            if (newsDetail.searchWord == null) { // 검색 결과
                bind.searchWordCont.visibility = View.GONE
                for (i in homeDetailList.indices) {
                    if (newsDetail.category == homeDetailList[i].category) {
                        bind.character.text = homeDetailList[i].characterName
                        bind.characterImg.setImageResource(homeDetailList[i].characterImgChat!!)
                    }
                }
                bind.newsText.text = newsDetail.content
                bind.searchResultTime.text = defaultDate
                if (newsDetail.next == true) {
                    bind.loadMoreBtn.visibility = View.VISIBLE
                    bind.loadMoreBtn.setOnClickListener {
                        itemClickListener.onClick(it, adapterPosition, null, newsDetail.id)
                    }
                }
            } else {
                if (newsDetail.isNoResult == false) { // 검색어
                    if (newsDetail.isFirst == true) {
                        bind.defaultChat.visibility = View.VISIBLE
                        bind.currentDate.text = currentDate + "요일"
                        bind.defaltChatTime.text = defaultDate
                    }
                    bind.searchWordCont.visibility = View.VISIBLE
                    bind.searchResultCont.visibility = View.GONE
                    bind.searchWord.text = newsDetail.searchWord
                    bind.searchWordTime.text = defaultDate
                } else { // 검색 결과가 없을 때
                    bind.defaultChat.visibility = View.VISIBLE
                    bind.dateCont.visibility = View.GONE
                    val param = bind.initialMsg.layoutParams as ViewGroup.MarginLayoutParams
                    param.topMargin = 0
                    bind.initialMsg.layoutParams = param
                    bind.searchResultCont.visibility = View.GONE

                    bind.defaltChatTime.text = defaultDate
                    val text = String.format(context.resources.getString(R.string.search_no_result), newsDetail.searchWord)
                    bind.msgText.setText(text, TextView.BufferType.SPANNABLE)
                    val word: Spannable = bind.msgText.text as Spannable
                    word.setSpan(
                        ForegroundColorSpan(context.resources.getColor(R.color.light_purple)),
                        1,
                        newsDetail.searchWord!!.length+1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                }
            }

            bind.characterImg.setOnClickListener {
                itemClickListener.onClick(it, adapterPosition, newsDetail.category, null)
            }
        }

    }
}