package com.pixman

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.util.Pair
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_operation.*
import java.io.ByteArrayOutputStream


class MainActivity : AppCompatActivity() {

    private var snackbar: Snackbar? = null
    private var bitmap: Bitmap? = null              //Original Bitmap
    private val map = ArrayList<Bitmap?>()          // List Of Bitmap
    private lateinit var parent: LinearLayout
    private var isOpacityAdded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        parent = findViewById(R.id.parent)

        // Image Picker From Gallery
        materialButton.setOnClickListener {
            picImage()
        }

        // Preview Image Changes
        previewButton.setOnClickListener {
            moveToNext()
        }

        // Flip Image vertically
        flip_vertical.setOnClickListener {
            image.setImageBitmap(utils().verticalFlip(map[map.size - 1], map))
        }

        // Flip Image Horizontally
        flip_horizontal.setOnClickListener {
            image.setImageBitmap(utils().horizontalFlip(map[map.size - 1], map))
        }

        // Add Opacity to 50%
        opacity.setOnClickListener {
            // ADD OPACITY ONLY ONCE
            if (!isOpacityAdded) {
                image.setImageBitmap(utils.setOpacity(map[map.size - 1], map))
                isOpacityAdded = true
            }
        }

        // Add Green Text to the center of image
        add_text.setOnClickListener {
            //            greedy_text.visibility = View.VISIBLE
            image.setImageBitmap(utils.addTextOnBitmap(map[map.size - 1], this, map))
        }

        // Save Image To Local Storage -> DCIM Folder -> Pixman Folder -> Image file
        save.setOnClickListener {
            val result = utils.saveImage(map[map.size - 1])
            // Result Will Tell if Image is saved successfully.
            if (result) {
                Toast.makeText(this, "Image Saved", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, SplashActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Image Saved Failed", Toast.LENGTH_SHORT).show()
            }
        }

        // Undo Last Operations until reach to original Position
        undo.setOnClickListener {
            if (map.size > 1) {
                map.remove(map[map.size - 1])
                image.setImageBitmap(map[map.size - 1])
            }
        }
    }

    // Method to Move Image Preview Screen
    private fun moveToNext() {
        val pairs: Array<Pair<View, String>?> =
            arrayOfNulls(1)
        pairs[0] = Pair(image, "imageTransition")

        val options =
            ActivityOptions.makeSceneTransitionAnimation(this, *pairs)

        val stream = ByteArrayOutputStream()
        map[map.size - 1]?.compress(Bitmap.CompressFormat.JPEG, 50, stream)
        val byteArray: ByteArray = stream.toByteArray()

        try {
            val intent = Intent(this, ImagePreview::class.java)
            intent.putExtra("image", byteArray);
            startActivity(intent, options.toBundle())
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }

    }

    // Method to Pick Image from Gallery
    private fun picImage() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val i = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(i, Constants.PICK_IMAGE_REQUEST)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                Constants.STROAGE_REQUEST
            )
        }
    }


    // To Check Storage Permission is Given Or Not
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            Constants.STROAGE_REQUEST -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // Permission Denied
                    showSnackbar()
                } else {
                    materialButton.performClick()

                }
            }
        }
    }


    // Show Snackbar if Permission is not granted
    private fun showSnackbar() {
        snackbar = Snackbar.make(
            parent,
            resources.getString(R.string.message_no_storage_permission_snackbar),
            Snackbar.LENGTH_INDEFINITE
        )
        snackbar!!.setAction(resources.getString(R.string.settings)) {
            val packageUri =
                Uri.fromParts("package", applicationContext.packageName, null)

            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = packageUri
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        snackbar!!.show()
    }


    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.PICK_IMAGE_REQUEST) { //TODO: action
            val selectedImage: Uri? = data?.data
            Log.e("Select Image uri ", selectedImage.toString());
//            image.setImageURI(selectedImage)

            val filePathColumn =
                arrayOf(MediaStore.Images.Media.DATA)
            // Get the cursor
            val cursor: Cursor? =
                contentResolver.query(selectedImage!!, filePathColumn, null, null, null)
            // Move to first row
            cursor?.moveToFirst()
            //Get the column index of MediaStore.Images.Media.DATA
            val columnIndex: Int? = cursor?.getColumnIndex(filePathColumn[0])
            //Gets the String value in the column
            val imgDecodableString: String? = cursor?.getString(columnIndex!!)
            cursor?.close()
            // Set the Image in ImageView after decoding the String
            menu_layout.visibility = View.GONE
            operation_layout.visibility = View.VISIBLE
            bitmap = BitmapFactory.decodeFile(imgDecodableString)
            image.setImageBitmap(bitmap)

            map.add(bitmap)


        }
    }


    // Check if permission is given or not. If permision Granted, dismiss Snackbar.
    override fun onResume() {
        super.onResume()
        if (snackbar != null) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                snackbar!!.dismiss()
            } else {
                snackbar!!.show()
            }
        }
    }
}
