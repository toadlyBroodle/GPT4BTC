package org.bitanon.chatgpt3

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AccountActivity: AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.account_activity)

		// initialize billing
		Billing.init(this, lifecycleScope)
	}

	@SuppressLint("ClickableViewAccessibility")
	override fun onStart() {
		super.onStart()

		// on account button click
		findViewById<Button>(R.id.button_login).setOnClickListener {

			// launch FirebaseUIActivity
			val startActivity = Intent(this, FirebaseUIActivity::class.java)
			startActivity(startActivity)
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
}