package org.bitanon.chatgpt3

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import androidx.core.app.ActivityCompat.startIntentSenderForResult
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

// custom analytics events
const val TERMS_AGREEMENT_SHOW = "terms_agreement_show"
const val LINK_TERMS_OF_USE_CLICK = "link_terms_of_use_click"
const val LINK_PRIVACY_POLICY_CLICK = "link_privacy_policy_click"
const val BUTTON_ACCEPT_TERMS = "button_accept_terms"
const val BUTTON_REJECT_TERMS = "button_reject_terms"
const val BUTTON_PROMPT_SEND = "button_prompt_send"
const val BUTTON_LOGIN = "button_login"
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
const val BILLING_SUBSCRIPTION_ACKNOWLEDGE = "billing_subscription_acknowledge"
const val EXCEPTION_SOCKET_TIMEOUT = "exception_socket_timeout"
const val LOGIN_SUCCESS = "login_success"
const val LOGIN_FAIL = "login_fail"

private const val TAG = "Firebase"
class Firebase {
	companion object {

		const val OPENAI_KEY_PART1 = "sk-dJEZ2sZb"
		private var firebaseAnalytics: FirebaseAnalytics = Firebase.analytics
		var auth: FirebaseAuth = Firebase.auth

		val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity
		var showOneTapUI = true
		private val webClientId = "362473545664-003vio7f3rlc9nglsadg0ifcq3sld5su.apps.googleusercontent.com"
		private lateinit var oneTapClient: SignInClient
		private lateinit var signInRequest: BeginSignInRequest
		private lateinit var signUpRequest: BeginSignInRequest


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

		fun getOpenAIResponseMaxTokens(): Int {
			return 80 // tokens -> max allowed 4096
		}

		fun getAdIdPart3(): String {
			return "6286785755"
		}

		fun signIn(activ: Activity) {
			// setup One Tap sign-in client
			oneTapClient = Identity.getSignInClient(activ)
			signInRequest = BeginSignInRequest.builder()
				.setPasswordRequestOptions(
					BeginSignInRequest.PasswordRequestOptions.builder()
						.setSupported(true)
						.build())
				.setGoogleIdTokenRequestOptions(
					BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
						.setSupported(true)
						// Your server's client ID, not your Android client ID.
						.setServerClientId(webClientId)
						// Only show accounts previously used to sign in.
						.setFilterByAuthorizedAccounts(true)
						.build())
				// Automatically sign in when exactly one credential is retrieved.
				.setAutoSelectEnabled(true)
				.build()

			// display the one tap sign-in UI
			oneTapClient.beginSignIn(signInRequest)
				.addOnSuccessListener(activ) { result ->
					try {
						startIntentSenderForResult(activ,
							result.pendingIntent.intentSender, REQ_ONE_TAP,
							null, 0, 0, 0, null)
					} catch (e: IntentSender.SendIntentException) {
						Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
					}
				}
				.addOnFailureListener(activ) { e ->
					// No saved credentials found. Launch the One Tap sign-up flow, or
					// do nothing and continue presenting the signed-out UI.
					Log.d(TAG, "No saved credentials found: ${e.localizedMessage}")

					signUpRequest = BeginSignInRequest.builder()
						.setGoogleIdTokenRequestOptions(
							BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
								.setSupported(true)
								// Your server's client ID, not your Android client ID.
								.setServerClientId(webClientId)
								// Show all accounts on the device.
								.setFilterByAuthorizedAccounts(false)
								.build())
						.build()

				}
		}

		fun onSignInResult(activ: Activity, requestCode: Int, resultCode: Int, data: Intent?) {
			when (requestCode) {
				REQ_ONE_TAP -> {
					try {
						val credential = oneTapClient.getSignInCredentialFromIntent(data)
						val idToken = credential.googleIdToken
						val username = credential.id
						val password = credential.password
						when {
							idToken != null -> {
								// Got an ID token from Google. Use it to authenticate
								// with Firebase.
								val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
								auth.signInWithCredential(firebaseCredential)
									.addOnCompleteListener(activ) { task ->
										if (task.isSuccessful) {
											// Sign in success, update UI with the signed-in user's information
											logCustomEvent(LOGIN_SUCCESS)
											MainActivity.showToast(activ, "Login success")

											val user = auth.currentUser
											//updateUI(user)
										} else {
											// If sign in fails, display a message to the user.
											logCustomEvent(LOGIN_FAIL)
											Log.w(TAG, "signInWithCredential:failure", task.exception)
											MainActivity.showToast(activ, "Login failed")

											//updateUI(null)
										}
									}

							}
							password != null -> {
								// Got a saved username and password. Use them to authenticate
								// with your backend.
								Log.d(TAG, "Got password.")
							}
							else -> {
								// Shouldn't happen.
								Log.d(TAG, "No ID token or password!")
							}
						}
					} catch (e: ApiException) {
						when (e.statusCode) {
							CommonStatusCodes.CANCELED -> {
								Log.d(TAG, "One-tap dialog was closed.")
								// Don't re-prompt the user.
								showOneTapUI = false
							}
							CommonStatusCodes.NETWORK_ERROR -> {
								Log.d(TAG, "One-tap encountered a network error.")
								// Try again or just ignore.
							}
							else -> {
								Log.d(TAG, "Couldn't get credential from result." +
										" (${e.localizedMessage})")
							}
						}

					}
				}
			}
			// can signout with: Firebase.auth.signOut()
		}
	}
}