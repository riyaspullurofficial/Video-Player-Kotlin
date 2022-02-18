package com.example.videoplayer

import android.annotation.SuppressLint
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.videoplayer.databinding.ActivityFoldersBinding
import java.io.File

class FoldersActivity : AppCompatActivity() {
    companion object{
        lateinit var currentFolderVideos:ArrayList<Video>
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.customThemecoolpinkNav)
        val binding=ActivityFoldersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val position=intent.getIntExtra("position",0)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=MainActivity.folderList[position].folderName
        currentFolderVideos=getAllVideos(MainActivity.folderList[position].id)
        binding.videoRVFA.setHasFixedSize(true)
        binding.videoRVFA.setItemViewCacheSize(10)
        binding.videoRVFA.layoutManager= LinearLayoutManager(this@FoldersActivity)
        binding.videoRVFA.adapter=VideoAdapter(this@FoldersActivity, currentFolderVideos,isFolder = true)
        binding.totalVideosFA.text="Total Videos : ${currentFolderVideos.size}"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }
    @SuppressLint("Range")
    private fun getAllVideos(folderId:String):ArrayList<Video>{
        val tempList=ArrayList<Video>()
        val selection=MediaStore.Video.Media.BUCKET_ID+" like?"
        val tempFolderList=ArrayList<String>()
        val projection= arrayOf(
            MediaStore.Video.Media.TITLE, MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media._ID, MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_ADDED, MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.BUCKET_ID)
        val cursor=this.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,projection,selection,
            arrayOf(folderId), MediaStore.Video.Media.DATE_ADDED+" DESC")
        if (cursor != null)
            if (cursor.moveToNext())
                do {
                    val titleC=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE))
                    val idC=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID))
                    val folderC=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))

                    val sizeC=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE))
                    val pathC=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
                    val durationC=cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))
                    try {
                        val file= File(pathC)
                        val artUriC= Uri.fromFile(file)
                        val video=Video(title=titleC,id=idC, folderName = folderC, duration = durationC, size = sizeC,
                            path = pathC, artUri = artUriC)
                        if (file.exists())tempList.add(video)


                    }catch (e:Exception){
                        Log.d("Hello =====",e.toString())
                    }


                }while (cursor.moveToNext())
        cursor?.close()
        return tempList
    }
}