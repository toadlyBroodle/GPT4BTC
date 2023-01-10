package org.bitanon.chatgpt3

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AccountActivity: AppCompatActivity() {

	private lateinit var buttonLogin: Button
	private lateinit var buttonLogout: Button


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.account_activity)

		// initialize billing
		Billing.init(this, lifecycleScope)
	}

	@SuppressLint("ClickableViewAccessibility")
	override fun onResume() {
		super.onResume()

		buttonLogin = findViewById<Button>(R.id.button_login)
		buttonLogin.setOnClickListener {

			// launch FirebaseUIActivity
			val startActivity = Intent(this, FirebaseUIActivity::class.java)
			startActivity(startActivity)
		}

		buttonLogout = findViewById<Button>(R.id.button_logout)
		buttonLogout.setOnClickListener {

			FirebaseUIActivity.signOut(this)

			// set button visibilities
			setLoginButtonVisibility(false)

		}

		// on join testers button click
		findViewById<Button>(R.id.button_join_testers).setOnClickListener {
			FirebaseAnalytics.logCustomEvent(BUTTON_JOIN_TEST_GROUP)

			composeEmail(
				arrayOf("anon@bitanon.org"),
				getString(R.string.join_testers),
				getString(R.string.require_gmail)
			)
		}

		// on subscribe button click
		findViewById<Button>(R.id.button_subscribe).setOnClickListener {
			FirebaseAnalytics.logCustomEvent(BUTTON_SUBSCRIBE)

			// check for internet connection
			if (!RequestRepository.isOnline(baseContext)) {
				// toast user to connect
				MainActivity.showToast(baseContext,
					getString(R.string.toast_no_internet))
				return@setOnClickListener
			}

			lifecycleScope.launch {
				Billing.subscribe(this@AccountActivity, lifecycleScope)
			}
		}

		// set button visibilities
		lifecycleScope.launch {
			FirebaseUIActivity.userState.collect { userState ->
				if (userState == null)
					setLoginButtonVisibility(false)
				else setLoginButtonVisibility(true)
			}
		}
	}

	private fun composeEmail(addresses: Array<String>, subject: String, text: String) {
		val intent = Intent(Intent.ACTION_SEND).apply {
			//data = Uri.parse("mailto:")  // only choose from email apps
			type = "message/rfc822"
			putExtra(Intent.EXTRA_EMAIL, addresses)
			putExtra(Intent.EXTRA_SUBJECT, subject)
			putExtra(Intent.EXTRA_TEXT, text)
		}
		if (intent.resolveActivity(packageManager) != null) {
			startActivity(intent)
		}
	}

	private fun setLoginButtonVisibility(loggedIn: Boolean) {
		if (loggedIn) {
			buttonLogin.isVisible = false
			buttonLogout.isVisible = true
		} else {
			buttonLogin.isVisible = true
			buttonLogout.isVisible = false
		}
	}

}