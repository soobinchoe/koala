package com.traydcorp.koala.ui.home

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import com.traydcorp.koala.R
import com.traydcorp.koala.data.homeList
import com.traydcorp.koala.dataModel.HomeDetail
import com.traydcorp.koala.dataModel.NewsDetail
import com.traydcorp.koala.databinding.ActivityHomeBinding
import com.traydcorp.koala.ui.player.ActionPlaying
import com.traydcorp.koala.ui.player.NotificationReceiver
import com.traydcorp.koala.ui.player.PlayerService
import com.traydcorp.newdio.utils.retofitService.RetrofitService
import com.traydcorp.newdio.utils.retrofitAPI.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import kotlin.collections.ArrayList

class HomeActivity : AppCompatActivity(), ActionPlaying, ServiceConnection {

    private lateinit var viewBinding : ActivityHomeBinding
    private var doubleBackToExitPressedOnce = false
    var homeDetailList: List<HomeDetail> = ArrayList()
    private var newsList = ArrayList<NewsDetail>()
    private var lastId = 0

    lateinit var retrofit: Retrofit
    lateinit var supplementService: RetrofitService

    private var playService: PlayerService? = null
    private var mediaSession: MediaSessionCompat? = null
    var isPlay = false
    var isResume = false
    var index = 0
    var category : String? = null
    var reporter : String? = null
    var reporterImg : Int? = 0
    var isPause = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(RetrofitService::class.java)

        // 홈 캐릭터 data 생성
        homeDetailList = homeList(resources)

        // 홈 fragment
        supportFragmentManager.beginTransaction().add(R.id.homeView, HomeFragment(), "home").commit()
    }

    // 뒤로가기 두번 시 앱 종료
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce || supportFragmentManager.backStackEntryCount != 0) {
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true
        Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

    fun getReporter() {
        for (i in homeDetailList.indices){
            if (category == homeDetailList[i].category){
                reporter = homeDetailList[i].characterName
                reporterImg = homeDetailList[i].characterImgChat
            } else if (category == null) {
                reporter = homeDetailList[0].characterName
                reporterImg = homeDetailList[1].characterImgChat
            }
        }
    }

    // 재생 리스트 API 호출
    fun getNewsList(service: Call<List<NewsDetail>>, reload: Boolean) {
        Handler(Looper.getMainLooper()).postDelayed({
            service.enqueue(object : Callback<List<NewsDetail>> {
                override fun onResponse(
                    call: Call<List<NewsDetail>>,
                    response: Response<List<NewsDetail>>
                ) {
                    if (response.code() == 200){
                        val result = response.body() as ArrayList<NewsDetail>
                        newsList = result

                        if (reload) { // 추가 로드
                            for (i in result.indices) {
                                newsList.add(result[i])
                            }
                        } else { // 추가 로드가 아닐 때(다른 카테고리 선택시) isPlay, isResume 초기화
                            isPlay = false
                            isResume = false
                        }

                        if (newsList.isNotEmpty()){ // 검색 결과가 empty가 아닐 때
                            lastId = newsList[newsList.lastIndex].id!!
                            if (!reload) playClicked()
                        }

                        getReporter()
                    } else {

                    }
                }

                override fun onFailure(call: Call<List<NewsDetail>>, t: Throwable) {
                }

            })
        }, 1000)
    }

    // service 연결 되었을 때
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as PlayerService.LocalBinder
        playService = binder.service
        playService!!.setCallBack(this)

        newsList[index].audio_url?.let { playMedia(it) }
    }

    // service 연결 해제
    override fun onServiceDisconnected(name: ComponentName?) {
        playService = null
    }

    // media 재생
    fun playMedia(mediaFile: String) {
        playService?.createMediaPlayer(mediaFile)
    }

    // play 눌렀을 때
    override fun playClicked() {
        super.playClicked()

        if(newsList.isEmpty()) return

        val homeFragment = supportFragmentManager.findFragmentByTag("home") as HomeFragment

        // 첫 재생일 때
        if (!isPlay && !isResume) {
            if (playService == null) { // 연결 되어있는 서비스가 없으면 서비스 연결
                connectMediaService()
            } else { // service가 있을 때 media file 변경
                newsList[index].audio_url?.let { playMedia(it) }
            }
        } else if(!isPlay && isResume) { // 일시정지 후 재생일 때
            playService?.resumeMedia()
            isPlay = true
            homeFragment.resetPlayBtn(null) // 홈에있는 플레이 버튼 업데이트
            buildNotification(android.R.drawable.ic_media_pause, 1F) // 알람창 플레이버튼 업데이트
        } else if (isPlay){ // 일시정지
            isPause = true
            playService?.pauseMedia()
            isPlay = false
            homeFragment.resetPlayBtn("pause")
            buildNotification(android.R.drawable.ic_media_play, 0F)
        }
    }

    // media가 prepared 되었을 때
    override fun playPrepared(play : Boolean) {
        super.playPrepared(play)
        buildNotification(android.R.drawable.ic_media_pause, 1F)
        isPlay = true
        isResume = true
    }

    // 다음 media
    override fun nextClicked() {
        super.nextClicked()
        index ++
        if (index == newsList.size-1) { // 리스트의 마지막일 때 추가 load
            getNewsList(supplementService.getNewsList(category, newsList[newsList.lastIndex].id), true)
        }
        newsList[index].audio_url?.let { playMedia(it) }

    }

    // service와 연결하기
    fun connectMediaService() {

        val playerIntent = Intent(this, PlayerService::class.java)

        if (playService == null) {
            this.startService(playerIntent)
        }

        this.bindService(playerIntent, this, Context.BIND_AUTO_CREATE)
        mediaSession = MediaSessionCompat(this, "AudioPlayer")
    }

    // media style notification 플레이어 알람창
    private fun buildNotification(playPauseBtn : Int, playBackSpeed: Float) {
        Log.d("buildNotification", "call")
        // notification 이미지
        val icon = BitmapFactory.decodeResource(this.resources, reporterImg!!)

        // media session
        mediaSession!!.setPlaybackState(
            PlaybackStateCompat.Builder()
            .setState(
                PlaybackStateCompat.STATE_PLAYING,
                playService?.mediaPlayer?.currentPosition!!.toLong(),
                playBackSpeed
            )
            .build()
        )

        // notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "koala"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            (this.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(mChannel)
        }

        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK)

        val contentIntent : PendingIntent? = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val playIntent = Intent(this, NotificationReceiver::class.java).setAction(Companion.ACTION_PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val deleteIntent = Intent(this, NotificationReceiver::class.java).setAction(
            Companion.ACTION_DELETE
        )
        val deletePendingIntent = PendingIntent.getBroadcast(this, 0, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setShowWhen(true) // Set the Notification style
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession!!.sessionToken) // Show our playback controls in the compact notification view.
                    .setShowCancelButton(true)
                    .setShowActionsInCompactView(0,1)
            )
            .setLargeIcon(icon)
            .setSmallIcon(R.drawable.ic_general_koala_chatting)
            .setContentTitle(reporter)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent)
            .addAction(playPauseBtn, "pause", playPendingIntent)
            .addAction(android.R.drawable.ic_delete, "delete", deletePendingIntent)

        (this.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager).notify(
            NOTIFICATION_ID, notificationBuilder.build()
        )
    }

    override fun stopClicked() {
        super.stopClicked()
        buildNotification(android.R.drawable.ic_media_play, 0F)
        playService!!.stopForeground(true)
        playService?.onDestroy()
        removeNotification()
    }

    // notification 삭제
    private fun removeNotification() {
        val notificationManager = this.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

    override fun onDestroy() {
        super.onDestroy()
        playService?.onDestroy()
        removeNotification()
    }

    companion object {
        const val ACTION_PLAY = "PLAY"
        const val ACTION_DELETE = "DELETE"
        const val ACTION_NEXT = "NEXT"
        const val CHANNEL_ID = "KOALA"
        const val NOTIFICATION_ID = 111
    }

}