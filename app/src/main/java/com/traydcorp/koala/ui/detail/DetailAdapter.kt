package com.traydcorp.koala.ui.detail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.traydcorp.koala.R
import com.traydcorp.koala.dataModel.HomeDetail
import com.traydcorp.koala.dataModel.NewsDetail
import com.traydcorp.koala.databinding.RecyclerDetailBinding
import com.traydcorp.koala.ui.home.HomeActivity
import java.text.SimpleDateFormat
import java.util.*

class DetailAdapter(private val newsDetail: ArrayList<NewsDetail>, private val homeDetailList: List<HomeDetail>, val topic : String?) : RecyclerView.Adapter<DetailAdapter.DetailViewHoler>() {

    private lateinit var context : Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHoler {
        val binding = RecyclerDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DetailViewHoler(binding)
    }

    override fun onBindViewHolder(holder: DetailViewHoler, position: Int) {
        newsDetail[position].let {  holder.bindNewsDetail(it) }
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
        fun onClick(v: View, position: Int, category: String)
    }

    private lateinit var itemClickListener : OnItemClickListener

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    fun setData(newData: ArrayList<NewsDetail>) {
        newsDetail.addAll(newsDetail.size, newData)
        this.notifyItemRangeInserted(newsDetail.size-1, newData.size)
    }

    inner class DetailViewHoler(private val bind : RecyclerDetailBinding) : RecyclerView.ViewHolder(bind.root) {
        fun bindNewsDetail(newsDetail: NewsDetail) {

            var characterName : String? = null
            for (i in homeDetailList.indices) {
                if (newsDetail.category == homeDetailList[i].category) { // 해당 카테고리 캐릭터 data 가져오기
                    bind.character.text = homeDetailList[i].characterName
                    bind.characterImg.setImageResource(homeDetailList[i].characterImgChat!!)
                    characterName = homeDetailList[i].characterName
                }
            }

            var newsText = newsDetail.content

            if (topic == null){ // 실시간 뉴스일 때 시작, 종료 text 붙이기
                val array: List<String> = characterName!!.split(" ")
                val startText = String.format(context.resources.getString(R.string.detail_start), array[0], array[1])
                val endText = String.format(context.resources.getString(R.string.detail_end), array[1])
                newsText = startText + newsDetail.content + endText
            }

            bind.newsText.text = newsText

            val formatterFrom = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ", Locale.getDefault())
            val formatterTo = SimpleDateFormat("aa HH:mm", Locale.getDefault())
            formatterFrom.timeZone = TimeZone.getTimeZone("UTC")
            val date = formatterFrom.parse(newsDetail.created)

            bind.time.text = formatterTo.format(date)

            // 기자 프로필
            bind.characterImg.setOnClickListener {
                itemClickListener.onClick(it, adapterPosition, newsDetail.category!!)
            }

        }


    }
}