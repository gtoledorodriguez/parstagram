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

    val CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034
    val photoFileName = "photo.jpg"
    var photoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //1. Setting the description of the post
        //2. A button to launch the camera to take a picture
        //3. An imageView to show the picture the user has taken
        //4. A button to save and send the post to our Parse Server

        findViewById<Button>(R.id.btnSubmit).setOnClickListener{
            //Send Post to Server
            //Get the description that hey have inputted
            val description = findViewById<EditText>(R.id.description).text.toString()
            val user = ParseUser.getCurrentUser()
            if(photoFile != null){

                submitPost(description, user, photoFile!!)

            }else{
                Log.e(TAG, "There is no picture")
                Toast.makeText(this, "Take a Picture", Toast.LENGTH_SHORT).show()

            }
        }

        findViewById<Button>(R.id.btnTakePicture).setOnClickListener {
            //Launch Camera to let user take picture
            onLaunchCamera()
        }
        findViewById<BottomNavigationView>(R.id.bottom_navigation).setOnItemSelectedListener {
            item ->

            when(item.itemId){
                R.id.action_home ->{
                    //TODO Navigate to home screen
                    Toast.makeText(this,"Home",Toast.LENGTH_SHORT).show()
                }
                R.id.action_compose->{
                    //TODO Navigate to compose screen
                    Toast.makeText(this,"Compose",Toast.LENGTH_SHORT).show()}
                R.id.action_profile->{
                    //TODO Navigate to profile screen
                    Toast.makeText(this,"Profile",Toast.LENGTH_SHORT).show()}
            }
            //return true to say that we've handled this user interaction on the itmem
            true
        }

        findViewById<Button>(R.id.logout_button).setOnClickListener{
            logoutUser()
        }

        //queryPosts()
    }

    //Send Post Object to our Parse Server
    fun submitPost(description: String, user: ParseUser, file: File) {
        val pb = findViewById<ProgressBar>(R.id.pbLoading)
        pb.visibility = ProgressBar.VISIBLE
        //Create Post object
        val post = Post()
        post.setDescription(description)
        post.setUser(user)
        post.setImage(ParseFile(file))
        post.saveInBackground { exception ->
            if(exception != null){
                //Something went wrong
                Toast.makeText(this, "Error while saving post", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Error while saving Post")
                exception.printStackTrace()

            }else{
                Log.i(TAG, "Successfully saved post")
                Toast.makeText(this, "Successfully saved post", Toast.LENGTH_SHORT).show()
                findViewById<EditText>(R.id.description).text.clear()
                findViewById<ImageView>(R.id.imageView).setImageBitmap(null)
                photoFile = null
            }
            pb.visibility = ProgressBar.INVISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                val takenImage = BitmapFactory.decodeFile(photoFile!!.absolutePath)

                val width = findViewById<ImageView>(R.id.imageView).width
                // RESIZE BITMAP, see section below
                val resizedBitmap = BitmapScaler.scaleToFitWidth(takenImage, width);

                // Configure byte output stream
                val bytes = ByteArrayOutputStream()
                // Compress the image further
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes)
                // Create a new file for the resized bitmap (`getPhotoFileUri` defined above)
                val resizedFile = getPhotoFileUri(photoFileName + "_resized")
                resizedFile.createNewFile()
                val fos = FileOutputStream(resizedFile)
                // Write the bytes of the bitmap to file
                fos.write(bytes.toByteArray())
                fos.close()

                // Load the taken image into a preview
                val ivPreview: ImageView = findViewById(R.id.imageView)
                ivPreview.setImageBitmap(resizedBitmap)
            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onLaunchCamera() {
        // create Intent to take a picture and return control to the calling application
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName)

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        if (photoFile != null) {
            val fileProvider: Uri =
                FileProvider.getUriForFile(this, "com.codepath.fileprovider", photoFile!!)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
            // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
            // So as long as the result is not null, it's safe to use the intent.

            // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
            // So as long as the result is not null, it's safe to use the intent.
            if (intent.resolveActivity(packageManager) != null) {
                // Start the image capture intent to take photo
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE)
            }
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    fun getPhotoFileUri(fileName: String): File {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        val mediaStorageDir =
            File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG)

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "failed to create directory")
        }

        // Return the file target for the photo based on filename
        return File(mediaStorageDir.path + File.separator + fileName)
    }

    //Query for all posts in our server
    fun queryPosts() {
        // Specify which class to query
        val query: ParseQuery<Post> = ParseQuery.getQuery(Post::class.java)

        query.include(Post.KEY_USER)
        query.findInBackground(object: FindCallback<Post>{
            //Find all Post Objects
            override fun done(posts: MutableList<Post>?, e: ParseException?) {
                if (e != null){
                    //Something has gone wrong
                    Log.e(TAG, "Error fetching posts")
                }else{
                    if (posts != null){
                        for (post in posts){
                            Log.i(TAG, "Post: " + post.getDescription() + ", username: "
                            + post.getUser()?.username)
                        }
                    }
                }
            }
        })
    }

    private fun logoutUser(){
        ParseUser.logOut()
        val currentUser = ParseUser.getCurrentUser() // this will now be null
        Log.i(TAG, "Logout, Current User: " + currentUser)
        goToLoginActivity()
    }

    private fun goToLoginActivity(){
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
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