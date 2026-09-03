import sys

with open('app/src/main/java/com/spinel/tolstoyreader/ads/AdManager.kt', 'r') as f:
    content = f.read()

# 1. Remove REWARDED_AD_UNIT_ID
content = content.replace('    const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"\n', '')

# 2. Remove INTERSTITIAL_ACTION_THRESHOLD and actionCountSinceLastInterstitial
content = content.replace('    private const val INTERSTITIAL_ACTION_THRESHOLD = 4\n', '')
content = content.replace('    private var actionCountSinceLastInterstitial = 0\n', '')

# 3. Remove rewarded variables
content = content.replace('    private var rewardedAd: RewardedAd? = null\n', '')
content = content.replace('    private var isRewardedLoading = false\n', '')

# 4. Remove loadRewardedAd from initializeMobileAds
content = content.replace('            loadRewardedAd(context)\n', '')

# 5. Replace showInterstitialOnTransition
old_interstitial = """    fun showInterstitialOnTransition(activity: Activity, onContinue: () -> Unit) {
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

new_interstitial = """    fun showInterstitialOnTransition(activity: Activity, onContinue: () -> Unit) {
        // Prevent double clicks while ad is showing or about to show
        if (isShowingFullScreenAd) {
            return // Ignore clicks if ad is already on screen
        }
        
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
    }"""

if old_interstitial in content:
    content = content.replace(old_interstitial, new_interstitial)
else:
    print("Warning: showInterstitialOnTransition not found exactly as expected.")

# 6. Remove rewarded section
import re
rewarded_regex = r"    // --- Rewarded Ad ---.*?(?=\z|})"
content = re.sub(r"    // --- Rewarded Ad ---.*?^}", "}", content, flags=re.DOTALL | re.MULTILINE)

# Also remove import com.google.android.gms.ads.rewarded.RewardedAd and RewardedAdLoadCallback
content = content.replace('import com.google.android.gms.ads.rewarded.RewardedAd\n', '')
content = content.replace('import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback\n', '')


with open('app/src/main/java/com/spinel/tolstoyreader/ads/AdManager.kt', 'w') as f:
    f.write(content)

print("Done AdManager")
