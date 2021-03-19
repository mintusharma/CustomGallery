package com.androidcenter.gallery

import android.os.Bundle
import android.widget.Toast
import com.androidcenter.gallery.base.BaseActivity

class ImageDetailsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_details)
        var data=intent.getStringExtra("data")

        Toast.makeText(this, data, Toast.LENGTH_SHORT).show()

    }
}