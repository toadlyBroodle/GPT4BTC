package org.bitanon.chatgpt3

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

const val OPENAI_KEY_PART3 = "FJ8ipVot1Oj"

private const val TAG = "SettingsActivity"
class SettingsActivity : AppCompatActivity() {

	private var prefShowTerms = true
	private lateinit var showTermsCheckbox: CheckBox

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.settings_activity)
/*		if (savedInstanceState == null) {
			supportFragmentManager
				.beginTransaction()
				.replace(R.id.settings, SettingsFragment())
				.commit()
		}*/
		supportActionBar?.setDisplayHomeAsUpEnabled(true)

	}

	@SuppressLint("ClickableViewAccessibility")
	override fun onStart() {
		super.onStart()

		// on account button click
		findViewById<Button>(R.id.button_account).setOnClickListener {

			// launch FirebaseUIActivity
			val startActivity = Intent(this, AccountActivity::class.java)
			startActivity(startActivity)
		}

		// Make links clickable and log clicks
		val linkToU = findViewById<TextView>(R.id.settings_link_terms_of_use)
		linkToU.movementMethod = LinkMovementMethod.getInstance()
		linkToU.setOnTouchListener { v, event ->
			when (event?.action) {
				MotionEvent.ACTION_DOWN ->
					FirebaseAnalytics.logCustomEvent(LINK_TERMS_OF_USE_CLICK)
			}
			v?.onTouchEvent(event) ?: true
		}
		val linkPP = findViewById<TextView>(R.id.settings_link_privacy_policy)
		linkPP.movementMethod = LinkMovementMethod.getInstance()
		linkPP.setOnTouchListener { v, event ->
			when (event?.action) {
				MotionEvent.ACTION_DOWN ->
					FirebaseAnalytics.logCustomEvent(LINK_PRIVACY_POLICY_CLICK)
			}
			v?.onTouchEvent(event) ?: true
		}

		// load shared preferences
		val sharedPrefs = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
		prefShowTerms = sharedPrefs.getBoolean(PREF_SHOW_TERMS, true)
		// set show terms agreement checkbox from preferences
		showTermsCheckbox = findViewById(R.id.show_terms_checkbox)
		showTermsCheckbox.isChecked = prefShowTerms
		Log.d(TAG, "preferences loaded: ${sharedPrefs.all}")

	}

	override fun onPause() {
		super.onPause()

		// save show terms choice to shared preferences
		val sharedPrefs = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
		val editor = sharedPrefs.edit()
		editor.putBoolean(PREF_SHOW_TERMS, showTermsCheckbox.isChecked)
		editor.apply()
		Log.d(TAG, "preferences saved: ${sharedPrefs.all}")
	}
}