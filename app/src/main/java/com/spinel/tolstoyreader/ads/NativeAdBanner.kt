package com.spinel.tolstoyreader.ads

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.spinel.tolstoyreader.R

@Composable
fun NativeAdBanner() {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var adLoaded by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val adLoader = AdLoader.Builder(context, AdManager.NATIVE_AD_UNIT_ID)
            .forNativeAd { ad ->
                nativeAd?.destroy()
                nativeAd = ad
                adLoaded = true
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adLoaded = false
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()

        adLoader.loadAd(AdRequest.Builder().build())

        onDispose {
            nativeAd?.destroy()
        }
    }

    if (adLoaded && nativeAd != null) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            AndroidView(
                factory = { ctx ->
                    val inflater = LayoutInflater.from(ctx)
                    // We need a layout for the native ad. 
                    // To avoid creating a separate XML file, we build it programmatically 
                    // or inflate a simple one if available. Since we don't have XML, let's create views programmatically.
                    
                    val adView = NativeAdView(ctx)
                    
                    val rootLayout = android.widget.LinearLayout(ctx).apply {
                        orientation = android.widget.LinearLayout.VERTICAL
                        setPadding(32, 32, 32, 32)
                    }
                    
                    // Ad attribution label
                    val adLabel = TextView(ctx).apply {
                        text = "Ad"
                        textSize = 12f
                        setBackgroundColor(android.graphics.Color.parseColor("#FFCC66"))
                        setTextColor(android.graphics.Color.BLACK)
                        setPadding(8, 2, 8, 2)
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    }
                    rootLayout.addView(adLabel)
                    
                    // Headline
                    val headlineView = TextView(ctx).apply {
                        textSize = 16f
                        setTypeface(null, android.graphics.Typeface.BOLD)
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { topMargin = 16 }
                    }
                    rootLayout.addView(headlineView)
                    adView.headlineView = headlineView
                    
                    // Body
                    val bodyView = TextView(ctx).apply {
                        textSize = 14f
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { topMargin = 8 }
                    }
                    rootLayout.addView(bodyView)
                    adView.bodyView = bodyView
                    
                    // Call to Action
                    val callToActionView = Button(ctx).apply {
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { topMargin = 16 }
                    }
                    rootLayout.addView(callToActionView)
                    adView.callToActionView = callToActionView
                    
                    adView.addView(rootLayout)
                    adView
                },
                update = { adView ->
                    // Set colors based on Material Theme inside Compose update
                    // We can't easily pass Compose colors to AndroidView directly like this but we can use current context colors.
                    
                    val nativeAdLocal = nativeAd ?: return@AndroidView
                    
                    (adView.headlineView as? TextView)?.text = nativeAdLocal.headline
                    
                    if (nativeAdLocal.body == null) {
                        adView.bodyView?.visibility = View.INVISIBLE
                    } else {
                        adView.bodyView?.visibility = View.VISIBLE
                        (adView.bodyView as? TextView)?.text = nativeAdLocal.body
                    }
                    
                    if (nativeAdLocal.callToAction == null) {
                        adView.callToActionView?.visibility = View.INVISIBLE
                    } else {
                        adView.callToActionView?.visibility = View.VISIBLE
                        (adView.callToActionView as? Button)?.text = nativeAdLocal.callToAction
                    }
                    
                    adView.setNativeAd(nativeAdLocal)
                }
            )
        }
    }
}
