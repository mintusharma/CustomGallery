package com.androidcenter.gallery.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.androidcenter.gallery.base.BaseActivity
import com.androidcenter.gallery.library.R
import com.androidcenter.gallery.model.*
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_gallery.*
import org.koin.androidx.viewmodel.ext.android.viewModel

const val REQUEST_SELECT_MEDIA = 88
private const val KEY_ALBUM_SETTING = "albumSetting"

class GalleryEngine : BaseActivity() {

    private val viewModel by viewModel<GalleryViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        viewModel.setting = intent.getSerializableExtra(KEY_ALBUM_SETTING) as AlbumSetting
        RxPermissions(this).request(Manifest.permission.READ_EXTERNAL_STORAGE)
            .subscribe {
                supportFragmentManager.beginTransaction()
                    .add(
                        R.id.mediaContainer,
                        MediaSelectFragment.newInstance()
                    )
                    .commit()
                setClickListener()
                subscribeChanges()
            }.apply { addDisposable(this) }

        buttonCancel.setOnClickListener { onBackPressed() }

        buttonFinish.visibility = if (true == viewModel.setting?.multipleSelection) View.VISIBLE else View.GONE
    }

    private fun setClickListener() {
        textAlbumName.setOnCheckedChangeListener { _, isChecked ->
            val transaction = supportFragmentManager.beginTransaction()

            if (isChecked) {
                transaction
                    .add(
                        R.id.mediaContainer,
                        AlbumSelectFragment.newInstance()
                    )
            } else {
                supportFragmentManager.findFragmentById(R.id.mediaContainer)?.run {
                    transaction.remove(this)
                }
            }
            transaction.commit()
            supportFragmentManager.executePendingTransactions()
        }
        arrow.setOnClickListener { textAlbumName.toggle() }

        buttonFinish.setOnClickListener {
            viewModel.multipleSelectMedia.value?.let {
                returnSelectMediaList(it)
            }
        }
    }

    private fun subscribeChanges() {
        viewModel.currentAlbumItem
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                val albumName = if (it.name == ALL_MEDIA_ALBUM_NAME) textAlbumName.context.getString(
                    R.string.total) else it.name
                textAlbumName.textOff = albumName
                textAlbumName.textOn = albumName
                textAlbumName.isChecked = false
            }.subscribe()
            .apply { addDisposable(this) }


        viewModel.multipleSelectMedia
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { buttonFinish.text = resources.getString(R.string.finish, it.size) }
            .subscribe()
            .apply { addDisposable(this) }
    }

    private fun returnSelectMediaList(list: List<Media>) {
        setResult(Activity.RESULT_OK, Intent().apply {
            putStringArrayListExtra(KEY_MEDIA_LIST, ArrayList<String>(list.map { it.path }))
        })
        finish()
    }

    class Builder {
        private val setting = AlbumSetting()

        fun multiple(multipleSelect: Boolean = true): Builder {
            setting.multipleSelection = multipleSelect
            return this
        }

        fun maxSelect(max: Int): Builder {
            check(max > 0) { "The maximum selection should be greater than 0." }
            setting.maxSelection = max
            return this
        }


        fun forResult(activity: Activity, requestCode: Int = REQUEST_SELECT_MEDIA) {
            activity.startActivityForResult(getIntent(activity), requestCode)
        }


        private fun getIntent(context: Context) =
            Intent(context, GalleryEngine::class.java).apply {
                putExtra(KEY_ALBUM_SETTING, setting)
            }
    }

    companion object {

        fun getSelectMediaPaths(intent: Intent?): List<String> {
            return intent?.getStringArrayListExtra(KEY_MEDIA_LIST) ?: listOf()
        }
    }
}
