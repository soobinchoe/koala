package com.traydcorp.koala.ui.player

import android.app.Service
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.traydcorp.koala.ui.home.HomeActivity.Companion.ACTION_DELETE
import com.traydcorp.koala.ui.home.HomeActivity.Companion.ACTION_PLAY
import java.io.IOException


class PlayerService : Service(), MediaPlayer.OnPreparedListener,
    AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnErrorListener {

    var actionPlaying : ActionPlaying? = null
    lateinit var audioManager : AudioManager
    private lateinit var mediaData : String

    var mediaPlayer : MediaPlayer? = null
    private var resumePosition = 0
    val focusLock = Any()
    lateinit var focusRequest : AudioFocusRequest

    var playbackDelayed = false
    var playbackNowAuthorized = false
    var resumeOnFocusGain = false
    var focusGained = false
    var isPlayed = false

    private val iBinder: IBinder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder {
        return iBinder
    }

    inner class LocalBinder : Binder() {
        val service: PlayerService
            get() = this@PlayerService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val actionName = intent?.getStringExtra("actionName")

        if (actionName != null) {
            when (actionName) {
                ACTION_PLAY -> actionPlaying?.playClicked()
                ACTION_DELETE -> actionPlaying?.stopClicked()
            }
        }
        return START_STICKY
    }

    fun setCallBack(actionPlaying: ActionPlaying) {
        this.actionPlaying = actionPlaying
    }

    private fun startMedia() {
        isPlayed = true
        buildAudioFocusRequest()
        if (mediaPlayer?.isPlaying == false && requestAudioFocus()) {
            mediaPlayer?.start()
            actionPlaying!!.playPrepared(true)
        } else if (!requestAudioFocus()) {
            pauseMedia()
        }
    }

    fun pauseMedia() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            resumePosition = mediaPlayer!!.currentPosition
        }
    }

    fun resumeMedia() {
        if (!focusGained) {
            buildAudioFocusRequest()
            focusGained = false
        }
        if (mediaPlayer?.isPlaying == false && requestAudioFocus()) {
            mediaPlayer?.seekTo(resumePosition)
            mediaPlayer?.start()
        } else if (!requestAudioFocus()) {
            pauseMedia()
        }
    }


    fun createMediaPlayer(mediaFile : String?) {
        if (mediaFile != null) {
            mediaData = mediaFile
        }
        if (mediaPlayer != null) { // 생성된 플레이어가 있으면 종료
            mediaPlayer?.release()
        }

        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setOnPreparedListener(this)

        mediaPlayer?.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )

        try { // mediaFile url로 data source 셋팅
            mediaFile?.let { mediaPlayer?.setDataSource(it) }
        } catch (e: IOException) {
            e.printStackTrace()
            stopSelf()
            return // 실패시 return
        }

        mediaPlayer?.prepareAsync() // 비동기식 prepare 실행
        mediaPlayer?.setOnCompletionListener { // 파일 재생 완료 후 다음 파일 호출
            actionPlaying!!.nextClicked()
        }
        mediaPlayer?.setOnErrorListener(this)

    }


    private fun buildAudioFocusRequest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                    setAudioAttributes(AudioAttributes.Builder().run {
                        setUsage(AudioAttributes.USAGE_GAME)
                        setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        build()
                    })
                    setAcceptsDelayedFocusGain(true)
                    setOnAudioFocusChangeListener(afChangeListener, handler)
                    setWillPauseWhenDucked(true)
                    build()
            }
        }
    }

    private fun requestAudioFocus(): Boolean {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val res = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.requestAudioFocus(focusRequest)
        } else {
            audioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }

        synchronized(focusLock) {
            playbackNowAuthorized = when (res) {
                AudioManager.AUDIOFOCUS_REQUEST_FAILED -> false
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {
                    true
                }
                AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> {
                    playbackDelayed = true
                    false
                }
                else -> false
            }
        }
        return playbackNowAuthorized
    }

    override fun onPrepared(mp: MediaPlayer?) {
        startMedia()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
            isPlayed = false
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN ->
                if (playbackDelayed || resumeOnFocusGain) {
                    synchronized(focusLock) {
                        playbackDelayed = false
                        resumeOnFocusGain = false
                    }
                    startMedia()
                    actionPlaying!!.playClicked()
                }
            AudioManager.AUDIOFOCUS_LOSS -> {
                synchronized(focusLock) {
                    resumeOnFocusGain = false
                    playbackDelayed = false
                }
                pauseMedia()
                actionPlaying!!.playClicked()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                synchronized(focusLock) {
                    resumeOnFocusGain = true
                    playbackDelayed = false
                }
                pauseMedia()
                actionPlaying!!.playClicked()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                synchronized(focusLock) {
                    resumeOnFocusGain = true
                    playbackDelayed = false
                }
                pauseMedia()
                actionPlaying!!.playClicked()
            }
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val afChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                if (mediaPlayer?.isPlaying == true)
                actionPlaying!!.playClicked()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if (mediaPlayer?.isPlaying == true)
                actionPlaying!!.playClicked()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                if (mediaPlayer?.isPlaying == true)
                actionPlaying!!.playClicked()
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (mediaPlayer == null) {
                    createMediaPlayer(mediaData)
                }
                else if (!mediaPlayer!!.isPlaying) {
                    focusGained = true
                    actionPlaying!!.playClicked()
                }
            }
        }
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        when (what) {
            MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK -> Log.d(
                "MediaPlayer Error",
                "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK $extra"
            )
            MediaPlayer.MEDIA_ERROR_SERVER_DIED -> Log.d(
                "MediaPlayer Error",
                "MEDIA ERROR SERVER DIED $extra"
            )
            MediaPlayer.MEDIA_ERROR_UNKNOWN -> Log.d(
                "MediaPlayer Error",
                "MEDIA ERROR UNKNOWN $extra"
            )
        }
        return false
    }




}

