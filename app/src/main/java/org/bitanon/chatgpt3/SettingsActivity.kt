package org.bitanon.chatgpt3

import android.content.Context
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
	private val TAG = "SettingsActivity"

	var prefShowTerms = true
	lateinit var showTermsCheckbox: CheckBox

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

	override fun onStart() {
		super.onStart()

		// Make links clickable
		(findViewById<View>(R.id.terms_of_use_link) as TextView?)!!.movementMethod =
			LinkMovementMethod.getInstance()
		(findViewById<View>(R.id.privacy_policy_link) as TextView?)!!.movementMethod =
			LinkMovementMethod.getInstance()

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

/*	class SettingsFragment : PreferenceFragmentCompat() {
		override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
			setPreferencesFromResource(R.xml.root_preferences, rootKey)
		}
	}*/
}