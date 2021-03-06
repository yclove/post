package com.ycengine.post.main

import android.arch.lifecycle.MutableLiveData
import com.ycengine.post.common.PostException
import com.ycengine.post.data.model.*
import com.ycengine.post.repository.remote.BaseViewModel
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class SplashViewModel : BaseViewModel() {

    val appVersionModel: MutableLiveData<AppVersionModel> = MutableLiveData()

    init {
    }

    fun getAppVersionData() {
        Flowable.just(Any())
            .subscribeOn(Schedulers.io())
            .map {
                remoteRepository.getAppVersion()
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                appVersionModel.value = it
            }, {
                if (it is PostException) {
                    postException.value = it
                }
                it.printStackTrace()
            }).apply {
                compositeDisposable.add(this)
            }
    }

    fun clearPostData() {
        try {
            databaseRepository.clearPostData()
            Timber.e("Post data 초기화")
        } catch (e: Exception) {
            Timber.e("Post data 초기화에 실패하였습니다. (${e.message})")
        }
    }

    fun insertPostColorData(colors: List<ColorModel>) {
        databaseRepository.insertColors(colors)
    }

    fun insertHashPopKeyword(keywords: List<HashPopKeywordModel>) {
        databaseRepository.insertHashPopKeyword(keywords)
    }

    fun insertPostPopKeyword(keywords: List<PostPopKeywordModel>) {
        databaseRepository.insertPostPopKeyword(keywords)
    }

    fun insertMusPopKeyword(keywords: List<MusPopKeywordModel>) {
        databaseRepository.insertMusPopKeyword(keywords)
    }
}