package com.traydcorp.koala.ui.home

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs


class CustomRecyclerView(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs) {

    var xBefore = 0F
    var xDiff = 0F

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {

        when (e.action) {
            MotionEvent.ACTION_DOWN -> { // 터치
                xBefore = e.x // 터치 시 x값
            }
            MotionEvent.ACTION_MOVE -> {

                xDiff = xBefore - e.x // 스크롤 시 x값 차이
                if (xDiff != 0F){
                    return true
                }
            }
        }

        super.onInterceptTouchEvent(e)
        return false
    }



}