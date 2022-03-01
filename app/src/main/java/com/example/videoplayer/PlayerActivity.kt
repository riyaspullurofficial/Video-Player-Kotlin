package com.example.videoplayer

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.opengl.Visibility
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.videoplayer.databinding.ActivityPlayerBinding
import com.example.videoplayer.databinding.MoreFeatureBinding
import com.example.videoplayer.databinding.SpeedDialogBinding
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.Exception
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList

class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
    private lateinit var runnable: Runnable
    private var isSubTitle:Boolean=true
    private var moreTime:Int=0
    companion object{
       private lateinit var player:ExoPlayer
        lateinit var playerList:ArrayList<Video>
        var position:Int=-1
        var repeat:Boolean=false
        var isFullScreen:Boolean= false
        var isLocked:Boolean=false
        lateinit var trackSelector:DefaultTrackSelector
        private var speed:Float = 1.0f
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
        binding.forwardFL.setOnClickListener(DoubleClickListener(callback = object :DoubleClickListener.Callback{
            override fun doubleClicked() {
                binding.playerView.showController()
                binding.forwardBtn.visibility=View.VISIBLE
                player.seekTo(player.currentPosition + 10000)
                moreTime=0
            }
        }))
        binding.rewindFL.setOnClickListener(DoubleClickListener(callback = object :DoubleClickListener.Callback{
            override fun doubleClicked() {
                binding.playerView.showController()
                binding.rewindBtn.visibility=View.VISIBLE
                player.seekTo(player.currentPosition - 10000)
                moreTime=0
            }
        }))
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
                "SearchVideos"->{
                    playerList= ArrayList()
                    playerList.addAll(MainActivity.searchList)
                    createPlayer()
                }
            }
    }
    @SuppressLint("SetTextI18n")
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
        binding.fullScreenBtn.setOnClickListener {
            if(isFullScreen){
                isFullScreen=false
                playInFullScreen(enable = isFullScreen)
            }else{
                isFullScreen=true
                playInFullScreen(enable = isFullScreen)
            }
        }
        binding.lockBtn.setOnClickListener {
            if (isLocked){
                //hiding
                isLocked=false
                binding.playerView.hideController()
                binding.playerView.useController=false
                binding.lockBtn.setImageResource(R.drawable.lock_closed_icon)
            }
            else{
                //for showing
                isLocked=true
                binding.playerView.showController()
                binding.playerView.useController=true
                binding.lockBtn.setImageResource(R.drawable.lock_open_icon)
            }
        }
        binding.moreFeatures.setOnClickListener {
           pauseVideo()
            val customDialog=LayoutInflater.from(this).inflate(R.layout.more_feature,binding.root,false)
            val bindingMF=MoreFeatureBinding.bind(customDialog)
            val dialog=MaterialAlertDialogBuilder(this).setView(customDialog)
                .setOnCancelListener{playVideo()}
                .setBackground(ColorDrawable(0x80000000.toInt()))
                .create()
            dialog.show()
            bindingMF.audioTrack.setOnClickListener {
                dialog.dismiss()
                playVideo()
                val audioTrack=ArrayList<String>()
                for (i in 0 until player.currentTrackGroups.length){
                    if (player.currentTrackGroups.get(i).getFormat(0).selectionFlags==C.SELECTION_FLAG_DEFAULT){
                        audioTrack.add(Locale(player.currentTrackGroups.get(i).getFormat(0).language.toString()).displayLanguage)
                    }
                }
                val tempTracks=audioTrack.toArray(arrayOfNulls<CharSequence>(audioTrack.size))
                MaterialAlertDialogBuilder(this,R.style.alertDialog)
                    .setTitle("Select Language")
                    .setOnCancelListener{playVideo()}
                    .setBackground(ColorDrawable(0x80000000.toInt()))
                    .setItems(tempTracks){_, position ->
                        Toast.makeText(this,audioTrack[position] +" Selected",Toast.LENGTH_SHORT).show()
                        trackSelector.setParameters(trackSelector.buildUponParameters().setPreferredAudioLanguage(audioTrack[position]))
                    }
                    .create()
                    .show()
            }
            bindingMF.subTitleBtn.setOnClickListener {
                if (isSubTitle){
                    trackSelector.parameters=DefaultTrackSelector.ParametersBuilder(this).setRendererDisabled(
                        C.TRACK_TYPE_VIDEO,true
                    ).build()
                    Toast.makeText(this,"Sub titles OFF",Toast.LENGTH_SHORT).show()
                    isSubTitle=false
                }else{
                    trackSelector.parameters=DefaultTrackSelector.ParametersBuilder(this).setRendererDisabled(
                        C.TRACK_TYPE_VIDEO,false
                    ).build()
                    Toast.makeText(this,"Sub titles ON",Toast.LENGTH_SHORT).show()
                    isSubTitle=true
                }
                dialog.dismiss()
                playVideo()
            }
            bindingMF.speedBtn.setOnClickListener {
                dialog.dismiss()
                playVideo()
                val customDialogS=LayoutInflater.from(this).inflate(R.layout.speed_dialog,binding.root,false)
                val bindingS=SpeedDialogBinding.bind(customDialogS)
                val dialogS=MaterialAlertDialogBuilder(this).setView(customDialogS)
                    .setCancelable(false)
                    .setPositiveButton("OK"){self, _->
                        self.dismiss()
                    }
                    .setBackground(ColorDrawable(0x80000000.toInt()))
                    .create()
                dialogS.show()
                bindingS.speedText.text="${DecimalFormat("#.##").format(speed)} X"
                bindingS.minusBtn.setOnClickListener {
                    changeSpeed(isIncrement = false)
                    bindingS.speedText.text="${DecimalFormat("#.##").format(speed)} X"
                }
                bindingS.plusBtn.setOnClickListener {
                    changeSpeed(isIncrement = true)
                    bindingS.speedText.text="${DecimalFormat("#.##").format(speed)} X"
                }

            }
        }
    }
    private fun createPlayer(){
        try { player.release() }catch (e:Exception){Log.d("Error==",e.toString())}
        speed =1.0f
        trackSelector=DefaultTrackSelector(this)
        binding.videoTitle.text= playerList[position].title
        binding.videoTitle.isSelected=true
        player=ExoPlayer.Builder(this).setTrackSelector(trackSelector).build()
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
        playInFullScreen(enable = isFullScreen)
        setVisibility()

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
    private fun playInFullScreen(enable:Boolean){
        if (enable){
            binding.playerView.resizeMode=AspectRatioFrameLayout.RESIZE_MODE_FILL
            player.videoScalingMode= C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            binding.fullScreenBtn.setImageResource(R.drawable.fullscreen_exit_icon)
        }else{
            binding.playerView.resizeMode=AspectRatioFrameLayout.RESIZE_MODE_FIT
            player.videoScalingMode= C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            binding.fullScreenBtn.setImageResource(R.drawable.fullscreen_icon)
        }
    }
    private fun setVisibility(){
        runnable= Runnable {
            if (binding.playerView.isControllerVisible) changeVisibility(View.VISIBLE)
            else changeVisibility(View.GONE)
            Handler(Looper.getMainLooper()).postDelayed(runnable,1000)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable,0)
    }
    private fun changeVisibility(visibility:Int){
        binding.topController.visibility=visibility
        binding.bottomController.visibility=visibility
        binding.playPauseBtn.visibility=visibility
      if(moreTime== 2){
          binding.rewindBtn.visibility=View.GONE
          binding.forwardBtn.visibility=View.GONE
      }else ++moreTime
    }
    private fun changeSpeed(isIncrement:Boolean){
        if (isIncrement){
            if (speed <= 2.9f){
                speed +=0.10f
            }
        }else{
            if (speed >0.20f){
                speed -=0.10f
            }
        }
    }

    override fun onStop() {
        super.onStop()
        pauseVideo()
    }

    override fun onResume() {
        super.onResume()
        playVideo()
    }
}