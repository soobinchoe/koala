package com.traydcorp.koala.dataModel

import java.util.*


data class HomeDetail (
    var index : Int,
    var category : String,
    var characterName : String? = null,
    var characterInfo : String? = null,
    var characterImgHome : Int,
    var characterImgChat : Int?,
    var characterImgProfile : Int?,
    var isPlaying : Boolean = false
        )

data class NewsDetail (
    var id : Int? = 0, // 뉴스 id
    var topic_index : String? = null, // 뉴스 index
    var next : Boolean? = false,
    var content : String? = null,
    var category : String? = null,
    var audio_url : String? = null,
    var created : String? = null,
    var modified : String? = null,
    var isFirst : Boolean? = false,
    var searchWord: String? = null,
    var isNoResult: Boolean? = false
)