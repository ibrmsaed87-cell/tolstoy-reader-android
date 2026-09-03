import sys

with open('app/src/main/java/com/spinel/tolstoyreader/ads/AdManager.kt', 'r') as f:
    content = f.read()

old_fun = """    fun showInterstitialOnTransition(activity: Activity, onContinue: () -> Unit) {
        actionCountSinceLastInterstitial++
        if (actionCountSinceLastInterstitial >= INTERSTITIAL_ACTION_THRESHOLD) {
            actionCountSinceLastInterstitial = 0 // Reset immediately
            
            if (interstitialAd != null && !isShowingFullScreenAd && !isReaderModeActive) {
                interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        interstitialAd = null
                        isShowingFullScreenAd = false
                        loadInterstitialAd(activity)
                        onContinue()
                    }
                    override fun onAdFailedToShowFullScreenContent(error: AdError) {
                        interstitialAd = null
                        isShowingFullScreenAd = false
                        loadInterstitialAd(activity)
                        onContinue()
                    }
                    override fun onAdShowedFullScreenContent() {
                        isShowingFullScreenAd = true
                    }
                }
                interstitialAd?.show(activity)
            } else {
                onContinue()
            }
        } else {
            onContinue()
        }
    }"""

new_fun = """    private var pendingAction: (() -> Unit)? = null
    
    fun showInterstitialOnTransition(activity: Activity, onContinue: () -> Unit) {
        // Prevent double clicks while ad is showing or about to show
        if (isShowingFullScreenAd) {
            return // Ignore clicks if ad is already on screen
        }
        
        actionCountSinceLastInterstitial++
        if (actionCountSinceLastInterstitial >= INTERSTITIAL_ACTION_THRESHOLD) {
            actionCountSinceLastInterstitial = 0 // Reset immediately
            
            if (interstitialAd != null && !isReaderModeActive) {
                pendingAction = onContinue // Store the action to prevent closure overwrite issues
                interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        interstitialAd = null
                        isShowingFullScreenAd = false
                        loadInterstitialAd(activity)
                        pendingAction?.invoke()
                        pendingAction = null
                    }
                    override fun onAdFailedToShowFullScreenContent(error: AdError) {
                        interstitialAd = null
                        isShowingFullScreenAd = false
                        loadInterstitialAd(activity)
                        pendingAction?.invoke()
                        pendingAction = null
                    }
                    override fun onAdShowedFullScreenContent() {
                        isShowingFullScreenAd = true
                    }
                }
                interstitialAd?.show(activity)
                isShowingFullScreenAd = true // Immediately mark as showing to prevent double-clicks
            } else {
                onContinue()
            }
        } else {
            onContinue()
        }
    }"""

if old_fun in content:
    content = content.replace(old_fun, new_fun)
    with open('app/src/main/java/com/spinel/tolstoyreader/ads/AdManager.kt', 'w') as f:
        f.write(content)
    print("Success")
else:
    print("Not found")
