package com.androidcenter.gallery.model

import android.content.Context
import android.provider.MediaStore
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.io.File

interface AlbumRepo {
    fun fetchAlbums(setting: AlbumSetting? = null): Completable
    fun getAlbums(setting: AlbumSetting? = null): BehaviorSubject<List<AlbumItem>>
    fun getAlbumItem(name: String, setting: AlbumSetting? = null): Observable<AlbumItem>
    fun getAlbumItemSync(name: String, settings: AlbumSetting? = null): AlbumItem?
}

class AlbumRepoImpl(private val context: Context) : AlbumRepo {

    private val albumSubject = BehaviorSubject.create<List<AlbumItem>>()
    private val albumItemMapping = mutableMapOf<String, AlbumItem>()

    override fun fetchAlbums(setting: AlbumSetting?): Completable {
        return Completable.fromAction {
            fetchAlbumSync(setting)
        }
    }

    private fun fetchAlbumSync(setting: AlbumSetting?) {
        albumItemMapping.clear()
        val contentUri = MediaStore.Files.getContentUri("external")
        val selection =
            "(${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR " +
                    "${MediaStore.Files.FileColumns.MEDIA_TYPE}=?) AND " +
                    "${MediaStore.MediaColumns.SIZE} > 0"
        val selectionArgs =
            arrayOf(
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString()
            )
        val projections =
            arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.DATE_MODIFIED,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.WIDTH,
                MediaStore.MediaColumns.HEIGHT,
                MediaStore.MediaColumns.SIZE
            )

        val sortBy = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
        val cursor =
            context.contentResolver.query(contentUri, projections, selection, selectionArgs, sortBy)
        if (true == cursor?.moveToFirst()) {
            val pathCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
            val bucketNameCol =
                cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val nameCol = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
            val dateCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)
            val mimeType = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
            val sizeCol = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
            val widthCol = cursor.getColumnIndex(MediaStore.MediaColumns.WIDTH)
            val heightCol = cursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT)

            do {
                val path = cursor.getString(pathCol)
                val bucketName = cursor.getString(bucketNameCol)
                val name = cursor.getString(nameCol)
                val dateTime = cursor.getLong(dateCol)
                val type = cursor.getString(mimeType)
                val size = cursor.getLong(sizeCol)
                val width = cursor.getInt(widthCol)
                val height = cursor.getInt(heightCol)

                if (path.isNullOrEmpty() || type.isNullOrEmpty())
                    continue

                val file = File(path)
                if (!file.exists() || !file.isFile)
                    continue

                if (MimeType.IMAGE == setting?.mimeType &&
                    !type.startsWith(MimeType.IMAGE.toString())
                )
                    continue

                if (type.startsWith(MimeType.IMAGE.toString())) {
                    if (null != setting?.imageMaxSize) {
                        if (size > setting.imageMaxSize!!) {
                            continue
                        }
                    }
                }


                val media = Media(
                    path,
                    name,
                    bucketName,
                    size,
                    dateTime,
                    width,
                    height
                )

                if (isEmpty()) {
                    addAlbumItem(ALL_MEDIA_ALBUM_NAME, "", path)
                }
                addMediaToAlbum(ALL_MEDIA_ALBUM_NAME, media)

                val folder = file.parentFile?.absolutePath ?: ""
                addAlbumItem(bucketName, folder, path)
                addMediaToAlbum(bucketName, media)

            } while (cursor.moveToNext())
        }
        cursor?.close()
        albumSubject.onNext(albumItemMapping.values.toList())
    }

    override fun getAlbums(setting: AlbumSetting?): BehaviorSubject<List<AlbumItem>> {
        if (isEmpty()) {
            fetchAlbums(setting)
                .subscribeOn(Schedulers.io())
                .subscribe()
        }
        return albumSubject
    }

    override fun getAlbumItem(name: String, setting: AlbumSetting?): Observable<AlbumItem> {
        if (isEmpty()) {
            fetchAlbums(setting)
                .subscribeOn(Schedulers.io())
                .subscribe()
        }
        return albumSubject.map {
            albumItemMapping[name]
        }
    }

    override fun getAlbumItemSync(name: String, settings: AlbumSetting?): AlbumItem? {
        if (isEmpty())
            fetchAlbumSync(settings)
        return albumItemMapping[name]
    }

    private fun addAlbumItem(name: String, folder: String, coverImagePath: String) {
        albumItemMapping[name] ?: run {
            albumItemMapping[name] =
                AlbumItem(name, folder, coverImagePath)
        }
    }

    private fun addMediaToAlbum(albumName: String, media: Media) {
        albumItemMapping[albumName]?.mediaList?.add(media)
    }

    private fun isEmpty() = albumItemMapping.keys.isEmpty()
}