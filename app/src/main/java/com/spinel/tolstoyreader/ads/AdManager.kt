package com.spinel.tolstoyreader.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

object AdManager {
    private const val TAG = "AdManager"

    // Test Ad Unit IDs
    const val APP_OPEN_AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    const val NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    var isReaderModeActive = false
    var isShowingFullScreenAd = false

    private lateinit var consentInformation: ConsentInformation

    // Interstitial State
    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false
    private var actionCountSinceLastInterstitial = 0
    private const val INTERSTITIAL_ACTION_THRESHOLD = 4

    private var chapterTransitionCount = 0
    private const val CHAPTER_TRANSITION_THRESHOLD = 3

    // App Open State
    private var appOpenAd: AppOpenAd? = null
    private var isAppOpenAdLoading = false
    private var loadTime: Long = 0

    // Rewarded State
    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    fun initConsentAndAds(activity: Activity) {
        val debugSettings = ConsentDebugSettings.Builder(activity)
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            .build()
            
        val params = ConsentRequestParameters.Builder()
            // .setConsentDebugSettings(debugSettings) // Uncomment for testing EEA
            .build()

        consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w(TAG, "Consent form error: ${formError.errorCode} - ${formError.message}")
                    }
                    if (consentInformation.canRequestAds()) {
                        initializeMobileAds(activity)
                    }
                }
            },
            { requestConsentError ->
                Log.w(TAG, "Consent update error: ${requestConsentError.errorCode} - ${requestConsentError.message}")
            }
        )

        // Check if we can initialize right away (e.g., returning user)
        if (consentInformation.canRequestAds()) {
            initializeMobileAds(activity)
        }
    }

    fun isPrivacyOptionsRequired(): Boolean {
        return if (::consentInformation.isInitialized) {
            consentInformation.privacyOptionsRequirementStatus == 
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
        } else {
            false
        }
    }

    fun showPrivacyOptionsForm(activity: Activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            if (formError != null) {
                Log.w(TAG, "Privacy options error: ${formError.errorCode} - ${formError.message}")
            }
        }
    }

    private var isMobileAdsInitialized = false
    private fun initializeMobileAds(context: Context) {
        if (isMobileAdsInitialized) return
        MobileAds.initialize(context) { initializationStatus ->
            Log.d(TAG, "Mobile Ads Initialized: $initializationStatus")
            isMobileAdsInitialized = true
            loadAppOpenAd(context)
            loadInterstitialAd(context)
            loadRewardedAd(context)
        }
    }

    // --- App Open Ad ---
    
    fun loadAppOpenAd(context: Context) {
        if (isAppOpenAdLoading || isAppOpenAdAvailable()) return
        isAppOpenAdLoading = true

        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context, APP_OPEN_AD_UNIT_ID, request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isAppOpenAdLoading = false
                    loadTime = System.currentTimeMillis()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.d(TAG, "AppOpenAd failed to load: ${loadAdError.message}")
                    isAppOpenAdLoading = false
                }
            }
        )
    }

    private fun isAppOpenAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
    }

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference = System.currentTimeMillis() - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    fun showAppOpenAdIfAvailable(activity: Activity) {
        if (isShowingFullScreenAd || isReaderModeActive || !isMobileAdsInitialized) {
            return
        }

        if (!isAppOpenAdAvailable()) {
            loadAppOpenAd(activity)
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingFullScreenAd = false
                loadAppOpenAd(activity)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAd = null
                isShowingFullScreenAd = false
                loadAppOpenAd(activity)
            }

            override fun onAdShowedFullScreenContent() {
                isShowingFullScreenAd = true
            }
        }

        appOpenAd?.show(activity)
    }

    // --- Interstitial Ad ---

    fun loadInterstitialAd(context: Context) {
        if (isInterstitialLoading || interstitialAd != null) return
        isInterstitialLoading = true

        InterstitialAd.load(context, INTERSTITIAL_AD_UNIT_ID, AdRequest.Builder().build(), 
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                }
            }
        )
    }

    fun showInterstitialOnTransition(activity: Activity, onContinue: () -> Unit) {
        actionCountSinceLastInterstitial++
        if (actionCountSinceLastInterstitial >= INTERSTITIAL_ACTION_THRESHOLD) {
            if (interstitialAd != null && !isShowingFullScreenAd && !isReaderModeActive) {
                interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        interstitialAd = null
                        isShowingFullScreenAd = false
                        actionCountSinceLastInterstitial = 0
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
    }

    fun showChapterTransitionInterstitial(activity: Activity, onContinue: () -> Unit) {
        chapterTransitionCount++
        if (chapterTransitionCount >= CHAPTER_TRANSITION_THRESHOLD) {
            // Notice: we bypass !isReaderModeActive here because we ARE in the reader mode
            if (interstitialAd != null && !isShowingFullScreenAd) {
                interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        interstitialAd = null
                        isShowingFullScreenAd = false
                        chapterTransitionCount = 0
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
    }

    // --- Rewarded Ad ---

    fun loadRewardedAd(context: Context) {
        if (isRewardedLoading || rewardedAd != null) return
        isRewardedLoading = true

        RewardedAd.load(context, REWARDED_AD_UNIT_ID, AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading = false
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading = false
                }
            })
    }

    fun showRewardedAd(activity: Activity, onRewardEarned: () -> Unit) {
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
    }
}
