package com.androidcenter.gallery.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidcenter.gallery.base.BaseFragment
import com.androidcenter.gallery.library.R
import com.androidcenter.gallery.ui.AlbumAdapter
import com.androidcenter.gallery.ui.GalleryViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_album_select.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class AlbumSelectFragment : BaseFragment() {

    private val viewModel by sharedViewModel<GalleryViewModel>()
    private val albumAdapter: AlbumAdapter by lazy {
        AlbumAdapter {
            viewModel.selectAlbum(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_album_select, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with (listAlbum) {
            layoutManager = LinearLayoutManager(listAlbum.context, LinearLayoutManager.VERTICAL, false)
            adapter = albumAdapter
        }

        viewModel.getAlbums()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                albumAdapter.albumList.addAll(it)
                albumAdapter.notifyDataSetChanged()
            }
            .subscribe()
            .apply { addDisposable(this) }
    }

    companion object {
        fun newInstance(): Fragment = AlbumSelectFragment()
    }
}