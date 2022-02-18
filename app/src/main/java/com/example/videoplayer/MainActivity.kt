package com.example.videoplayer
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.videoplayer.databinding.ActivityMainBinding
import java.io.File
import java.time.Duration
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    companion object{
        lateinit var videoList: ArrayList<Video>
        lateinit var folderList:ArrayList<Folder>
    }
    lateinit var binding:ActivityMainBinding
    private lateinit var toggle:ActionBarDrawerToggle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setTheme(R.style.customThemecoolpinkNav)
        setContentView(binding.root)
        //for nav drawer
        toggle= ActionBarDrawerToggle(this,binding.root,R.string.open,R.string.close)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (requestRuntimePermission()){
            folderList=ArrayList()
            videoList=getAllVideos()
            setFragment(VideoFragment())
        }
        binding.bottomNav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.videoView-> {
                    Toast.makeText(this,"Video Fragment",Toast.LENGTH_SHORT).show()
                    setFragment(VideoFragment())
                }
                R.id.folderView-> {
                    Toast.makeText(this,"folder Fragment",Toast.LENGTH_SHORT).show()
                    setFragment(FolderFragment())
                }
            }
            return@setOnItemSelectedListener true
        }
        binding.navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.feedback->{
                    Toast.makeText(this,"Feedback...!!",Toast.LENGTH_SHORT).show()
                }
                R.id.themes->{
                    Toast.makeText(this,"Themes...!!",Toast.LENGTH_SHORT).show()
                }
                R.id.sortorder->{
                    Toast.makeText(this,"Sort Order...!!",Toast.LENGTH_SHORT).show()
                }
                R.id.about->{
                    Toast.makeText(this,"About...!!",Toast.LENGTH_SHORT).show()
                }
                R.id.exit->{
                    Toast.makeText(this,"Exit...!!",Toast.LENGTH_SHORT).show()
                    finishAffinity();
                    exitProcess(0);

                }
            }
            return@setNavigationItemSelectedListener true
        }
    }
    private fun setFragment(fragment: Fragment){
        val transaction =supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentFL,fragment)
        transaction.disallowAddToBackStack()
        transaction.commit()
    }
    private fun requestRuntimePermission():Boolean{
        if (ActivityCompat.checkSelfPermission(this,WRITE_EXTERNAL_STORAGE)!=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE),13)
            return false
        }
     return true
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode==13){
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Permission Granted...!!",Toast.LENGTH_SHORT).show()
                folderList=ArrayList()
                videoList=getAllVideos()
                setFragment(VideoFragment())
            }else{
                ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE),13)
            }
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)){
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("Range")
    private fun getAllVideos():ArrayList<Video>{
        val tempList=ArrayList<Video>()
        val tempFolderList=ArrayList<String>()
        val projection= arrayOf(MediaStore.Video.Media.TITLE,MediaStore.Video.Media.SIZE,
                                MediaStore.Video.Media._ID,MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                                MediaStore.Video.Media.DATA,
                                MediaStore.Video.Media.DATE_ADDED,MediaStore.Video.Media.DURATION,
                                MediaStore.Video.Media.BUCKET_ID)
        val cursor=this.contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,projection,null,
                                                null,MediaStore.Video.Media.DATE_ADDED+" DESC")
        if (cursor != null)
            if (cursor.moveToNext())
                do {
                    val titleC=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE))
                    val idC=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID))
                    val folderC=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
                    val folderIdC=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID))
                    val sizeC=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE))
                    val pathC=cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
                    val durationC=cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))
                    try {
                        val file=File(pathC)
                        val artUriC= Uri.fromFile(file)
                        val video=Video(title=titleC,id=idC, folderName = folderC, duration = durationC, size = sizeC,
                        path = pathC, artUri = artUriC)
                        if (file.exists())tempList.add(video)

                        //for adding folders
                        if (!tempFolderList.contains(folderC)){
                            tempFolderList.add(folderC)
                            folderList.add((Folder(id=folderIdC, folderName = folderC)))
                        }
                    }catch (e:Exception){
                        Log.d("Hello =====",e.toString())
                    }


                }while (cursor.moveToNext())
                cursor?.close()
        return tempList
    }
}