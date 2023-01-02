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
const val BUTTON_SUBSCRIBE = "button_subscribe"
const val BUTTON_PROMPT_SEND = "button_prompt_send"
const val NOTIFICATION_SHOW = "notification_show"
const val NOTIFICATION_PROMPT_TRUNCATED = "notification_prompt_truncated"
const val NOTIFICATION_ANSWER_TRUNCATED = "notification_answer_truncated"

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
			val type = NOTIFICATION_SHOW
			firebaseAnalytics.logEvent(type) {
				param(FirebaseAnalytics.Param.ITEM_ID, id)
				param(FirebaseAnalytics.Param.CONTENT_TYPE, type)
			}
		}

		fun logItemSelect(id: String, name: String) {
			val type = FirebaseAnalytics.Event.SELECT_ITEM
			firebaseAnalytics.logEvent(type) {
				param(FirebaseAnalytics.Param.ITEM_ID, id)
				param(FirebaseAnalytics.Param.ITEM_NAME, name)
				param(FirebaseAnalytics.Param.CONTENT_TYPE, type)
			}
		}
	}
}