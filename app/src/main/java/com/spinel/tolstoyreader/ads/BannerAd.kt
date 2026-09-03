package com.spinel.tolstoyreader.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun ReaderBannerAd() {
    val context = LocalContext.current
    var adView by remember { mutableStateOf<AdView?>(null) }

    DisposableEffect(adView) {
        onDispose {
            adView?.destroy()
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { ctx ->
            AdView(ctx).apply {
                val displayMetrics = ctx.resources.displayMetrics
                val adWidth = (displayMetrics.widthPixels / displayMetrics.density).toInt()
                
                setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(ctx, adWidth))
                adUnitId = AdManager.BANNER_AD_UNIT_ID
                
                // Only load if consent allows
                val consentInfo = com.google.android.ump.UserMessagingPlatform.getConsentInformation(ctx)
                if (consentInfo.canRequestAds()) {
                    loadAd(AdRequest.Builder().build())
                }
                
                adView = this
            }
        }
    )
}
