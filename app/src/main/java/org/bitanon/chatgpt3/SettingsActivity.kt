package org.bitanon.chatgpt3

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope

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

		// initialize billing
		Billing.init(this, lifecycleScope)
	}

	override fun onStart() {
		super.onStart()

		// Make links clickable
		findViewById<TextView>(R.id.terms_of_use_link).movementMethod =
			LinkMovementMethod.getInstance()
		findViewById<TextView>(R.id.privacy_policy_link).movementMethod =
			LinkMovementMethod.getInstance()

		// load shared preferences
		val sharedPrefs = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
		prefShowTerms = sharedPrefs.getBoolean(PREF_SHOW_TERMS, true)
		// set show terms agreement checkbox from preferences
		showTermsCheckbox = findViewById(R.id.show_terms_checkbox)
		showTermsCheckbox.isChecked = prefShowTerms
		Log.d(TAG, "preferences loaded: ${sharedPrefs.all}")

		findViewById<Button>(R.id.button_subscribe).setOnClickListener {
			Firebase.logCustomEvent(BUTTON_SUBSCRIBE)

			Billing.subscribe(this, lifecycleScope)
		}

		findViewById<Button>(R.id.button_join_testers).setOnClickListener {
			Firebase.logCustomEvent(BUTTON_JOIN_TEST_GROUP)

			composeEmail(arrayOf("anon@bitanon.org"),
				getString(R.string.join_testers),
				getString(R.string.require_gmail))
		}
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

/*	class SettingsFragment : PreferenceFragmentCompat() {
		override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
			setPreferencesFromResource(R.xml.root_preferences, rootKey)
		}
	}*/
}