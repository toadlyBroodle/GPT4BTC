package org.bitanon.gpt4btc

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.bitanon.gpt4btc.databinding.ActivityAccountBinding
import kotlin.math.roundToInt

const val LIMIT_ANON = 60
const val LIMIT_USER = 120
const val MAX_LIMIT_PROMPT_CHARS = 1000
const val MAX_LIMIT_RESPONSE_TOKENS = 2000 // 4000 maximum tokens allowed by GPT3 text-davinci-003 model

private const val TAG = "AccountActivity"
class AccountActivity: AppCompatActivity() {

	private lateinit var binding: ActivityAccountBinding

	private lateinit var buttonLogin: Button
	private lateinit var buttonLogout: Button
	private lateinit var buttonUpgrade: Button
	private lateinit var switchBlockAds: SwitchCompat
	private lateinit var tvName: TextView
	private lateinit var tvPromptLimit: TextView
	private lateinit var tvResponseLimit: TextView
	private lateinit var tvPurchasedWords: TextView

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityAccountBinding.inflate(layoutInflater)
		setContentView(binding.root)
	}

	@SuppressLint("ClickableViewAccessibility")
	override fun onStart() {
		super.onStart()

		// get textviews
		tvName = binding.accountName
		tvPromptLimit = binding.accountPromptLimit
		tvResponseLimit = binding.accountResponseLimit
		tvPurchasedWords = binding.accountPurchasedWords
		// get block ads switch
		switchBlockAds = binding.switchBlockAds

		// get buttons, set click listeners, and log events
		buttonLogin = binding.buttonLogin
		buttonLogin.setOnClickListener {
			FirebaseAnalytics.logCustomEvent(BUTTON_LOGIN)

			// launch FirebaseAuthActivity
			val startActivity = Intent(this, FirebaseAuthActivity::class.java)
			startActivity(startActivity)
		}
		buttonLogout = binding.buttonLogout
		buttonLogout.setOnClickListener {
			FirebaseAnalytics.logCustomEvent(BUTTON_LOGOUT)

			FirebaseAuthActivity.signOut(this)

			// set button visibilities
			setButtonStates(false)

		}
		buttonUpgrade = binding.buttonUpgrade
		buttonUpgrade.setOnClickListener {
			FirebaseAnalytics.logCustomEvent(BUTTON_UPGRADE)

			// check for internet connection
			if (!RequestRepository.isOnline(baseContext)) {
				// toast user to connect
				MainActivity.showToast(baseContext,
					getString(R.string.toast_no_internet))
				return@setOnClickListener
			}

			// launch upgrade process
			val startActivity = Intent(this, UpgradeActivity::class.java)
			startActivity(startActivity)
		}

		// collect changes to logged in user
		lifecycleScope.launch {
			Firestore.userState.collect { user ->

				// user not logged in
				var userName = getString(R.string.anon)
				if (user == null) {
					setButtonStates(false)

					// disable and uncheck block ads switch
					switchBlockAds.isEnabled = false
					switchBlockAds.isChecked = false

					freePromptChars = LIMIT_ANON
					freeResponseTokens = LIMIT_ANON
				}
				else { // user logged in
					setButtonStates(true)

					// enable ad block switch, if has remaining purchased words
					if (user.purchasedWords > 0) {
						switchBlockAds.isEnabled = true
						// set ad block switch to user setting
						switchBlockAds.isChecked = Firestore.userState.value?.blockAds ?: false
					}
					else { // disable and turn off ad blocking
						switchBlockAds.isEnabled = false
						switchBlockAds.isChecked = false
					}

					// set user properties
					userName = user.displayName.toString()
					freePromptChars = LIMIT_USER
					freeResponseTokens = LIMIT_USER
				}

				// set textview properties
				// update user details ui
				tvName.text = userName
				tvPromptLimit.text = getPromptLimitWords()
				tvResponseLimit.text = getResponseLimitWords()
				tvPurchasedWords.text = Firestore.getUserPaidWords().toString()
			}
		}
	}

	override fun onResume() {
		super.onResume()

		// try verifying any unsettled payments
		MainActivity.firestore.verifyPaymentsUnsettled(this, lifecycleScope)
	}

	override fun onStop() {
		super.onStop()

		// update user's block ads setting
		MainActivity.firestore.updateUserBlockAds(switchBlockAds.isChecked)
	}

	// show/hide login/logout buttons depending on user login status
	private fun setButtonStates(loggedIn: Boolean) {
		if (loggedIn) { // user logged in
			buttonLogin.isVisible = false
			buttonLogout.isVisible = true
			buttonUpgrade.isEnabled = true

		} else { // logged out
			buttonLogin.isVisible = true
			buttonLogout.isVisible = false
			buttonUpgrade.isEnabled = false
		}
	}

	companion object {

		private var freePromptChars = LIMIT_ANON
		private var freeResponseTokens = LIMIT_ANON

		fun getMaxPromptChars(): Int {
			val maxChars = freePromptChars + (Firestore.getUserPaidWords() * 5)
			return if (maxChars > MAX_LIMIT_PROMPT_CHARS)
				MAX_LIMIT_PROMPT_CHARS // limit -> max edittext length (~400words)
			else if (Firestore.isUserLoggedIn() && maxChars < LIMIT_USER)
				LIMIT_USER // limit -> logged in user free length
			else
				maxChars // limit -> within purchased word count
		}
		fun getMaxResponseTokens(): Int {
			val maxTokens = freeResponseTokens + (Firestore.getUserPaidWords() * 1.33).roundToInt()
			Log.d(TAG, "maxResponseTokens=$maxTokens")
			return if (maxTokens > MAX_LIMIT_RESPONSE_TOKENS)
				MAX_LIMIT_RESPONSE_TOKENS
			else maxTokens
		}

		fun getConsumedWords(usedTokens: Long): Int {
			val consumed = (usedTokens - (freePromptChars / 5) - (freeResponseTokens * 0.75)).roundToInt()
			return if (consumed <= 0 )
				0
			else consumed
		}

		fun getPromptLimitWords(): String {
			// char/word ~= 5
			return (freePromptChars / 5).toString()
		}

		fun getResponseLimitWords(): String {
			// tokens/words ~= 3/4
			return (freeResponseTokens * 0.75).roundToInt().toString()
		}
	}

	/*	private fun composeEmail(addresses: Array<String>, subject: String, text: String) {
		val intent = Intent(Intent.ACTION_SEND).apply {
			//data = Uri.parse("mailto:")  // only choose from email apps, doesn't work?
			type = "message/rfc822"
			putExtra(Intent.EXTRA_EMAIL, addresses)
			putExtra(Intent.EXTRA_SUBJECT, subject)
			putExtra(Intent.EXTRA_TEXT, text)
		}
		if (intent.resolveActivity(packageManager) != null) {
			startActivity(intent)
		}
	}*/
}