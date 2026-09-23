package com.handdict.studyassistant

import android.content.Intent
import android.os.Bundle
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.handdict.studyassistant.data.LocalStore
import com.handdict.studyassistant.ui.MiraStudyApp
import com.handdict.studyassistant.ui.theme.MiraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 每次打开应用都重新进入专注模式；家长可在应用内验证后临时退出。
        LocalStore(applicationContext).saveFocusModeActive(true)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            MiraTheme {
                MiraStudyApp()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // 从桌面或启动图标再次进入时，重建为默认的专注状态。
        recreate()
    }
}

