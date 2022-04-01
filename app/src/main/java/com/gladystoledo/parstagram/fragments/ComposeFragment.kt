package com.gladystoledo.parstagram.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.gladystoledo.parstagram.LoginActivity
import com.gladystoledo.parstagram.MainActivity
import com.gladystoledo.parstagram.Post
import com.gladystoledo.parstagram.R
import com.parse.ParseFile
import com.parse.ParseUser
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class ComposeFragment : Fragment() {

    val CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034
    val photoFileName = "photo.jpg"
    var photoFile: File? = null

    lateinit var ivPreview: ImageView
    lateinit var pb: ProgressBar
    lateinit var etDescription: EditText
    lateinit var ivImageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_compose, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Set onClickListeners and setup logic

        ivPreview = view.findViewById(R.id.imageView)

        view.findViewById<Button>(R.id.logout_button).setOnClickListener{
            //logoutUser()
        }

        view.findViewById<Button>(R.id.btnSubmit).setOnClickListener{
            //Send Post to Server
            //Get the description that hey have inputted
            val description = view.findViewById<EditText>(R.id.description).text.toString()
            val user = ParseUser.getCurrentUser()
            if(photoFile != null){
                pb = view.findViewById<ProgressBar>(R.id.pbLoading)
                etDescription = view.findViewById<EditText>(R.id.description)
                ivImageView = view.findViewById<ImageView>(R.id.imageView)

                submitPost(description, user, photoFile!!)

            }else{
                Log.e(MainActivity.TAG, "There is no picture")
                Toast.makeText(requireContext(), "Take a Picture", Toast.LENGTH_SHORT).show()

            }
        }

        view.findViewById<Button>(R.id.btnTakePicture).setOnClickListener {
            //Launch Camera to let user take picture
            onLaunchCamera()
        }
    }

    //Send Post Object to our Parse Server
    fun submitPost(description: String, user: ParseUser, file: File){
        pb.visibility = ProgressBar.VISIBLE
        //Create Post object
        val post = Post()
        post.setDescription(description)
        post.setUser(user)
        post.setImage(ParseFile(file))
        post.saveInBackground { exception ->
            if(exception != null){
                //Something went wrong
                Toast.makeText(requireContext(), "Error while saving post", Toast.LENGTH_SHORT).show()
                Log.e(MainActivity.TAG, "Error while saving Post")
                exception.printStackTrace()

            }else{
                Log.i(MainActivity.TAG, "Successfully saved post")
                Toast.makeText(requireContext(), "Successfully saved post", Toast.LENGTH_SHORT).show()
                etDescription.text.clear()
                ivImageView.setImageBitmap(null)
                photoFile = null
            }
            pb.visibility = ProgressBar.INVISIBLE
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
                FileProvider.getUriForFile(requireContext(), "com.codepath.fileprovider", photoFile!!)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
            // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
            // So as long as the result is not null, it's safe to use the intent.

            // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
            // So as long as the result is not null, it's safe to use the intent.
            if (intent.resolveActivity(requireContext().packageManager) != null) {
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
            File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), MainActivity.TAG)

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(MainActivity.TAG, "failed to create directory")
        }

        // Return the file target for the photo based on filename
        return File(mediaStorageDir.path + File.separator + fileName)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                // by this point we have the camera photo on disk
                val takenImage = BitmapFactory.decodeFile(photoFile!!.absolutePath)

                val width = ivImageView.width
                // RESIZE BITMAP, see section below
                val resizedBitmap = MainActivity.BitmapScaler.scaleToFitWidth(takenImage, width);

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

                ivPreview.setImageBitmap(resizedBitmap)
            } else { // Result was a failure
                Toast.makeText(requireContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show()
            }
        }
    }

//    private fun logoutUser(){
//        ParseUser.logOut()
//        val currentUser = ParseUser.getCurrentUser() // this will now be null
//        Log.i(MainActivity.TAG, "Logout, Current User: " + currentUser)
//        goToLoginActivity()
//    }

//    private fun goToLoginActivity(){
//        val intent = Intent(requireContext(), LoginActivity::class.java)
//        startActivity(intent)
//        finish()
//    }

}