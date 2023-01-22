package org.bitanon.chatgpt3

import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

// custom analytics events
const val TERMS_AGREEMENT_SHOW = "terms_agreement_show"
const val BUTTON_ACCEPT_TERMS = "button_accept_terms"
const val BUTTON_REJECT_TERMS = "button_reject_terms"
const val BUTTON_PROMPT_DICTATE = "button_prompt_dictate"
const val BUTTON_PROMPT_RANDOM = "button_prompt_random"
const val BUTTON_PROMPT_SEND = "button_prompt_send"
const val BUTTON_LOGIN = "button_login"
const val BUTTON_LOGOUT = "button_logout"
const val BUTTON_UPGRADE = "button_upgrade"
const val APP_PRIVACY_TERMS_OF_USE = "app_terms_of_use_click"
const val APP_PRIVACY_POLICY_CLICK = "app_privacy_policy_click"
const val OPENAI_TERMS_OF_USE_CLICK = "openai_terms_of_use_click"
const val OPENAI_PRIVACY_POLICY_CLICK = "openai_privacy_policy_click"
const val OPENAI_RESPONSE_SHOW = "openai_response_show"
const val OPENAI_UNAUTHORIZED_ACCESS = "openai_unauthorized_access"
const val NOTIFICATION_PROMPT_TRUNCATED = "notification_prompt_truncated"
const val NOTIFICATION_RESPONSE_TRUNCATED = "notification_response_truncated"
const val AD_INTERSTITIAL_LOAD_SUCCESS = "ad_interstitial_load_success"
const val AD_INTERSTITIAL_LOAD_FAIL = "ad_interstitial_load_fail"
const val AD_INTERSTITIAL_SHOW_SUCCESS = "ad_interstitial_show_success"
const val AD_INTERSTITIAL_SHOW_FAIL = "ad_interstitial_show_fail"
const val AD_INTERSTITIAL_CLICK = "ad_interstitial_click"
const val AD_INTERSTITIAL_DISMISS = "ad_interstitial_dismiss"
const val EXCEPTION_SOCKET_TIMEOUT = "exception_socket_timeout"
const val LOGIN_SUCCESS = "login_success"
const val LOGIN_FAIL = "login_fail"
const val UPGRADE_LN_PAYMENT_SEND_CLICK = "upgrade_ln_payment_send_click"
const val UPGRADE_LN_WALLET_GETALBY_CLICK = "upgrade_ln_wallet_getalby_click"
const val UPGRADE_LN_WALLET_NONCUSTODIAL_CLICK = "upgrade_ln_wallet_noncustodial_click"


private const val TAG = "Firebase"
class FirebaseAnalytics {
	companion object {

		const val OPENAI_KEY_PART1 = "sk-dJEZ2sZb"
		private var firebaseAnalytics: FirebaseAnalytics = Firebase.analytics

		fun logCustomEvent(id: String) {
			Log.d(TAG, "logCustomEvent: $id")

			// don't sent events while debugging
			if (BuildConfig.DEBUG)
				return

			firebaseAnalytics.logEvent(id) {
				param(FirebaseAnalytics.Param.ITEM_ID, id)
				param(FirebaseAnalytics.Param.CONTENT_TYPE, id)
			}
		}

		fun getAdIdPart3(): String {
			return "6286785755"
		}
	}
}