package com.example.android.mymusicplayer.fragments


import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.*
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import com.example.android.mymusicplayer.CurrentSongHelper
import com.example.android.mymusicplayer.R
import com.example.android.mymusicplayer.Songs
import com.example.android.mymusicplayer.Utils.SeekBarController
import com.example.android.mymusicplayer.databases.EchoDatabase
import kotlinx.android.synthetic.main.fragment_song_playing.*
import org.w3c.dom.Text
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * A simple [Fragment] subclass.
 */
class SongPlayingFragment : Fragment() {
    object Statified {
        var myActivity: Activity? = null
        var mediaplayer: MediaPlayer? = null
        var startTimeText: TextView? = null
        var endTimeText: TextView? = null
        var playPauseImageButton: ImageButton? = null
        var previousImageButton: ImageButton? = null
        var nextImageButton: ImageButton? = null
        var loopImageButton: ImageButton? = null
        var shuffleImageButton: ImageButton? = null
        var seekbar: SeekBar? = null
        var songTitleView: TextView? = null
        var songArtistView: TextView? = null
        var fab: ImageButton? = null

        var currentPosition: Int = 0
        var fetchSongs: ArrayList<Songs>? = null
        var currentSongHelper: CurrentSongHelper? = null

        var audioVisualizer: AudioVisualization? = null
        var glView: GLAudioVisualizationView? = null

        var favouriteContent: EchoDatabase? = null

        var mSensorManager: SensorManager? = null
        var mSensorListener: SensorEventListener? = null
        var MY_PREFS_NAME = "ShakeFeature"

        var updateSongTime = object : Runnable {
            override fun run() {
                val getCurrent = mediaplayer?.currentPosition
                val finalTime = mediaplayer?.duration
                startTimeText?.setText(String.format("%d:%2d", TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong() as Long), (TimeUnit.MILLISECONDS.toSeconds(getCurrent?.toLong() as Long) - TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong() as Long))) % 60))
                endTimeText?.setText(String.format("%d:%2d", TimeUnit.MILLISECONDS.toMinutes(finalTime?.toLong() as Long - getCurrent?.toLong() as Long), (TimeUnit.MILLISECONDS.toSeconds(finalTime?.toLong() - getCurrent?.toLong()) - TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime?.toLong() - getCurrent?.toLong() ))) % 60))
                seekbar?.setProgress(getCurrent?.toInt() as Int)

                Handler().postDelayed(this,1000)
            }
        }

    }
    object Staticated{
        var MY_PREFS_SHUFFLE = "Shuffle"
        var MY_PREFS_LOOP = "loop"


        fun playNext(check: String) {
            if(check.equals("Normal",true)){
                Statified.currentPosition += 1
                if(Statified.currentPosition == Statified.fetchSongs?.size)
                    Statified.currentPosition = 0
            }
            else if(check.equals("Shuffle",true)){
                var randomObject = Random()
                Statified.currentPosition = randomObject.nextInt(Statified.fetchSongs?.size?.plus(1) as Int)
            }

            Statified.currentSongHelper?.isLoop = false
            val nextSong = Statified.fetchSongs?.get(Statified.currentPosition)
            Statified.currentSongHelper?.songTitle = nextSong?.songTitle
            Statified.currentSongHelper?.songArtist = nextSong?.artist
            Statified.currentSongHelper?.songPath = nextSong?.songData
            Statified.currentSongHelper?.currentPosition = Statified.currentPosition
            Statified.currentSongHelper?.songId = nextSong?.songID as Long
            updateTextViews(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)


            Statified.mediaplayer?.reset()

            Statified.mediaplayer?.setDataSource(Statified.myActivity, Uri.parse(Statified.currentSongHelper?.songPath))
            Statified.mediaplayer?.prepare()
            Statified.mediaplayer?.start()
            processInformation(Statified.mediaplayer as MediaPlayer)
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.ic_pause_circle_filled_black)

            if(Statified.favouriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean){
                Statified.fab?.setBackgroundResource(R.drawable.ic_favorite_red)
            }
            else{
                Statified.fab?.setBackgroundResource(R.drawable.ic_not_favourite)
            }

        }

        fun playPrevious() {
            Statified.currentPosition -= 1
            if(Statified.currentPosition == -1)
                Statified.currentPosition = (Statified.fetchSongs?.size as Int - 1)

            if(Statified.currentSongHelper?.isPlaying as Boolean){
                Statified.playPauseImageButton?.setBackgroundResource(R.drawable.ic_pause_circle_filled_black)
            }
            else{
                Statified.playPauseImageButton?.setBackgroundResource(R.drawable.ic_play_circle_filled_black)
            }

            val nextSong = Statified.fetchSongs?.get(Statified.currentPosition)
            Statified.currentSongHelper?.songTitle = nextSong?.songTitle
            Statified.currentSongHelper?.songArtist = nextSong?.artist
            Statified.currentSongHelper?.songPath = nextSong?.songData
            Statified.currentSongHelper?.currentPosition = Statified.currentPosition
            Statified.currentSongHelper?.songId = nextSong?.songID as Long
            updateTextViews(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)


            Statified.mediaplayer?.reset()

            Statified.mediaplayer?.setDataSource(Statified.myActivity, Uri.parse(Statified.currentSongHelper?.songPath))
            Statified.mediaplayer?.prepare()
            Statified.mediaplayer?.start()
            processInformation(Statified.mediaplayer as MediaPlayer)

            if(Statified.favouriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean){
                Statified.fab?.setBackgroundResource(R.drawable.ic_favorite_red)
            }
            else{
                Statified.fab?.setBackgroundResource(R.drawable.ic_not_favourite)
            }
        }

        fun onSongComplete(){
            if(Statified.currentSongHelper?.isShuffle as Boolean){
                playNext("Shuffle")
                Statified.currentSongHelper?.isPlaying= true
            }
            else{
                if(Statified.currentSongHelper?.isLoop as Boolean){
                    Statified.currentSongHelper?.isPlaying = true
                    val nextSong = Statified.fetchSongs?.get(Statified.currentPosition)
                    Statified.currentSongHelper?.songTitle = nextSong?.songTitle
                    Statified.currentSongHelper?.songArtist = nextSong?.artist
                    Statified.currentSongHelper?.songPath = nextSong?.songData
                    Statified.currentSongHelper?.currentPosition = Statified.currentPosition
                    Statified.currentSongHelper?.songId = nextSong?.songID as Long
                    updateTextViews(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)


                    Statified.mediaplayer?.reset()

                    Statified.mediaplayer?.setDataSource(Statified.myActivity, Uri.parse(Statified.currentSongHelper?.songPath))
                    Statified.mediaplayer?.prepare()
                    Statified.mediaplayer?.start()
                    processInformation(Statified.mediaplayer as MediaPlayer)

                }
                else{
                    playNext("Normal")
                    Statified.currentSongHelper?.isPlaying = true
                }
            }

            if(Statified.favouriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean){
                Statified.fab?.setBackgroundResource(R.drawable.ic_favorite_red)
            }
            else{
                Statified.fab?.setBackgroundResource(R.drawable.ic_not_favourite)
            }
        }

        fun updateTextViews(_songTitle: String, _songArtist: String){
            Statified.songTitleView?.setText(_songTitle)
            Statified.songArtistView?.setText(_songArtist)
        }

        fun processInformation(mediaPlayer: MediaPlayer){
            val finalTime = mediaPlayer.duration
            val startTime = mediaPlayer.currentPosition
            Statified.seekbar?.max = finalTime
            Statified.startTimeText?.setText(String.format("%d:%2d", TimeUnit.MILLISECONDS.toMinutes(startTime?.toLong() as Long), (TimeUnit.MILLISECONDS.toSeconds(startTime?.toLong() as Long) - TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime?.toLong() as Long))) % 60))
            Statified.seekbar?.setProgress(startTime)

            Handler().postDelayed(Statified.updateSongTime,1000)
        }
    }


    var mAcceleration: Float = 0f
    var mAccelerationCurrent: Float = 0f
    var mAccelearationLast: Float = 0f

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_song_playing, container, false)
        setHasOptionsMenu(true)
        activity.title = "now playing"

        Statified.seekbar = view?.findViewById(R.id.seekBar)
        Statified.songArtistView = view?.findViewById(R.id.songArtist)
        Statified.songTitleView = view?.findViewById(R.id.songTitle)
        Statified.shuffleImageButton = view?.findViewById(R.id.shuffleButton)
        Statified.loopImageButton = view?.findViewById(R.id.loopButton)
        Statified.playPauseImageButton = view?.findViewById(R.id.playPauseButton)
        Statified.nextImageButton = view?.findViewById(R.id.nextButton)
        Statified.previousImageButton = view?.findViewById(R.id.previousButton)
        Statified.startTimeText = view?.findViewById(R.id.startTime)
        Statified.endTimeText = view?.findViewById(R.id.endTime)
        Statified.fab = view?.findViewById(R.id.favouritesButton)
        Statified.fab?.alpha = 0.8f

        Statified.glView = view?.findViewById(R.id.visualizer_view)

        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Statified.audioVisualizer = Statified.glView as AudioVisualization
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        Statified.myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        Statified.myActivity = activity
    }

    override fun onResume() {
        super.onResume()
        Statified.audioVisualizer?.onResume()
        Statified.mSensorManager?.registerListener(Statified.mSensorListener, Statified.mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        Statified.audioVisualizer?.onPause()

        Statified.mSensorManager?.unregisterListener(Statified.mSensorListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Statified.audioVisualizer?.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Statified.mSensorManager = Statified.myActivity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAcceleration = 0.0f
        mAccelerationCurrent = SensorManager.GRAVITY_EARTH
        mAccelearationLast = SensorManager.GRAVITY_EARTH

        bindShakeListener()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.song_playing_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item: MenuItem? = menu?.findItem(R.id.action_redirect)
        item?.isVisible = true

        val item2: MenuItem? = menu?.findItem(R.id.action_sort)
        item2?.isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_redirect -> {
                Statified.myActivity?.onBackPressed()
                return false
            }
        }
        return false
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Statified.favouriteContent = EchoDatabase(Statified.myActivity)

        Statified.currentSongHelper = CurrentSongHelper()
        Statified.currentSongHelper?.isPlaying = true
        Statified.currentSongHelper?.isLoop = false
        Statified.currentSongHelper?.isShuffle = false

        var path: String? = null
        var _songTitle: String? = null
        var _songArtist: String? = null
        var songId: Long = 0

        path = arguments.getString("path")
        _songTitle = arguments.getString("songTitle")
        _songArtist = arguments.getString("songArtist")
        songId = arguments.getInt("songId").toLong()
        Statified.currentPosition = arguments.getInt("songPosition")
        Statified.fetchSongs = arguments.getParcelableArrayList("songData")

        Statified.currentSongHelper?.songPath = path
        Statified.currentSongHelper?.songArtist = _songArtist
        Statified.currentSongHelper?.songTitle = _songTitle
        Statified.currentSongHelper?.songId = songId
        Statified.currentSongHelper?.currentPosition = Statified.currentPosition
        Staticated.updateTextViews(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)

        var fromFavBottomBar = arguments.get("FavBottomBar") as? String
        var fromMainScreenBottomBar = arguments.get("MainScreenBottomBar") as? String
        if(fromFavBottomBar != null){
            Statified.mediaplayer?.reset()
            Statified.mediaplayer = FavouritesFragment.Statified.mediaPlayer
        } else if (fromMainScreenBottomBar != null) {
            Statified.mediaplayer?.reset()
            Statified.mediaplayer = MainScreenFragment.Statified.mediaPlayer
        }else{
            Statified.mediaplayer = MediaPlayer()
            Statified.mediaplayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            Statified.mediaplayer?.stop()
            Statified.mediaplayer?.reset()
            Statified.mediaplayer?.setDataSource(Statified.myActivity, Uri.parse(path))
            Statified.mediaplayer?.prepare()
            Statified.mediaplayer?.start()
        }

        Staticated.processInformation(Statified.mediaplayer as MediaPlayer)

        if(Statified.currentSongHelper?.isPlaying as Boolean){
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.ic_pause_circle_filled_black)
        }
        else{
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.ic_play_circle_filled_black)
        }

        Statified.mediaplayer?.setOnCompletionListener {
            Staticated.onSongComplete()
        }
        clickHandler()
        seekbarHandler()

        val visualizationHandler = DbmHandler.Factory.newVisualizerHandler(Statified.myActivity as Context, 0)
        Statified.audioVisualizer?.linkTo(visualizationHandler)


        var prefsForShuffle = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)
        var isShuffleAllow = prefsForShuffle?.getBoolean("feature",false)
        if(isShuffleAllow as Boolean){
            Statified.currentSongHelper?.isShuffle = true
            Statified.currentSongHelper?.isLoop = false
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.ic_shuffle_gold)
            Statified.loopImageButton?.setBackgroundResource(R.drawable.ic_loop_white)
        }
        else{
            Statified.currentSongHelper?.isShuffle = false
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.ic_shuffle_white)
        }

        var prefsForLoop = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)
        var isLoopAllow = prefsForLoop?.getBoolean("feature",false)
        if(isLoopAllow as Boolean){
            Statified.currentSongHelper?.isShuffle = false
            Statified.currentSongHelper?.isLoop = true
            Statified.loopImageButton?.setBackgroundResource(R.drawable.ic_loop_gold)
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.ic_shuffle_white)
        }
        else{
            Statified.currentSongHelper?.isLoop = false
            Statified.loopImageButton?.setBackgroundResource(R.drawable.ic_loop_white)

        }

        if(Statified.favouriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean){
            Statified.fab?.setBackgroundResource(R.drawable.ic_favorite_red)
        }
        else{
            Statified.fab?.setBackgroundResource(R.drawable.ic_not_favourite)
        }
    }

    fun clickHandler(){

        Statified.fab?.setOnClickListener({
            if(Statified.favouriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean){
                Statified.fab?.setBackgroundResource(R.drawable.ic_not_favourite)
                Statified.favouriteContent?.deleteFavourite(Statified.currentSongHelper?.songId?.toInt() as Int)
                Toast.makeText(Statified.myActivity,"Removed from Favourites",Toast.LENGTH_SHORT).show()
            }
            else{
                Statified.fab?.setBackgroundResource(R.drawable.ic_favorite_red)
                Statified.favouriteContent?.storeAsFavourite(Statified.currentSongHelper?.songId?.toInt(), Statified.currentSongHelper?.songArtist, Statified.currentSongHelper?.songTitle, Statified.currentSongHelper?.songPath)
                Toast.makeText(Statified.myActivity, "Made as a Favourite!", Toast.LENGTH_SHORT).show()

            }
        })

        Statified.shuffleImageButton?.setOnClickListener({
            var editorShuffle = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)?.edit()
            var editorLoop = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)?.edit()
            if(Statified.currentSongHelper?.isShuffle as Boolean){
                Statified.currentSongHelper?.isShuffle = false
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.ic_shuffle_white)
                editorShuffle?.putBoolean("feature",false)
                editorShuffle?.apply()
            }
            else{
                Statified.currentSongHelper?.isShuffle = true
                Statified.currentSongHelper?.isLoop = false
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.ic_shuffle_gold)
                Statified.loopImageButton?.setBackgroundResource(R.drawable.ic_loop_white)
                editorShuffle?.putBoolean("feature",true)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature",false)
                editorLoop?.apply()
            }
        })

        Statified.nextImageButton?.setOnClickListener({
            Statified.currentSongHelper?.isPlaying = true
            if(Statified.currentSongHelper?.isShuffle as Boolean){
                Staticated.playNext("Shuffle")
            }
            else{
                Staticated.playNext("Normal")
            }
        })

        Statified.previousImageButton?.setOnClickListener({
            Statified.currentSongHelper?.isPlaying = true
            Staticated.playPrevious()
        })

        Statified.loopImageButton?.setOnClickListener({
            var editorShuffle = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)?.edit()
            var editorLoop = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)?.edit()
            if(Statified.currentSongHelper?.isLoop as Boolean){
                Statified.currentSongHelper?.isLoop = false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.ic_loop_white)
                editorLoop?.putBoolean("feature",false)
                editorLoop?.apply()
            }else{
                Statified.currentSongHelper?.isLoop = true
                Statified.currentSongHelper?.isShuffle = false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.ic_loop_gold)
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.ic_shuffle_white)
                editorShuffle?.putBoolean("feature",false)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature",true)
                editorLoop?.apply()
            }
        })

        Statified.playPauseImageButton?.setOnClickListener({
            if(Statified.currentSongHelper?.isPlaying as Boolean){
                Statified.mediaplayer?.pause()
                Statified.currentSongHelper?.isPlaying = false
                Statified.playPauseImageButton?.setBackgroundResource(R.drawable.ic_play_circle_filled_black)
            }
            else{
                Statified.mediaplayer?.start()
                Statified.currentSongHelper?.isPlaying = true
                Statified.playPauseImageButton?.setBackgroundResource(R.drawable.ic_pause_circle_filled_black)
            }
        })


    }

    fun bindShakeListener(){
        Statified.mSensorListener = object: SensorEventListener{
            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

            }

            override fun onSensorChanged(p0: SensorEvent) {
                val x = p0.values[0]
                val y = p0.values[1]
                val z = p0.values[2]

                mAccelearationLast = mAccelerationCurrent
                mAccelerationCurrent = Math.sqrt(((x*x + y*y + z*z).toDouble())).toFloat()
                val delta = mAccelerationCurrent - mAccelearationLast
                mAcceleration = mAcceleration * 0.9f + delta

                if(mAcceleration > 12){
                    val prefs = Statified.myActivity?.getSharedPreferences(Statified.MY_PREFS_NAME, Context.MODE_PRIVATE)
                    val isAllowed = prefs?.getBoolean("feature", false)
                    if(isAllowed as Boolean){
                        Staticated.playNext("Normal")
                    }
                }
            }


        }
    }

    fun seekbarHandler() {
        val seekbarListener = SeekBarController()
        Statified.seekbar?.setOnSeekBarChangeListener(seekbarListener)
    }

}
