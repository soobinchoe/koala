package com.traydcorp.koala.ui.search

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.traydcorp.koala.databinding.RecyclerSearchRecentBinding

class SearchAdapter(private val recentSearchList: ArrayList<String>) : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val binding = RecyclerSearchRecentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        recentSearchList[position].let { holder.bindRecentSearchList(it) }
    }

    override fun getItemCount(): Int {
        return recentSearchList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    interface OnItemClickListener {
        fun onClick(v: View, position: Int, search: String?)
    }

    private lateinit var itemClickListener : OnItemClickListener

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    inner class SearchViewHolder(private val binding: RecyclerSearchRecentBinding) : RecyclerView.ViewHolder(binding.root) {
        // 최근 검색어 bind
        fun bindRecentSearchList(recentSearch: String) {
            binding.searchWord.text = recentSearch
            binding.deleteBtn.setOnClickListener { // 삭제 클릭
                itemClickListener.onClick(it, adapterPosition, recentSearch)
            }

            binding.searchWord.setOnClickListener { // 검색어 클릭
                itemClickListener.onClick(it, adapterPosition, null)
            }
        }

    }
}