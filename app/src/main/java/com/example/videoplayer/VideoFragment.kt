package com.example.videoplayer

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.videoplayer.databinding.FragmentVideoBinding
class VideoFragment : Fragment() {
    private lateinit var adapter:VideoAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view=inflater.inflate(R.layout.fragment_video, container, false)
        val binding=FragmentVideoBinding.bind(view)
        binding.videoRV.setHasFixedSize(true)
        binding.videoRV.setItemViewCacheSize(10)
        binding.videoRV.layoutManager=LinearLayoutManager(requireContext())
        adapter=VideoAdapter(requireContext(),MainActivity.videoList)
        binding.videoRV.adapter=adapter

        //total videos
        binding.totalVideos.text="Total videos : ${MainActivity.videoList.size} "
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_view,menu)
        val searchView=menu.findItem(R.id.searchView).actionView as androidx.appcompat.widget.SearchView
        searchView.setOnQueryTextListener(object :androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean =true

            override fun onQueryTextChange(newText: String?): Boolean {
                if(newText!=null){
                    MainActivity.searchList=ArrayList()
                    for (video in MainActivity.videoList){
                        if (video.title.lowercase().contains(newText.lowercase()))
                            MainActivity.searchList.add(video)
                    }
                    MainActivity.search=true
                    adapter.updateList(searchList = MainActivity.searchList)

//                    Toast.makeText(requireContext(),newText.toString(),Toast.LENGTH_SHORT).show()
                }
                return true
            }

        })
        super.onCreateOptionsMenu(menu, inflater)
    }
}