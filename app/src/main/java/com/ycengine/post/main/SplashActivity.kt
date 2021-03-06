package com.ycengine.post.main

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.ycengine.post.BuildConfig
import com.ycengine.post.R
import com.ycengine.post.common.Constants
import com.ycengine.post.common.base.BaseActivity
import com.ycengine.post.databinding.ActivitySplashBinding
import com.ycengine.post.util.DeviceUtil
import com.ycengine.post.util.SPUtil
import com.ycengine.post.util.handler.IOnHandlerMessage
import com.ycengine.post.util.handler.WeakRefHandler
import com.ycengine.post.widget.TextureVideoView
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class SplashActivity : BaseActivity(), IOnHandlerMessage, View.OnClickListener {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var viewModel: SplashViewModel

    private val mHandler by lazy {
        WeakRefHandler(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)

        initViewModel()
        initLayout()
    }

    override fun onDestroy() {
        super.onDestroy()
        textureVideoView?.apply {
            stop()
        }
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders
            .of(this)
            .get(SplashViewModel::class.java)

        viewModel.appVersionModel.observe(this, Observer {
            it?.let { appVersionModel ->
                if (SPUtil.getIntSharedPreference(this, Constants.SP_LATEST_APP_VERSION) != appVersionModel.APP_VER)
                    SPUtil.setSharedPreference(this, Constants.SP_LATEST_APP_VERSION, appVersionModel.APP_VER)

                if (!SPUtil.getSharedPreference(this, Constants.SP_ICI_LIKE).equals(appVersionModel.LIKE_ICI, ignoreCase = false))
                    SPUtil.setSharedPreference(this, Constants.SP_ICI_LIKE, appVersionModel.LIKE_ICI)

                if (!SPUtil.getSharedPreference(this, Constants.SP_COUNTRY_CODE).equals(appVersionModel.COUNTRY_CODE, ignoreCase = false))
                    SPUtil.setSharedPreference(this, Constants.SP_COUNTRY_CODE, appVersionModel.COUNTRY_CODE)

                if (!SPUtil.getSharedPreference(this, Constants.SP_INTRO_BG).equals(appVersionModel.POST_INTRO_BG, ignoreCase = false))
                    SPUtil.setSharedPreference(this, Constants.SP_INTRO_BG, appVersionModel.POST_INTRO_BG)

                appVersionModel.arrNotiSettingItem?.takeIf { list -> list.isNotEmpty() }?.run {
                    val isNotificationPost: Boolean = "Y".equals(this[0].POST, ignoreCase = true)
                    val isNotificationOst: Boolean = "Y".equals(this[0].OST, ignoreCase = true)
                    val isNotificationService: Boolean = "Y".equals(this[0].MANNER, ignoreCase = true)
                    val isNotificationToday: Boolean = "Y".equals(this[0].TODAY, ignoreCase = true)

                    if (isNotificationPost != SPUtil.getBooleanSharedPreference(this@SplashActivity, Constants.SP_NOTIFICATION_POST))
                        SPUtil.setSharedPreference(this@SplashActivity, Constants.SP_NOTIFICATION_POST, isNotificationPost)
                    if (isNotificationOst != SPUtil.getBooleanSharedPreference(this@SplashActivity, Constants.SP_NOTIFICATION_OST))
                        SPUtil.setSharedPreference(this@SplashActivity, Constants.SP_NOTIFICATION_OST, isNotificationOst)
                    if (isNotificationService != SPUtil.getBooleanSharedPreference(this@SplashActivity, Constants.SP_NOTIFICATION_SERVICE))
                        SPUtil.setSharedPreference(this@SplashActivity, Constants.SP_NOTIFICATION_SERVICE, isNotificationService)
                    if (isNotificationToday != SPUtil.getBooleanSharedPreference(this@SplashActivity, Constants.SP_NOTIFICATION_TODAY))
                        SPUtil.setSharedPreference(this@SplashActivity, Constants.SP_NOTIFICATION_TODAY, isNotificationToday)
                }

                /**
                 * YCNOTE : GlobalScope launch & async - kotlin coroutines
                 *
                 * 개념적으로 async 는 launch 와 같습니다.
                 * launch 는 Job 을 반환하고 아무런 결과 값도 전달하지 않는 반면 async 는 Deferred 를 반환합니다.
                 * Deferred 은 light-weight non-blocking future 로 결과를 전달하며, .await()을 사용하면 최종 결과를 얻을 수 있습니다.
                 * Deferred 는 또한 Job 이기 때문에 필요할 경우 취소 할 수 있습니다.
                 */
                GlobalScope.launch {
                    viewModel.clearPostData()

                    appVersionModel.arrColorItem?.takeIf { list -> list.isNotEmpty() }?.run {
                        viewModel.insertPostColorData(this)
                    }

                    appVersionModel.arrPostHashTagKeyWordItem?.takeIf { list -> list.isNotEmpty() }?.run {
                        viewModel.insertHashPopKeyword(this)
                    }

                    appVersionModel.arrPostPopKeyWordItem?.takeIf { list -> list.isNotEmpty() }?.run {
                        viewModel.insertPostPopKeyword(this)
                    }

                    appVersionModel.arrMusPopKeyWordItem?.takeIf { list -> list.isNotEmpty() }?.run {
                        viewModel.insertMusPopKeyword(this)
                    }
                }

                TedPermission.with(this)
                    .setPermissionListener(permissionListener)
                    .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                    .setPermissions(
                        android.Manifest.permission.RECORD_AUDIO // 마이크
                        , android.Manifest.permission.READ_CONTACTS // 연락처
                        , android.Manifest.permission.ACCESS_FINE_LOCATION // 위치
                        , android.Manifest.permission.ACCESS_COARSE_LOCATION // 위치
                        , android.Manifest.permission.WRITE_EXTERNAL_STORAGE // 저장공간
                        , android.Manifest.permission.READ_PHONE_STATE // 전화
                        , android.Manifest.permission.CALL_PHONE // 전화
                    )
                    .check()
            }
        })
    }

    private fun initLayout() {
        tvAppVersion.text = getString(R.string.app_version, BuildConfig.VERSION_NAME)

        textureVideoView?.apply {
            alpha = 0.3f
            setScaleType(TextureVideoView.ScaleType.CENTER_CROP)
            setDataSource(this@SplashActivity, Uri.parse("android.resource://$packageName/${R.raw.swings}"))
            setLooping(true)
            play()
        }

        RotateAnimation(180f, 360f, Animation.RELATIVE_TO_SELF, 0.5f,  Animation.RELATIVE_TO_SELF, 0.5f).apply {
            duration = 500
            repeatCount = Animation.INFINITE
        }.let {
            ivRotate.startAnimation(it)
        }

        // Network 에 연결되어 있지 않을 경우
        if (!DeviceUtil.isOnline(this))
            mHandler.sendEmptyMessage(Constants.DIALOG_EXCEPTION_NETWORK)
        else
            viewModel.getAppVersionData()
    }

    override fun handleMessage(msg: Message?) {
        when (msg?.what) {
            Constants.DIALOG_EXCEPTION_NETWORK -> {
                if (!isFinishing) {
                    createGlobalDialog(Constants.DIALOG_EXCEPTION_NETWORK, true)?.show()
                }
            }
        }
    }

    override fun onClick(v: View?) {
        v?.let {
            when (v.id) {
                // 삭제 onClick
                R.id.rlNoticeLayoutCloseBtn -> {
                }
            }
        }
    }

    private var permissionListener: PermissionListener = object : PermissionListener {
        override fun onPermissionGranted() {
            onPostActivity()
        }

        override fun onPermissionDenied(deniedPermissions: ArrayList<String>) {
            onPostActivity()
        }
    }

    private fun onPostActivity() {
        if (!isFinishing) {
            val intent = Intent(applicationContext, PostActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}