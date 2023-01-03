package org.bitanon.chatgpt3

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

const val SCREEN_CHAT = "screen_chat"
const val SCREEN_TERMS_AGREEMENT = "screen_terms_agreement"
const val SCREEN_SETTINGS = "screen_settings"
const val BUTTON_ACCEPT_TERMS = "button_accept_terms"
const val BUTTON_REJECT_TERMS = "button_reject_terms"
const val BUTTON_PROMPT_SEND = "button_prompt_send"
const val BUTTON_SUBSCRIBE = "button_subscribe"
const val BUTTON_JOIN_TEST_GROUP = "button_join_test_group"
const val EVENT_OPENAI_RESPONSE_SHOW = "openai_response_show"
const val EVENT_NOTIFICATION_SHOW = "notification_show"
const val NOTIFICATION_PROMPT_TRUNCATED = "notification_prompt_truncated"
const val NOTIFICATION_RESPONSE_TRUNCATED = "notification_response_truncated"
const val EVENT_AD_INTERSTITIAL_LOAD = "ad_interstitial_load"
const val AD_INTERSTITIAL_LOAD_SUCCESS = "ad_interstitial_load_success"
const val AD_INTERSTITIAL_LOAD_FAIL = "ad_interstitial_load_fail"
const val EVENT_AD_INTERSTITIAL_SHOW = "ad_interstitial_show"
const val AD_INTERSTITIAL_SHOW_SUCCESS = "ad_interstitial_show_success"
const val AD_INTERSTITIAL_SHOW_FAIL = "ad_interstitial_show_fail"
const val EVENT_AD_INTERSTITIAL_ENGAGE = "ad_interstitial_engage"
const val AD_INTERSTITIAL_CLICK = "ad_interstitial_click"
const val AD_INTERSTITIAL_DISMISS = "ad_interstitial_dismiss"

//private const val TAG = "Firebase"
class Firebase {
	companion object {

		private var firebaseAnalytics: FirebaseAnalytics = Firebase.analytics

		fun logScreenView(id: String) {
			val type = FirebaseAnalytics.Event.SCREEN_VIEW
			firebaseAnalytics.logEvent(type) {
				param(FirebaseAnalytics.Param.ITEM_ID, id)
				param(FirebaseAnalytics.Param.CONTENT_TYPE, type)
			}
		}

		fun logContentSelect(id: String) {
			val type = FirebaseAnalytics.Event.SELECT_CONTENT
			firebaseAnalytics.logEvent(type) {
				param(FirebaseAnalytics.Param.ITEM_ID, id)
				param(FirebaseAnalytics.Param.CONTENT_TYPE, type)
			}
		}

		fun logNotificationShow(id: String) {
			val type = EVENT_NOTIFICATION_SHOW
			firebaseAnalytics.logEvent(type) {
				param(FirebaseAnalytics.Param.ITEM_ID, id)
				param(FirebaseAnalytics.Param.CONTENT_TYPE, type)
			}
		}

		fun logAnswerShow(id: String) {
			val type = EVENT_OPENAI_RESPONSE_SHOW
			firebaseAnalytics.logEvent(type) {
				param(FirebaseAnalytics.Param.ITEM_ID, id)
				param(FirebaseAnalytics.Param.CONTENT_TYPE, type)
			}
		}

		fun logAdInterstitialLoad(id: String) {
			val type = EVENT_AD_INTERSTITIAL_LOAD
			firebaseAnalytics.logEvent(type) {
				param(FirebaseAnalytics.Param.ITEM_ID, id)
				param(FirebaseAnalytics.Param.CONTENT_TYPE, type)
			}
		}

		fun logAdInterstitialShow(id: String) {
			val type = EVENT_AD_INTERSTITIAL_SHOW
			firebaseAnalytics.logEvent(type) {
				param(FirebaseAnalytics.Param.ITEM_ID, id)
				param(FirebaseAnalytics.Param.CONTENT_TYPE, type)
			}
		}

		fun logAdInterstitialEngage(id: String) {
			val type = EVENT_AD_INTERSTITIAL_ENGAGE
			firebaseAnalytics.logEvent(type) {
				param(FirebaseAnalytics.Param.ITEM_ID, id)
				param(FirebaseAnalytics.Param.CONTENT_TYPE, type)
			}
		}

/*		fun logItemSelect(id: String, name: String) {
			val type = FirebaseAnalytics.Event.SELECT_ITEM
			firebaseAnalytics.logEvent(type) {
				param(FirebaseAnalytics.Param.ITEM_ID, id)
				param(FirebaseAnalytics.Param.ITEM_NAME, name)
				param(FirebaseAnalytics.Param.CONTENT_TYPE, type)
			}
		}*/
	}
}