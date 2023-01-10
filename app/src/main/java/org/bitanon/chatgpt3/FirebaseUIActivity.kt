package org.bitanon.chatgpt3

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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
			val u = FirebaseAuth.getInstance().currentUser
			_userState.value = u

			Log.d(TAG, "Login success: user: ${u?.email}")
			FirebaseAnalytics.logCustomEvent(LOGIN_SUCCESS)
			MainActivity.showToast(this, "Signed in as ${u?.displayName}")

		} else {
			// Sign in failed. If response is null the user canceled the
			// sign-in flow using the back button. Otherwise check
			// response.getError().getErrorCode() and handle the error.
			Log.d(TAG, "Login error: ${response?.error}")
			FirebaseAnalytics.logCustomEvent(LOGIN_FAIL)
			MainActivity.showToast(this, getString(R.string.login_failed)
					+ " " + response?.error)

		}

		finish()
	}

	companion object {

		// Backing property to avoid state updates from other classes
		private val _userState: MutableStateFlow<FirebaseUser?> = MutableStateFlow(null)
		// The UI collects from this StateFlow to get its state updates
		var userState: StateFlow<FirebaseUser?> = _userState

		fun signOut(ctx: Context) {
			AuthUI.getInstance()
				.signOut(ctx)
				.addOnCompleteListener {
					MainActivity.showToast(ctx, ctx.getString(R.string.logged_out))
				}
			_userState.value = null
		}
	}
}