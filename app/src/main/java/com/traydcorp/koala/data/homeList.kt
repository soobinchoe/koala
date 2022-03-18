package com.traydcorp.koala.data

import android.content.res.Resources
import com.traydcorp.koala.R
import com.traydcorp.koala.dataModel.HomeDetail

fun homeList(resources: Resources): List<HomeDetail> {
    return listOf(
        HomeDetail(
            index = 0,
            category = resources.getString(R.string.home_playAll),
            characterName = resources.getString(R.string.home_live),
            characterInfo = null,
            characterImgHome = R.drawable.ic_home_news,
            characterImgChat = null,
            characterImgProfile = null
        ),
        HomeDetail(
            index = 1,
            category = resources.getString(R.string.category_economy),
            characterName = resources.getString(R.string.character_koala),
            characterInfo = resources.getString(R.string.info_koala),
            characterImgHome = R.drawable.ic_home_koala,
            characterImgChat = R.drawable.ic_general_koala_chatting,
            characterImgProfile = R.drawable.ic_general_koala_profile
        ),
        HomeDetail(
            index = 2,
            category = resources.getString(R.string.category_society),
            characterName = resources.getString(R.string.character_bambi),
            characterInfo = resources.getString(R.string.info_bambi),
            characterImgHome = R.drawable.ic_home_bambi,
            characterImgChat = R.drawable.ic_general_bambi_chatting,
            characterImgProfile = R.drawable.ic_general_bambi_profile
        ),
        HomeDetail(
            index = 3,
            category = resources.getString(R.string.category_politic),
            characterName = resources.getString(R.string.character_gomi),
            characterInfo = resources.getString(R.string.info_gomi),
            characterImgHome = R.drawable.ic_home_gomi,
            characterImgChat = R.drawable.ic_general_gomi_chatting,
            characterImgProfile = R.drawable.ic_general_gomi_profile
        ),
        HomeDetail(
            index = 4,
            category = resources.getString(R.string.category_culture),
            characterName = resources.getString(R.string.character_neogul),
            characterInfo = resources.getString(R.string.info_neogul),
            characterImgHome = R.drawable.ic_home_neogul,
            characterImgChat = R.drawable.ic_general_neogul_chatting,
            characterImgProfile = R.drawable.ic_general_neogul_profile
        ),
        HomeDetail(
            index = 5,
            category = resources.getString(R.string.category_IT),
            characterName = resources.getString(R.string.character_tory),
            characterInfo = resources.getString(R.string.info_tory),
            characterImgHome = R.drawable.ic_home_tory,
            characterImgChat = R.drawable.ic_general_tory_chatting,
            characterImgProfile = R.drawable.ic_general_tory_profile
        ),
        HomeDetail(
            index = 6,
            category = resources.getString(R.string.category_world),
            characterName = resources.getString(R.string.character_mocha),
            characterInfo = resources.getString(R.string.info_mocha),
            characterImgHome = R.drawable.ic_home_mocha,
            characterImgChat = R.drawable.ic_general_mocha_chatting,
            characterImgProfile = R.drawable.ic_general_mocha_profile
        ),
        HomeDetail(
            index = 7,
            category = resources.getString(R.string.category_sports),
            characterName = resources.getString(R.string.character_hosu),
            characterInfo = resources.getString(R.string.info_hosu),
            characterImgHome = R.drawable.ic_home_hosu,
            characterImgChat = R.drawable.ic_general_hosu_chatting,
            characterImgProfile = R.drawable.ic_general_hosu_profile
        )
    )
}