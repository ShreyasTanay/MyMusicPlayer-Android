package com.example.android.mymusicplayer.activities

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.RelativeLayout
import com.example.android.mymusicplayer.R
import com.example.android.mymusicplayer.fragments.*

class MainActivity : AppCompatActivity() {

    var audioManager: AudioManager? = null

    object Statified{
        var drawerLayout: DrawerLayout? = null      // static variable of type "DrawerLayout"
        var notificationMAnager : NotificationManager? = null
    }

    var trackNotificationBuilder : Notification? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        MainActivity.Statified.drawerLayout = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(this@MainActivity, Statified.drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        Statified.drawerLayout?.setDrawerListener(toggle)
        toggle.syncState()

        val mainScreenFragment = MainScreenFragment()
        this.supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment1, mainScreenFragment, "MainScreenFragment")
                .commit()




        val allSongsOpen: RelativeLayout = findViewById(R.id.all_songs)
        allSongsOpen.setOnClickListener(View.OnClickListener {
            val mainScreenFragment = MainScreenFragment()
            this.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment1, mainScreenFragment)
                    .commit()
            Statified.drawerLayout?.closeDrawers()
        })
        val favouritesOpen: RelativeLayout = findViewById(R.id.favourites)
        favouritesOpen.setOnClickListener(View.OnClickListener {
            val favouritesFragment = FavouritesFragment()
            this.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment1, favouritesFragment)
                    .commit()
            Statified.drawerLayout?.closeDrawers()
        })
        val settingsOpen: RelativeLayout = findViewById(R.id.settings)
        settingsOpen.setOnClickListener(View.OnClickListener {
            val settingsFragment = SettingsFragment()
            this.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment1, settingsFragment)
                    .commit()
            Statified.drawerLayout?.closeDrawers()
        })
        val aboutMeOpen: RelativeLayout = findViewById(R.id.about_me)
        aboutMeOpen.setOnClickListener(View.OnClickListener {
            val aboutUsFragment = AboutUsFragment()
            this.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment1, aboutUsFragment)
                    .commit()
            Statified.drawerLayout?.closeDrawers()
        })

        val intent = Intent(this@MainActivity, MainActivity::class.java)
        val preIntent = PendingIntent.getActivity(this@MainActivity, System.currentTimeMillis().toInt() as Int, intent, 0)

        trackNotificationBuilder = Notification.Builder(this)
                .setContentTitle("You are listening to music!!")
                .setSmallIcon(R.drawable.appicon)
                .setContentIntent(preIntent)
                .setOngoing(true)
                .setAutoCancel(true)
                .build()

        Statified.notificationMAnager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStart() {
        try{
            Statified.notificationMAnager?.cancel(1111)
        }catch (e : Exception){
            e.printStackTrace()
        }
        super.onStart()
    }

    override fun onResume() {
        try{
            Statified.notificationMAnager?.cancel(1111)
        }catch (e : Exception){
            e.printStackTrace()
        }
        super.onResume()
    }

    override fun onStop() {
        try{
            if(SongPlayingFragment.Statified.mediaplayer?.isPlaying as Boolean){
                Statified.notificationMAnager?.notify(1111, trackNotificationBuilder)
            }
        }catch(e : Exception) {
            e.printStackTrace()
        }
        super.onStop()
    }

    override fun onDestroy() {
        try{
            if(SongPlayingFragment.Statified.mediaplayer?.isPlaying as Boolean){
                Statified.notificationMAnager?.notify(1111, trackNotificationBuilder)
            }
        }catch(e : Exception) {
            e.printStackTrace()
        }

        super.onDestroy()
    }


}
