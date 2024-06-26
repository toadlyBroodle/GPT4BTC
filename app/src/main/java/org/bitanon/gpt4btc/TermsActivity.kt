package org.bitanon.gpt4btc

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.MotionEvent
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import org.bitanon.gpt4btc.MainActivity.Companion.prefShowTerms
import org.bitanon.gpt4btc.databinding.ActivityTermsBinding

const val OPENAI_KEY_PART3 = "FJ8ipVot1Oj"

private const val TAG = "TermsActivity"
class TermsActivity : AppCompatActivity() {

	private lateinit var binding: ActivityTermsBinding

	private lateinit var showTermsCheckbox: CheckBox

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityTermsBinding.inflate(layoutInflater)
		setContentView(binding.root)

		supportActionBar?.setDisplayHomeAsUpEnabled(true)

	}

	@SuppressLint("ClickableViewAccessibility")
	override fun onStart() {
		super.onStart()

		AdMob.loadNewInterstitial(baseContext)

		showTermsCheckbox = binding.showTermsCheckbox

		// Make links clickable and log clicks
		val linkToU = binding.appLinkTermsOfUse
		linkToU.movementMethod = LinkMovementMethod.getInstance()
		linkToU.setOnTouchListener { v, event ->
			when (event?.action) {
				MotionEvent.ACTION_DOWN ->
					FirebaseAnalytics.logCustomEvent(APP_PRIVACY_TERMS_OF_USE)
			}
			v?.onTouchEvent(event) ?: true
		}
		val linkPP = binding.appLinkPrivacyPolicy
		linkPP.movementMethod = LinkMovementMethod.getInstance()
		linkPP.setOnTouchListener { v, event ->
			when (event?.action) {
				MotionEvent.ACTION_DOWN ->
					FirebaseAnalytics.logCustomEvent(APP_PRIVACY_POLICY_CLICK)
			}
			v?.onTouchEvent(event) ?: true
		}
		val openaiLinkToU = binding.openaiLinkTermsOfUse
		openaiLinkToU.movementMethod = LinkMovementMethod.getInstance()
		openaiLinkToU.setOnTouchListener { v, event ->
			when (event?.action) {
				MotionEvent.ACTION_DOWN ->
					FirebaseAnalytics.logCustomEvent(OPENAI_TERMS_OF_USE_CLICK)
			}
			v?.onTouchEvent(event) ?: true
		}
		val openaiLinkPP = binding.openaiLinkPrivacyPolicy
		openaiLinkPP.movementMethod = LinkMovementMethod.getInstance()
		openaiLinkPP.setOnTouchListener { v, event ->
			when (event?.action) {
				MotionEvent.ACTION_DOWN ->
					FirebaseAnalytics.logCustomEvent(OPENAI_PRIVACY_POLICY_CLICK)
			}
			v?.onTouchEvent(event) ?: true
		}

		// set show terms agreement checkbox from main preferences
		showTermsCheckbox.isChecked = prefShowTerms

	}

	override fun onPause() {
		super.onPause()

		// save show terms choice to shared preferences
		val sharedPrefs = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
		val editor = sharedPrefs.edit()
		editor.putBoolean(PREF_SHOW_TERMS, showTermsCheckbox.isChecked)
		editor.apply()
		Log.d(TAG, "preferences saved: ${sharedPrefs.all}")

		// try showing ad
		AdMob.showInterstitial(this)
	}
}