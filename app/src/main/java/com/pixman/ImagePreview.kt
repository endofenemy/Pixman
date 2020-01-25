package com.pixman

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_preview.*


class ImagePreview:AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        val byteArray = intent.getByteArrayExtra("image")
        val bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        image.setImageBitmap(bmp)

    }
}