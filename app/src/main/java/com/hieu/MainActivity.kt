
package com.hieu

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.hieu.core.theme.IndMartTheme
import com.hieu.data.local.TokenManager
import com.hieu.ui.navigation.AppNavHost
import com.hieu.ui.navigation.Screen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    private val deepLinkTriggered = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleDeepLink(intent)

        setContent {
            IndMartTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    AppNavHost(
                        navController = navController,
                        tokenManager = tokenManager,
                        onDeepLinkCallback = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )

                    if (deepLinkTriggered.value) {
                        deepLinkTriggered.value = false

                        Toast.makeText(
                            this@MainActivity,
                            "Hãy đăng nhập lại",
                            Toast.LENGTH_LONG
                        ).show()

                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val uri: Uri? = intent?.data

        if (uri != null &&
            uri.scheme == "indmart" &&
            uri.host == "callback"
        ) {
            deepLinkTriggered.value = true
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    IndMartTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // Preview giao diện độc lập
            // Không sử dụng TokenManager hoặc Activity ở đây
        }
    }
}
