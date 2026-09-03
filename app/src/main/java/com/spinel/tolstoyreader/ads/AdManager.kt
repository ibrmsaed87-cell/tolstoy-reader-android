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
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

object AdManager {
    private const val TAG = "AdManager"

    // Test Ad Unit IDs
    const val APP_OPEN_AD_UNIT_ID = "ca-app-pub-9118481973136364/6369664858"
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-9118481973136364/3893720920"
    const val NATIVE_AD_UNIT_ID = "ca-app-pub-9118481973136364/1277120271"
    const val BANNER_AD_UNIT_ID = "ca-app-pub-9118481973136364/9357800592"
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-9118481973136364/7083980728"

    var isReaderModeActive = false
    var isShowingFullScreenAd = false

    private lateinit var consentInformation: ConsentInformation

    private val nativeAdCache = mutableMapOf<String, com.google.android.gms.ads.nativead.NativeAd>()

    private val loadingNativeAds = mutableSetOf<String>()

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
    }

    // Interstitial State
    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false

    private var chapterTransitionCount = 0
    private const val CHAPTER_TRANSITION_THRESHOLD = 3

    // App Open State
    private var appOpenAd: AppOpenAd? = null
    private var isAppOpenAdLoading = false
    private var loadTime: Long = 0

    // Rewarded State

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
        // Prevent double clicks while ad is showing or about to show
        if (isShowingFullScreenAd) {
            return // Ignore clicks if ad is already on screen
        }
        
        if (interstitialAd != null && !isReaderModeActive) {
            val targetAction = onContinue
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    isShowingFullScreenAd = false
                    loadInterstitialAd(activity)
                    targetAction()
                }
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    isShowingFullScreenAd = false
                    loadInterstitialAd(activity)
                    targetAction()
                }
                override fun onAdShowedFullScreenContent() {
                    isShowingFullScreenAd = true
                }
            }
            interstitialAd?.show(activity)
            isShowingFullScreenAd = true // Immediately mark as showing to prevent double-clicks
        } else {
            loadInterstitialAd(activity)
            onContinue()
        }
    }

    fun showChapterTransitionInterstitial(activity: Activity, onContinue: () -> Unit) {
        chapterTransitionCount++
        if (chapterTransitionCount >= CHAPTER_TRANSITION_THRESHOLD) {
            chapterTransitionCount = 0 // Reset immediately
            
            // Notice: we bypass !isReaderModeActive here because we ARE in the reader mode
            if (interstitialAd != null && !isShowingFullScreenAd) {
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
            loadInterstitialAd(activity)
            onContinue()
        }
    }

}
