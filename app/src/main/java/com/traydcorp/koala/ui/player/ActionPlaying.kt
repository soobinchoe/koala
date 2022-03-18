package com.traydcorp.koala.ui.player

interface ActionPlaying {
    fun nextClicked () {}
    fun playClicked () {}
    fun playPrepared (play : Boolean) {}
    fun stopClicked () {}
}