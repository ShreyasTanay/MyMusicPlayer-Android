package com.example.android.mymusicplayer.adapters

import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.android.mymusicplayer.R
import com.example.android.mymusicplayer.Songs
import com.example.android.mymusicplayer.fragments.SongPlayingFragment

/**
 * Created by Shreyas Tanay on 18-01-2018.
 */
class FavouritesAdapter(_songDetails: ArrayList<Songs>, _context: Context): RecyclerView.Adapter<FavouritesAdapter.MyViewHolder>() {
    var songDetails: ArrayList<Songs>? = null
    var mContext: Context? = null

    init{
        this.songDetails = _songDetails
        this.mContext = _context
    }


    override fun onBindViewHolder(holder: MyViewHolder?, position: Int) {
        val songObject = songDetails?.get(position)
        holder?.trackTitle?.text = songObject?.artist
        holder?.trackArtist?.text = songObject?.songTitle
        holder?.contentHolder?.setOnClickListener(View.OnClickListener {
            SongPlayingFragment.Statified.mediaplayer?.reset()

            val NowPlayingOpen = SongPlayingFragment()
            var args = Bundle()
            args.putString("songArtist", songObject?.songTitle)
            args.putString("path", songObject?.songData)
            args.putString("songTitle", songObject?.artist)
            args.putInt("songId", songObject?.songID?.toInt() as Int)
            args.putInt("songPosition", position)
            args.putParcelableArrayList("songData", songDetails)
            NowPlayingOpen.arguments = args

            if(SongPlayingFragment.Statified.mediaplayer != null && SongPlayingFragment.Statified.mediaplayer?.isPlaying as Boolean){
                SongPlayingFragment.Statified.mediaplayer?.pause()
                SongPlayingFragment.Statified.mediaplayer?.release()
            }

            (mContext as FragmentActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment1, NowPlayingOpen)
                    .commit()
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent?.context)
                .inflate(R.layout.row_custom_mainscreen_adapter,parent,false)

        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        if(songDetails == null){
            return 0
        }
        else{
            return (songDetails as ArrayList<Songs>).size
        }
    }

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {
        var trackTitle: TextView? = null
        var trackArtist: TextView? = null
        var contentHolder: RelativeLayout? = null

        init{
            trackTitle = view.findViewById(R.id.trackTitle)
            trackArtist = view.findViewById(R.id.trackArtist)
            contentHolder = view.findViewById(R.id.contentRow)
        }
    }

}