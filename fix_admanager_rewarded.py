import sys

with open('app/src/main/java/com/spinel/tolstoyreader/ads/AdManager.kt', 'r') as f:
    content = f.read()

old_fun = """    fun showRewardedAd(activity: Activity, onRewardEarned: () -> Unit) {
        if (isShowingFullScreenAd) return
        if (rewardedAd != null) {
            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    isShowingFullScreenAd = false
                    loadRewardedAd(activity)
                }
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    rewardedAd = null
                    isShowingFullScreenAd = false
                    loadRewardedAd(activity)
                }
                override fun onAdShowedFullScreenContent() {
                    isShowingFullScreenAd = true
                }
            }
            rewardedAd?.show(activity) {
                actionCountSinceLastInterstitial = 0 // Reset interstitial cap after watching rewarded
                onRewardEarned()
            }
        } else {
            loadRewardedAd(activity)
        }
    }"""

new_fun = """    fun showRewardedAd(activity: Activity, onAdNotReady: () -> Unit = {}, onRewardEarned: () -> Unit) {
        if (isShowingFullScreenAd) return
        if (rewardedAd != null) {
            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    isShowingFullScreenAd = false
                    loadRewardedAd(activity)
                }
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    rewardedAd = null
                    isShowingFullScreenAd = false
                    loadRewardedAd(activity)
                }
                override fun onAdShowedFullScreenContent() {
                    isShowingFullScreenAd = true
                }
            }
            rewardedAd?.show(activity) {
                actionCountSinceLastInterstitial = 0 // Reset interstitial cap after watching rewarded
                onRewardEarned()
            }
            isShowingFullScreenAd = true
        } else {
            loadRewardedAd(activity)
            onAdNotReady()
        }
    }"""

if old_fun in content:
    content = content.replace(old_fun, new_fun)
    with open('app/src/main/java/com/spinel/tolstoyreader/ads/AdManager.kt', 'w') as f:
        f.write(content)
    print("Success")
else:
    print("Not found")

