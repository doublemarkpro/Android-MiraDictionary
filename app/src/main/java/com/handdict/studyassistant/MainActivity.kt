package com.handdict.studyassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.handdict.studyassistant.ui.MiraStudyApp
import com.handdict.studyassistant.ui.theme.MiraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiraTheme {
                MiraStudyApp()
            }
        }
    }
}

