package com.traydcorp.koala.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken

class SharedPreference {
    private fun getPreferences(context: Context): SharedPreferences? {
        return context.getSharedPreferences("Shared", Context.MODE_PRIVATE)
    }

    fun setShared(key: String, value: String, context: Context) {

        val sharedPreferences = getPreferences(context)
        val editor = sharedPreferences!!.edit()

        editor.putString(key, value)
        editor.apply()
    }


    fun getShared(context: Context?, key: String?): String? {
        val prefs = getPreferences(context!!)
        return prefs!!.getString(key, null)
    }

    fun sharedClear(context: Context,key:String) {
        val sharedPreferences = getPreferences(context)
        val editor = sharedPreferences!!.edit()
        editor.remove(key)
        editor.commit()
    }


    fun setRecentSearch(key: String, value: ArrayList<String>, context: Context) {
        val sharedPreference = getPreferences(context)
        val editor = sharedPreference!!.edit()
        val gson = Gson()
        val json = gson.toJson(value)

        editor.putString(key, json)
        editor.apply()
    }

    fun getRecentSearch(context: Context, key: String?) : ArrayList<String>? {
        val prefs = getPreferences(context)
        if (prefs?.contains(key) == true) {
            val gson = Gson()
            val json = prefs.getString(key, "")
            try {
                val typeToken = object : TypeToken<ArrayList<String>>() {}.type
                return gson.fromJson(json, typeToken)
            } catch (e: JsonParseException) {
                e.printStackTrace()
            }
        }
        return null
    }

}