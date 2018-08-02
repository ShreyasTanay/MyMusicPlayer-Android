package com.example.android.mymusicplayer.fragments


import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.android.mymusicplayer.R
import com.example.android.mymusicplayer.Songs
import com.example.android.mymusicplayer.adapters.FavouritesAdapter
import com.example.android.mymusicplayer.databases.EchoDatabase


/**
 * A simple [Fragment] subclass.
 */
class FavouritesFragment : Fragment() {
    var myActivity: Activity? = null
    var noFavourites: TextView? = null
    var nowPlayingBottomBar: RelativeLayout? = null
    var playPauseButton: ImageButton? = null
    var songTitle: TextView? = null
    var songArtist: TextView? = null
    var recyclerView: RecyclerView? = null
    var trackPosition: Int = 0

    var favouriteContent: EchoDatabase? = null
    var refreshList: ArrayList<Songs>? = null
    var getListfromDatabase: ArrayList<Songs>? = null

    object Statified{
        var mediaPlayer: MediaPlayer? = null
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_favourites, container, false)
        setHasOptionsMenu(true)

        activity.title = "favourites"
        noFavourites = view?.findViewById(R.id.noFavourites)
        nowPlayingBottomBar = view?.findViewById(R.id.hiddenBarFavScreen)
        playPauseButton = view?.findViewById(R.id.playPauseButtonFav)
        songTitle = view?.findViewById(R.id.songTitleNowPlayingFav)
        songArtist = view?.findViewById(R.id.songArtistNowPlayingFav)
        recyclerView = view?.findViewById(R.id.favouriteRecycler)

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        favouriteContent = EchoDatabase(myActivity)
        display_favourites_by_searching()
        bottomBarSetup()

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)

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

    fun bottomBarSetup(){
        try{
            bottomBarClickHandler()
            songTitle?.setText(SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            songArtist?.setText(SongPlayingFragment.Statified.currentSongHelper?.songArtist)
            SongPlayingFragment.Statified.mediaplayer?.setOnCompletionListener({
                SongPlayingFragment.Staticated.onSongComplete()
                songTitle?.setText(SongPlayingFragment.Statified.currentSongHelper?.songTitle)
                songArtist?.setText(SongPlayingFragment.Statified.currentSongHelper?.songArtist)
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
        Statified.mediaPlayer = SongPlayingFragment.Statified.mediaplayer
        nowPlayingBottomBar?.setOnClickListener({
            val NowPlayingOpen = SongPlayingFragment()
            var args = Bundle()
            args.putString("songArtist", SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            args.putString("path", SongPlayingFragment.Statified.currentSongHelper?.songPath)
            args.putString("songTitle", SongPlayingFragment.Statified.currentSongHelper?.songArtist)
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

    fun display_favourites_by_searching(){
        if(favouriteContent?.checkSize() as Int > 0){
            refreshList = ArrayList<Songs>()
            getListfromDatabase = favouriteContent?.queryDBList()
            var fetchListfromDevide: ArrayList<Songs>? = getSongsFromPhone()
            if(fetchListfromDevide != null){
                for(i in 0..(fetchListfromDevide.size -1)){
                    for(j in 0..(getListfromDatabase?.size as Int -1)){
                        if(getListfromDatabase?.get(j)?.songID == fetchListfromDevide[i].songID){
                            refreshList?.add((getListfromDatabase as ArrayList<Songs>)[j])
                        }
                    }
                }
            }

            if(refreshList == null){
                recyclerView?.visibility = View.INVISIBLE
                noFavourites?.visibility = View.VISIBLE
            }
            else{
                var favouritesAdapter = FavouritesAdapter(refreshList as ArrayList<Songs>, myActivity as Context)
                val mLayoutManager = LinearLayoutManager(myActivity)
                recyclerView?.layoutManager = mLayoutManager
                recyclerView?.itemAnimator = DefaultItemAnimator()
                recyclerView?.adapter = favouritesAdapter
                recyclerView?.setHasFixedSize(true)
            }
        }
        else{
            recyclerView?.visibility = View.INVISIBLE
            noFavourites?.visibility = View.VISIBLE
        }
    }

}// Required empty public constructor
