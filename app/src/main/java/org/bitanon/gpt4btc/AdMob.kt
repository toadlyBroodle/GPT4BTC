package org.bitanon.gpt4btc

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

private const val AD_ID_TEST = "ca-app-pub-3940256099942544/1033173712"
private const val AD_ID_BANNER = "ca-app-pub-9043912704472803/1187839419"
private const val AD_BLOCK_WORDS_TO_CONSUME = 50

private const val TAG = "AdMob"
class AdMob {
	companion object {

		var adIdInterstitial = "ca-app-pub-9043912704472803/9248779375"
		var mInterstitialAd: InterstitialAd? = null

		fun init(ctx: Context) {
			// don't load/show ads if ad blocking on
			if (Firestore.userState.value?.blockAds == true)
				return

			Log.d(TAG, "initializing AdMob")

			// when developing, use test ad id
			if (BuildConfig.DEBUG)
				adIdInterstitial = AD_ID_TEST

			// init AdMob
			MobileAds.initialize(ctx) {}
		}

		fun loadNewInterstitial(ctx: Context?) {
			if (ctx == null)
				return

			val adRequest = AdRequest.Builder().build()

			InterstitialAd.load(ctx, adIdInterstitial,
				adRequest, object : InterstitialAdLoadCallback() {
					override fun onAdFailedToLoad(adError: LoadAdError) {
						Log.d(TAG, "Ad load fail: $adError")
						FirebaseAnalytics.logCustomEvent(AD_INTERSTITIAL_LOAD_FAIL)
						mInterstitialAd = null
					}

					override fun onAdLoaded(interstitialAd: InterstitialAd) {
						Log.d(TAG, "Ad load success.")
						FirebaseAnalytics.logCustomEvent(AD_INTERSTITIAL_LOAD_SUCCESS)
						mInterstitialAd = interstitialAd

						mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
							override fun onAdClicked() {
								// Called when a click is recorded for an ad.
								Log.d(TAG, "Ad was clicked.")
								FirebaseAnalytics.logCustomEvent(AD_INTERSTITIAL_CLICK)
							}

							override fun onAdDismissedFullScreenContent() {
								// Called when ad is dismissed.
								Log.d(TAG, "Ad dismissed fullscreen content.")
								FirebaseAnalytics.logCustomEvent(AD_INTERSTITIAL_DISMISS)
								mInterstitialAd = null
							}

							override fun onAdFailedToShowFullScreenContent(p0: AdError) {
								// Called when ad fails to show.
								Log.e(TAG, "Ad failed to show fullscreen content.")
								FirebaseAnalytics.logCustomEvent(AD_INTERSTITIAL_SHOW_FAIL)
								mInterstitialAd = null
							}

							override fun onAdImpression() {
								// Called when an impression is recorded for an ad.
								Log.d(TAG, "Ad recorded an impression.")
							}

							override fun onAdShowedFullScreenContent() {
								// Called when ad is shown.
								Log.d(TAG, "Ad showed fullscreen content.")
								FirebaseAnalytics.logCustomEvent(AD_INTERSTITIAL_SHOW_SUCCESS)
							}
						}
					}
				})
		}

		fun getOpenAIKeyPart4(): String {
			return "4snRKPYJTyM"
		}

		fun showInterstitial(activ: Activity?) {

			// don't show if ad blocking on and purchased words available
			if (Firestore.userState.value?.blockAds == true &&
				Firestore.userState.value!!.purchasedWords != 0) {
				// consume purchased words
				activ?.let { MainActivity.firestore.consumePurchasedWords(it,
					AD_BLOCK_WORDS_TO_CONSUME) }
				return
			}

			if (activ != null && mInterstitialAd != null) {
				mInterstitialAd!!.show(activ)
			} else {
				Log.d(TAG, "The interstitial ad wasn't ready yet.")
				FirebaseAnalytics.logCustomEvent(AD_INTERSTITIAL_SHOW_FAIL)
			}
		}
	}
}
