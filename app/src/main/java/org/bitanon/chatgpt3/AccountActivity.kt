package org.bitanon.chatgpt3

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.bitanon.chatgpt3.databinding.AccountActivityBinding
import kotlin.math.roundToInt

class AccountActivity: AppCompatActivity() {

	private lateinit var binding: AccountActivityBinding

	private lateinit var buttonLogin: Button
	private lateinit var buttonLogout: Button
	private lateinit var tvName: TextView
	private lateinit var tvPromptLimit: TextView
	private lateinit var tvResponseLimit: TextView

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = AccountActivityBinding.inflate(layoutInflater)
		setContentView(binding.root)

		// initialize billing
		Billing.init(this, lifecycleScope)
	}

	@SuppressLint("ClickableViewAccessibility", "SetTextI18n")
	override fun onStart() {
		super.onStart()

		// get textviews
		tvName = binding.accountName
		tvPromptLimit = binding.accountPromptLimit
		tvResponseLimit = binding.accountResponseLimit

		// get buttons, set click listeners, and log events
		buttonLogin = binding.buttonLogin
		buttonLogin.setOnClickListener {
			FirebaseAnalytics.logCustomEvent(BUTTON_LOGIN)

			// launch FirebaseUIActivity
			val startActivity = Intent(this, FirebaseUIActivity::class.java)
			startActivity(startActivity)
		}
		buttonLogout = binding.buttonLogout
		buttonLogout.setOnClickListener {
			FirebaseAnalytics.logCustomEvent(BUTTON_LOGOUT)

			FirebaseUIActivity.signOut(this)

			// set button visibilities
			setLoginButtonVisibility(false)

		}
		binding.buttonJoinTesters.setOnClickListener {
			FirebaseAnalytics.logCustomEvent(BUTTON_JOIN_TEST_GROUP)

			composeEmail(
				arrayOf("anon@bitanon.org"),
				getString(R.string.join_testers),
				getString(R.string.require_gmail)
			)
		}
		binding.buttonSubscribe.setOnClickListener {
			FirebaseAnalytics.logCustomEvent(BUTTON_SUBSCRIBE)

			// check for internet connection
			if (!RequestRepository.isOnline(baseContext)) {
				// toast user to connect
				MainActivity.showToast(baseContext,
					getString(R.string.toast_no_internet))
				return@setOnClickListener
			}

			// launch subscription process
			lifecycleScope.launch {
				Billing.subscribe(this@AccountActivity, lifecycleScope)
			}
		}


		// collect changes to logged in user
		lifecycleScope.launch {
			FirebaseUIActivity.userState.collect { user ->

				var userName = getString(R.string.anon)

				if (user == null) {
					setLoginButtonVisibility(false)

					maxPromptChars = FirebaseAnalytics.getPromptMaxChars()
					maxResponseTokens = FirebaseAnalytics.getResponseMaxTokens()
				}
				else {
					setLoginButtonVisibility(true)

					// set user properties
					userName = user.displayName.toString()
					maxPromptChars = 80
					maxResponseTokens = 80
				}

				// set textview properties
				// update user details ui
				tvName.text = userName
				tvPromptLimit.text = "~${getPromptLimitWords()} ${getString(R.string.words)}"
				tvResponseLimit.text = "~${getResponseLimitWords()} ${getString(R.string.words)}"
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

	// show/hide login/logout buttons depending on user login status
	private fun setLoginButtonVisibility(loggedIn: Boolean) {
		if (loggedIn) {
			buttonLogin.isVisible = false
			buttonLogout.isVisible = true
		} else {
			buttonLogin.isVisible = true
			buttonLogout.isVisible = false
		}
	}

	companion object {

		private var maxPromptChars = FirebaseAnalytics.getResponseMaxTokens()
		fun getMaxPromptChars(): Int { return maxPromptChars}
		private var maxResponseTokens = FirebaseAnalytics.getPromptMaxChars()
		fun getMaxResponseTokens(): Int { return maxResponseTokens}

		fun getPromptLimitWords(): String {
			return (maxPromptChars / 5).toString()
		}

		fun getResponseLimitWords(): String {
			return (maxResponseTokens * 0.75).roundToInt().toString()
		}
	}

}