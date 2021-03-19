package com.androidcenter.gallery

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.androidcenter.gallery.base.BaseActivity
import com.androidcenter.gallery.ui.GalleryEngine
import com.androidcenter.gallery.ui.REQUEST_SELECT_MEDIA
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        RxPermissions(this).request(Manifest.permission.READ_EXTERNAL_STORAGE)
            .subscribe {

                buttonImageSelection.setOnClickListener {
                    GalleryEngine.Builder()
                        .multiple(true)
                        .maxSelect(10)
                        .forResult(this)
                }
            }.apply { addDisposable(this) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECT_MEDIA && resultCode == Activity.RESULT_OK) {
            var text:TextView=findViewById(R.id.imageText)
            var imgFile = File(GalleryEngine.getSelectMediaPaths(data).toString())
            text.text=GalleryEngine.getSelectMediaPaths(data).toString()
            Toast.makeText(this, GalleryEngine.getSelectMediaPaths(data).toString(), Toast.LENGTH_SHORT).show()
        }
    }
}
