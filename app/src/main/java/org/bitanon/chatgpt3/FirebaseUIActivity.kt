package org.bitanon.chatgpt3

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth

private const val TAG = "FirebaseUIActivity"
class FirebaseUIActivity: AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// See: https://developer.android.com/training/basics/intents/result
		val signInLauncher = registerForActivityResult(
			FirebaseAuthUIActivityResultContract()
		) { res ->
			this.onSignInResult(res)
		}

		// Choose authentication providers
		val providers = arrayListOf(
			//AuthUI.IdpConfig.EmailBuilder().build(),
			//AuthUI.IdpConfig.PhoneBuilder().build(),
			AuthUI.IdpConfig.GoogleBuilder().build())

		// Create and launch sign-in intent
		val signInIntent = AuthUI.getInstance()
			.createSignInIntentBuilder()
			.setAvailableProviders(providers)
			.build()
		signInLauncher.launch(signInIntent)

	}

	private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
		val response = result.idpResponse
		if (result.resultCode == RESULT_OK) {
			// Successfully signed in
			val user = FirebaseAuth.getInstance().currentUser
			Log.d(TAG, "Login success: user: ${user?.email}")
			Firebase.logCustomEvent(LOGIN_SUCCESS)
			MainActivity.showToast(this, "Signed in as ${user?.displayName}")

		} else {
			// Sign in failed. If response is null the user canceled the
			// sign-in flow using the back button. Otherwise check
			// response.getError().getErrorCode() and handle the error.
			Log.d(TAG, "Login error: ${response?.error}")
			Firebase.logCustomEvent(LOGIN_FAIL)
			MainActivity.showToast(this, "Login failed: ${response?.error}")

		}
	}
}