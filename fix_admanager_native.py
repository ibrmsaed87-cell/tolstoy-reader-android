import sys

with open('app/src/main/java/com/spinel/tolstoyreader/ads/AdManager.kt', 'r') as f:
    content = f.read()

# Replace getNativeAd
old_getNativeAd = """    fun getNativeAd(context: Context, id: String, onLoaded: (com.google.android.gms.ads.nativead.NativeAd?) -> Unit) {
        if (nativeAdCache.containsKey(id)) {
            onLoaded(nativeAdCache[id])
            return
        }
        val adLoader = com.google.android.gms.ads.AdLoader.Builder(context, NATIVE_AD_UNIT_ID)
            .forNativeAd { ad ->
                nativeAdCache[id] = ad
                onLoaded(ad)
            }
            .withAdListener(object : com.google.android.gms.ads.AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    onLoaded(null)
                }
            })
            .withNativeAdOptions(com.google.android.gms.ads.nativead.NativeAdOptions.Builder().build())
            .build()
        
        if (::consentInformation.isInitialized && consentInformation.canRequestAds()) {
            adLoader.loadAd(AdRequest.Builder().build())
        } else {
            onLoaded(null)
        }
    }"""

new_getNativeAd = """    private val loadingNativeAds = mutableSetOf<String>()

    fun getNativeAd(context: Context, id: String, onLoaded: (com.google.android.gms.ads.nativead.NativeAd?) -> Unit) {
        if (nativeAdCache.containsKey(id)) {
            onLoaded(nativeAdCache[id])
            return
        }
        if (loadingNativeAds.contains(id)) {
            // Already loading this ad, wait for it to finish and hope recomposition picks it up later,
            // or just return null for now. To keep it simple, we just ignore duplicate requests.
            return
        }
        
        if (!::consentInformation.isInitialized || !consentInformation.canRequestAds()) {
            onLoaded(null)
            return
        }

        loadingNativeAds.add(id)
        val adLoader = com.google.android.gms.ads.AdLoader.Builder(context, NATIVE_AD_UNIT_ID)
            .forNativeAd { ad ->
                loadingNativeAds.remove(id)
                nativeAdCache[id] = ad
                onLoaded(ad)
            }
            .withAdListener(object : com.google.android.gms.ads.AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    loadingNativeAds.remove(id)
                    onLoaded(null)
                }
            })
            .withNativeAdOptions(com.google.android.gms.ads.nativead.NativeAdOptions.Builder().build())
            .build()
        
        adLoader.loadAd(AdRequest.Builder().build())
    }"""

if old_getNativeAd in content:
    content = content.replace(old_getNativeAd, new_getNativeAd)
    with open('app/src/main/java/com/spinel/tolstoyreader/ads/AdManager.kt', 'w') as f:
        f.write(content)
    print("Success")
else:
    print("Not found")

