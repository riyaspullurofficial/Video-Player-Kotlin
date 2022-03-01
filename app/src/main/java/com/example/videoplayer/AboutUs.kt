package com.example.videoplayer

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.videoplayer.databinding.ActivityAboutUsBinding

class AboutUs : AppCompatActivity() {
    companion object{
        lateinit var binding:ActivityAboutUsBinding
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAboutUsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.whatsappImageBtn.setOnClickListener {
            openWhatsapp()
        }
        binding.backBtnAbout.setOnClickListener{
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }

    }
    private fun openWhatsapp(){
        val number="+919072990008"
        val url = "https://api.whatsapp.com/send?phone=$number"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
    }

}