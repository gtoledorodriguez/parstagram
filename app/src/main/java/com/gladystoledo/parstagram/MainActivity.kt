package com.gladystoledo.parstagram

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.gladystoledo.parstagram.fragments.ComposeFragment
import com.gladystoledo.parstagram.fragments.FeedFragment
import com.gladystoledo.parstagram.fragments.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.parse.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


/**
 * Let user create a post by taking a photo with their camera
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fragmentManager: FragmentManager = supportFragmentManager

        // define your fragments here
//        val fragment1: Fragment = FirstFragment()
//        val fragment2: Fragment = SecondFragment()
//        val fragment3: Fragment = ThirdFragment()

        var bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener {
            item ->
            var fragmentToShow: Fragment? = null

            when(item.itemId){
                R.id.action_home ->{
                    fragmentToShow = FeedFragment()
                }
                R.id.action_compose->{
                    fragmentToShow = ComposeFragment()
                }
                R.id.action_profile->{
                    fragmentToShow = ProfileFragment()
                }
            }
            if(fragmentToShow != null) {
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragmentToShow)
                    .commit()
            }
            //return true to say that we've handled this user interaction on the itmem
            true
        }

        // Set default selection
        bottomNavigationView.selectedItemId = R.id.action_home

        //queryPosts()
    }





    companion object{
        const val TAG = "MainActivity"
    }
    object BitmapScaler {
        // Scale and maintain aspect ratio given a desired width
        // BitmapScaler.scaleToFitWidth(bitmap, 100)
        fun scaleToFitWidth(b: Bitmap, width: Int): Bitmap {
            val factor = width / b.width.toFloat()
            return Bitmap.createScaledBitmap(b, width, (b.height * factor).toInt(), true)
        }
        // Scale and maintain aspect ratio given a desired height
        // BitmapScaler.scaleToFitHeight(bitmap, 100)
        fun scaleToFitHeight(b: Bitmap, height: Int): Bitmap {
            val factor = height / b.height.toFloat()
            return Bitmap.createScaledBitmap(b, (b.width * factor).toInt(), height, true)
        } // ...
    }

}