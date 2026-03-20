package com.example.basic_ml_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContent
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.basic_ml_app.ui.navigation.AppNavGraph
import com.example.basic_ml_app.ui.theme.Basic_ml_appTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Basic_ml_appTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeContent
                ) { innerPadding ->
                    AppNavGraph(
                        innerPaddingValues = innerPadding,
                        navHostController = rememberNavController()
                    )
                }
            }
        }
    }
}
