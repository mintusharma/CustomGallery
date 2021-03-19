package com.androidcenter.gallery.di

import com.androidcenter.gallery.model.AlbumRepo
import com.androidcenter.gallery.model.AlbumRepoImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repoModule = module {
    single<AlbumRepo> {
        AlbumRepoImpl(
            androidContext()
        )
    }
}