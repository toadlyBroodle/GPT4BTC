package org.bitanon.chatgpt3

import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

// custom analytics events
const val TERMS_AGREEMENT_SHOW = "terms_agreement_show"
const val LINK_TERMS_OF_USE_CLICK = "link_terms_of_use_click"
const val LINK_PRIVACY_POLICY_CLICK = "link_privacy_policy_click"
const val BUTTON_ACCEPT_TERMS = "button_accept_terms"
const val BUTTON_REJECT_TERMS = "button_reject_terms"
const val BUTTON_PROMPT_SEND = "button_prompt_send"
const val BUTTON_SUBSCRIBE = "button_subscribe"
const val BUTTON_JOIN_TEST_GROUP = "button_join_test_group"
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

private const val TAG = "Firebase"
class Firebase {
	companion object {

		const val OPENAI_KEY_PART1 = "sk-dJEZ2sZb"
		private var firebaseAnalytics: FirebaseAnalytics = Firebase.analytics

		fun logCustomEvent(id: String) {
			Log.d(TAG, "logCustomEvent: $id")
			firebaseAnalytics.logEvent(id) {
				param(FirebaseAnalytics.Param.ITEM_ID, id)
				param(FirebaseAnalytics.Param.CONTENT_TYPE, id)
			}
		}

		fun getOpenAIResponseMaxTokens(): Int {
			return 80 // Max allowed: 4096
		}

		fun getAdIdPart3(): String {
			return "6286785755"
		}
	}
}