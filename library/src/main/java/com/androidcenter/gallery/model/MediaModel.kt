package com.androidcenter.gallery.model

import java.io.Serializable

const val KEY_MEDIA_LIST = "imagelist"

data class Media(
    val path: String,
    var name: String?,
    var album: String?,
    var size: Long?,
    var datetime: Long?,
    var width: Int?,
    var height: Int?
) : Serializable

enum class MimeType(private val typeName: String) {
    ALL("all"),
    IMAGE("image");

    override fun toString() = typeName
}