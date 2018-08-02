package com.example.android.mymusicplayer

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by Shreyas Tanay on 08-01-2018.
 */
class Songs(var songID: Long, var songTitle:  String, var artist: String, var songData: String, var dateAdded: Long) : Parcelable {
    override fun writeToParcel(p0: Parcel?, p1: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    object Statified{
        var nameComparator: Comparator<Songs> = Comparator<Songs>{song1, song2 ->
            val songOne = song1.songTitle.toUpperCase()
            val songTwo = song2.songTitle.toUpperCase()
            songOne.compareTo(songTwo)
        }

        var dateComparator: Comparator<Songs> = Comparator<Songs>{song1, song2 ->
            val dateOne = song1.dateAdded.toDouble()
            val dateTwo = song2.dateAdded.toDouble()
            dateOne.compareTo(dateTwo)
        }


    }
}