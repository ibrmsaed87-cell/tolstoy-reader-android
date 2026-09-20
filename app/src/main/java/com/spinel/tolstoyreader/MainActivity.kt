package com.spinel.tolstoyreader

import android.content.Intent
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.spinel.tolstoyreader.ui.navigation.AppNavigation
import com.spinel.tolstoyreader.ui.theme.MyApplicationTheme
import com.spinel.tolstoyreader.ui.viewmodel.BookViewModel
import com.spinel.tolstoyreader.ads.AdManager
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        AdManager.initConsentAndAds(this)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        handleIntentUrl(intent)

        setContent {
            val bookViewModel: BookViewModel = viewModel()
            val currentLang by bookViewModel.appLanguage.collectAsState()
            val currentTheme by bookViewModel.appTheme.collectAsState()
            
            val locale = Locale(currentLang)
            Locale.setDefault(locale)
            val configuration = Configuration(resources.configuration)
            configuration.setLocale(locale)
            val newContext = createConfigurationContext(configuration)
            
            val layoutDirection = if (TextUtilsCompat.getLayoutDirectionFromLocale(locale) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                LayoutDirection.Rtl
            } else {
                LayoutDirection.Ltr
            }
            
            val darkTheme = when (currentTheme) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }
            
            CompositionLocalProvider(
                LocalContext provides newContext,
                LocalConfiguration provides configuration,
                LocalLayoutDirection provides layoutDirection
            ) {
                MyApplicationTheme(darkTheme = darkTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        AppNavigation(navController = navController, bookViewModel = bookViewModel)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntentUrl(intent)
    }

    private fun handleIntentUrl(intent: Intent?) {
        val url = intent?.getStringExtra("url")
            ?: intent?.getStringExtra("link")
            ?: intent?.getStringExtra("action_url")
            ?: intent?.dataString

        if (url.isNullOrBlank()) return

        try {
            val uri = Uri.parse(url.trim())
            val isWebLink = uri.scheme.equals("https", true) || uri.scheme.equals("http", true)
            val isMarketLink = uri.scheme.equals("market", true)
            if (!isWebLink && !isMarketLink) {
                Log.w("MainActivity", "Blocked unsupported notification URL: $url")
                return
            }

            val playPackageId = when {
                isMarketLink -> uri.getQueryParameter("id")
                uri.host.equals("play.google.com", true) -> uri.getQueryParameter("id")
                else -> null
            }

            if (!playPackageId.isNullOrBlank()) {
                try {
                    startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$playPackageId"))
                            .setPackage("com.android.vending")
                    )
                } catch (_: ActivityNotFoundException) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=$playPackageId")
                        )
                    )
                }
            } else {
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }

            intent?.removeExtra("url")
            intent?.removeExtra("link")
            intent?.removeExtra("action_url")
            intent?.data = null
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to open notification URL: $url", e)
            Toast.makeText(this, "Unable to open link", Toast.LENGTH_SHORT).show()
        }
    }
}
