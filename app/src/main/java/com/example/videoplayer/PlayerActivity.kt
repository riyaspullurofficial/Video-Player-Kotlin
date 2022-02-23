package com.example.videoplayer

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.videoplayer.databinding.ActivityPlayerBinding
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import java.lang.Exception

class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
    companion object{
       private lateinit var player:SimpleExoPlayer
        lateinit var playerList:ArrayList<Video>
        var position:Int=-1
        var repeat:Boolean=false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode=WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        setTheme(R.style.playerActivityTheme)
        binding=ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Immersive Mode
        WindowCompat.setDecorFitsSystemWindows(window,false)
        WindowInsetsControllerCompat(window,binding.root).let{controller -> 
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior= WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        initializeLayout()
        initializeBinding()
    }
    private fun initializeLayout(){
            when(intent.getStringExtra("class")){
                "AllVideos"->{
                    playerList= ArrayList()
                    playerList.addAll(MainActivity.videoList)
                    createPlayer()
                }
                "FolderActivity"->{
                    playerList= ArrayList()
                    playerList.addAll(FoldersActivity.currentFolderVideos)
                    createPlayer()
                }
            }
    }
    private fun initializeBinding(){

        binding.backBtn.setOnClickListener {
            finish()
        }
        binding.playPauseBtn.setOnClickListener {
                if (player.isPlaying){
                    pauseVideo()
                }else{
                    playVideo()
                }
        }
        binding.nextBtn.setOnClickListener { nextPrevVideo() }
        binding.prevBtn.setOnClickListener { nextPrevVideo(isNext = false) }
        binding.repeatBtn.setOnClickListener {
                if (repeat){
                    repeat=false
                    player.repeatMode=Player.REPEAT_MODE_OFF
                    binding.repeatBtn.setImageResource(R.drawable.repeat_icon)
                }else{
                    repeat=true
                    player.repeatMode=Player.REPEAT_MODE_ONE
                    binding.repeatBtn.setImageResource(R.drawable.repeate_true_icon)
                }

        }
    }
    private fun createPlayer(){
        try { player.release() }catch (e:Exception){Log.d("Error==",e.toString())}
        binding.videoTitle.text= playerList[position].title
        binding.videoTitle.isSelected=true
        player=SimpleExoPlayer.Builder(this).build()
        binding.playerView.player=player
        val mediaItem=MediaItem.fromUri(playerList[position].artUri)
        player.setMediaItem(mediaItem)
        player.prepare()
        playVideo()

        player.addListener(object :Player.Listener{
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == Player.STATE_ENDED) nextPrevVideo()
            }
        })
    }
    override fun onDestroy() {
        super.onDestroy()
        try {
            player.release()
        }catch (e:Exception){
            Log.d("Exc : ",e.toString())
        }
    }
    private fun playVideo(){
        binding.playPauseBtn.setImageResource(R.drawable.pause_icon)
        player.play()
    }
    private fun pauseVideo(){
      binding.playPauseBtn.setImageResource(R.drawable.play_icon)
        player.pause()
    }
    private fun nextPrevVideo(isNext:Boolean=true){
        player.stop()
        if (isNext)setPosition()
        else setPosition(isIncrement = false)
        createPlayer()
    }
    private fun setPosition(isIncrement:Boolean=true){
        if(!repeat){
            if (isIncrement){
                if (playerList.size -1 == position ) position=0
                else ++position
            }else{
                if (position==0) position= playerList.size-1
                else --position
            }
        }
    }

}