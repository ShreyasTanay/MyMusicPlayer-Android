package com.example.android.mymusicplayer.fragments


import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.android.mymusicplayer.R
import com.example.android.mymusicplayer.R.id.action_sort_ascending
import com.example.android.mymusicplayer.R.id.action_sort_recent
import com.example.android.mymusicplayer.Songs
import com.example.android.mymusicplayer.adapters.MainScreenAdapter
import java.util.*


class MainScreenFragment : Fragment() {

    var nowPlayingBottomBar: RelativeLayout? = null
    var getSongList: ArrayList<Songs>? = null
    var playPauseButton: ImageButton? = null
    var songTitle: TextView? = null
    var songArtist: TextView? = null
    var visibleLayout: RelativeLayout? = null
    var noSongs: RelativeLayout? = null
    var recyclerView: RecyclerView? = null
    var myActivity: Activity? = null
    var _mainScreenAdapter: MainScreenAdapter? = null
    var trackPosition: Int = 0

    object Statified{
        var mediaPlayer: MediaPlayer? = null
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_main_screen, container, false)
        setHasOptionsMenu(true)

        activity.title = "all songs"
        visibleLayout = view?.findViewById(R.id.visibleLayout)
        noSongs = view?.findViewById(R.id.noSongs)
        nowPlayingBottomBar = view?.findViewById(R.id.hiddenBarMainScreen)
        songTitle = view?.findViewById(R.id.songTitleNowPlaying)
        songArtist = view?.findViewById(R.id.songArtistNowPlaying)
        playPauseButton = view?.findViewById(R.id.playPauseButton)
        recyclerView = view?.findViewById(R.id.contentMain)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        getSongList = getSongsFromPhone()
        val getSongsList = getSongList
        val prefs = activity.getSharedPreferences("action_sort", Context.MODE_PRIVATE)
        val action_sort_ascending = prefs.getString("action_sort_ascending", "true")
        val action_sort_recent = prefs.getString("action_sort_recent", "false")

        if(getSongsList == null){
            recyclerView?.visibility = View.INVISIBLE
            noSongs?.visibility = View.VISIBLE
        }
        else{
            _mainScreenAdapter = MainScreenAdapter(getSongsList, myActivity as Context)
            val mLayoutManager = LinearLayoutManager(myActivity)
            recyclerView?.layoutManager = mLayoutManager
            recyclerView?.itemAnimator = DefaultItemAnimator()
            recyclerView?.adapter = _mainScreenAdapter
        }

        if(getSongsList != null){
            if(action_sort_ascending!!.equals("true", true)){
                Collections.sort(getSongsList, Songs.Statified.nameComparator)
                _mainScreenAdapter?.notifyDataSetChanged()
            }
            else if(action_sort_recent!!.equals("true", true)){
                Collections.sort(getSongsList, Songs.Statified.dateComparator)
                _mainScreenAdapter?.notifyDataSetChanged()
            }
        }

        bottomBar_Setup()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.main, menu)
        return
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val switcher = item?.itemId
        if(switcher == R.id.action_sort_ascending){

            val editor = myActivity?.getSharedPreferences("action_sort", Context.MODE_PRIVATE)?.edit()
            editor?.putString("action_sort_ascending", "true")
            editor?.putString("action_sort_recent", "false")
            editor?.apply()

            if(getSongList != null){
                Collections.sort(getSongList, Songs.Statified.nameComparator)
                _mainScreenAdapter?.notifyDataSetChanged()
            }

            return false
        }
        else if(switcher == R.id.action_sort_recent){
            val editor = myActivity?.getSharedPreferences("action_sort", Context.MODE_PRIVATE)?.edit()
            editor?.putString("action_sort_ascending", "false")
            editor?.putString("action_sort_recent", "true")
            editor?.apply()

            if(getSongList != null){
                Collections.sort(getSongList, Songs.Statified.dateComparator)
                _mainScreenAdapter?.notifyDataSetChanged()
            }

            return false
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity
    }

    fun getSongsFromPhone(): ArrayList<Songs>{
        var arrayList = ArrayList<Songs>()
        var contentResolver = myActivity?.contentResolver
        var songURI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var songCursor = contentResolver?.query(songURI,null,null,null,null)

        if(songCursor!=null && songCursor.moveToFirst()){
            val songID = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val dateIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)

            while(songCursor.moveToNext()){
                var currentID = songCursor.getLong(songID)
                var currentTitle = songCursor.getString(songTitle)
                var currentArtist = songCursor.getString(songArtist)
                var currentData = songCursor.getString(songData)
                var currentDate = songCursor.getLong(dateIndex)

                arrayList.add(Songs(currentID, currentTitle, currentArtist, currentData, currentDate))
            }
        }

        return arrayList
    }

    fun bottomBar_Setup(){
        try{
            bottomBarClickHandler()
            songTitle?.setText(SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            songArtist?.setText(SongPlayingFragment.Statified.currentSongHelper?.songArtist)
            SongPlayingFragment.Statified.mediaplayer?.setOnCompletionListener({
                songTitle?.setText(SongPlayingFragment.Statified.currentSongHelper?.songTitle)
                songArtist?.setText(SongPlayingFragment.Statified.currentSongHelper?.songArtist)
                SongPlayingFragment.Staticated.onSongComplete()
            })

            if(SongPlayingFragment.Statified.currentSongHelper?.isPlaying as Boolean){
                nowPlayingBottomBar?.visibility = View.VISIBLE
            }
            else{
                nowPlayingBottomBar?.visibility = View.INVISIBLE
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun bottomBarClickHandler(){
        FavouritesFragment.Statified.mediaPlayer = SongPlayingFragment.Statified.mediaplayer
        nowPlayingBottomBar?.setOnClickListener({
            val NowPlayingOpen = SongPlayingFragment()
            MainScreenFragment.Statified.mediaPlayer = SongPlayingFragment.Statified.mediaplayer
            var args = Bundle()
            args.putString("songArtist", SongPlayingFragment.Statified.currentSongHelper?.songArtist)
            args.putString("path", SongPlayingFragment.Statified.currentSongHelper?.songPath)
            args.putString("songTitle", SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            args.putInt("songId", SongPlayingFragment.Statified.currentSongHelper?.songId?.toInt() as Int)
            args.putInt("songPosition", SongPlayingFragment.Statified.currentSongHelper?.currentPosition?.toInt() as Int)
            args.putParcelableArrayList("songData", SongPlayingFragment.Statified.fetchSongs)
            args.putString("FavBottomBar", "success")
            NowPlayingOpen.arguments = args

            fragmentManager.beginTransaction()
                    .replace(R.id.fragment1, NowPlayingOpen)
                    .addToBackStack("SongPlayingFragment")
                    .commit()
        })

        playPauseButton?.setOnClickListener({
            if(SongPlayingFragment.Statified.currentSongHelper?.isPlaying as Boolean){
                SongPlayingFragment.Statified.currentSongHelper?.isPlaying = false
                SongPlayingFragment.Statified.mediaplayer?.pause()
                trackPosition = SongPlayingFragment.Statified.mediaplayer?.getCurrentPosition() as Int
                playPauseButton?.setBackgroundResource(R.drawable.ic_play_green)
            }
            else{
                SongPlayingFragment.Statified.currentSongHelper?.isPlaying = true
                SongPlayingFragment.Statified.mediaplayer?.seekTo(trackPosition)
                SongPlayingFragment.Statified.mediaplayer?.start()
                playPauseButton?.setBackgroundResource(R.drawable.ic_pause_green)
            }
        })
    }
}// Required empty public constructor
