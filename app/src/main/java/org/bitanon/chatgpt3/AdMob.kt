package org.bitanon.chatgpt3

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AdMob {
	companion object {
		private val TAG = "AdMob"

		var mInterstitialAd: InterstitialAd? = null

		fun init(ctx: Context) {
			Log.d(TAG, "initializing AdMob")

			//var adId = ctx.resources.getString(R.string.admob_chat_interstitial_ad_unit_id)
			// when developing/testing, use test ad id
			//if (BuildConfig.DEBUG)
			val adId = ctx.resources.getString(R.string.admob_test_ad_unit_id)

			// init AdMob
			MobileAds.initialize(ctx) {}

			val adRequest = AdRequest.Builder().build()

			InterstitialAd.load(ctx, adId,
				adRequest, object : InterstitialAdLoadCallback() {
					override fun onAdFailedToLoad(adError: LoadAdError) {
						Log.d(TAG, adError.toString())
						mInterstitialAd = null
					}

					override fun onAdLoaded(interstitialAd: InterstitialAd) {
						Log.d(TAG, "Ad was loaded.")
						mInterstitialAd = interstitialAd
					}
				})

			mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
				override fun onAdClicked() {
					// Called when a click is recorded for an ad.
					Log.d(TAG, "Ad was clicked.")
				}

				override fun onAdDismissedFullScreenContent() {
					// Called when ad is dismissed.
					Log.d(TAG, "Ad dismissed fullscreen content.")
					mInterstitialAd = null
				}

				override fun onAdFailedToShowFullScreenContent(p0: AdError) {
					// Called when ad fails to show.
					Log.e(TAG, "Ad failed to show fullscreen content.")
					mInterstitialAd = null
				}

				override fun onAdImpression() {
					// Called when an impression is recorded for an ad.
					Log.d(TAG, "Ad recorded an impression.")
				}

				override fun onAdShowedFullScreenContent() {
					// Called when ad is shown.
					Log.d(TAG, "Ad showed fullscreen content.")
				}
			}

		}

		fun show(activ: Activity?) {
			if (activ != null && mInterstitialAd != null) {
				mInterstitialAd!!.show(activ)
			} else {
				Log.d(TAG, "The interstitial ad wasn't ready yet.")
			}
		}
	}
}
