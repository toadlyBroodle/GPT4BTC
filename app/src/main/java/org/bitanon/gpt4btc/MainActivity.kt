package org.bitanon.gpt4btc

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.bitanon.gpt4btc.databinding.ActivityMainBinding

const val SHARED_PREFS = "CHATGPT3_SHARED_PREFS"
const val PREF_SHOW_TERMS = "pref_show_terms_on_start"
const val PREF_DICTATION_AUTO = "pref_dictation_auto"
const val PREF_DICTATION_SPEED = "pref_dictation_speed"

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {

	private lateinit var appBarConfiguration: AppBarConfiguration
	private lateinit var binding: ActivityMainBinding
	private val viewModel: ChatViewModel by viewModels()


	@SuppressLint("ClickableViewAccessibility")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		setSupportActionBar(binding.toolbar)

		val navController = findNavController(R.id.nav_host_fragment_content_main)
		appBarConfiguration = AppBarConfiguration(navController.graph)
		setupActionBarWithNavController(navController, appBarConfiguration)

		// Create a ViewModel the first time the system calls an activity's onCreate() method.
		// Re-created activities receive the same ChatViewModel instance created by the first activity.
		// Use the 'by viewModels()' Kotlin property delegate from the activity-ktx artifact
		lifecycleScope.launch {
			repeatOnLifecycle(Lifecycle.State.STARTED) {
				viewModel.uiState.collect {
				}
			}
		}

		// initialize ads
		AdMob.init(this)

		// load prefs
		loadPrefs(this)

		// prompt user to accept terms agreement if not previously hidden
		if (prefShowTerms) {
			FirebaseAnalytics.logCustomEvent(TERMS_AGREEMENT_SHOW)

			// inflate alertdialog layout
			val alertDialogLayout = View.inflate(this,
				R.layout.activity_terms, null)

			// Make links clickable and log clicks
			val linkToU = alertDialogLayout.findViewById<TextView>(R.id.app_link_terms_of_use)
			linkToU.movementMethod = LinkMovementMethod.getInstance()
			linkToU.setOnTouchListener { v, event ->
				when (event?.action) {
					MotionEvent.ACTION_DOWN ->
						FirebaseAnalytics.logCustomEvent(APP_PRIVACY_TERMS_OF_USE)
				}
				v?.onTouchEvent(event) ?: true
			}
			val linkPP = alertDialogLayout.findViewById<TextView>(R.id.app_link_privacy_policy)
			linkPP.movementMethod = LinkMovementMethod.getInstance()
			linkPP.setOnTouchListener { v, event ->
				when (event?.action) {
					MotionEvent.ACTION_DOWN ->
						FirebaseAnalytics.logCustomEvent(APP_PRIVACY_POLICY_CLICK)
				}
				v?.onTouchEvent(event) ?: true
			}
			val openaiLinkToU = alertDialogLayout.findViewById<TextView>(R.id.openai_link_terms_of_use)
			openaiLinkToU.movementMethod = LinkMovementMethod.getInstance()
			openaiLinkToU.setOnTouchListener { v, event ->
				when (event?.action) {
					MotionEvent.ACTION_DOWN ->
						FirebaseAnalytics.logCustomEvent(OPENAI_TERMS_OF_USE_CLICK)
				}
				v?.onTouchEvent(event) ?: true
			}
			val openaiLinkPP = alertDialogLayout.findViewById<TextView>(R.id.openai_link_privacy_policy)
			openaiLinkPP.movementMethod = LinkMovementMethod.getInstance()
			openaiLinkPP.setOnTouchListener { v, event ->
				when (event?.action) {
					MotionEvent.ACTION_DOWN ->
						FirebaseAnalytics.logCustomEvent(OPENAI_PRIVACY_POLICY_CLICK)
				}
				v?.onTouchEvent(event) ?: true
			}

			// inflate show terms checkbox and set ischecked same as preference
			val showTermsCheckbox = alertDialogLayout.findViewById<View>(
				R.id.show_terms_checkbox) as CheckBox
			showTermsCheckbox.isChecked = prefShowTerms
			// get show terms checked state
			showTermsCheckbox.setOnCheckedChangeListener { _, isChecked ->
				prefShowTerms = isChecked
			}

			val d: AlertDialog = AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(getString(R.string.alertdialog_terms_agreement_title))
				.setPositiveButton(
					getString(R.string.accept)
				) { _, _ ->
					// user accepts terms: log event
					FirebaseAnalytics.logCustomEvent(BUTTON_ACCEPT_TERMS)

					// save show terms check state to preferences
					savePrefs(this)
				}
				.setNegativeButton(getString(R.string.exit)) { _, _ ->
					// user rejects terms: log event and exit app
					FirebaseAnalytics.logCustomEvent(BUTTON_REJECT_TERMS)
					finishAndRemoveTask()
				}
				.setView(alertDialogLayout)
				.create()

			d.show()
		}
	}

	override fun onResume() {
		super.onResume()

		// try verifying any unsettled payments
		firestore.verifyPaymentsUnsettled(this, lifecycleScope)
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		// Inflate the menu; this adds items to the action bar if it is present.
		menuInflater.inflate(R.menu.menu_main, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		return when (item.itemId) {
			R.id.action_account -> {
				val startActivity = Intent(this, AccountActivity::class.java)
				startActivity(startActivity)
				true
			}
			R.id.action_settings -> {
				val startActivity = Intent(this, SettingsActivity::class.java)
				startActivity(startActivity)
				true
			}
			R.id.action_terms -> {
				val startActivity = Intent(this, TermsActivity::class.java)
				startActivity(startActivity)
				true
			}

			else -> super.onOptionsItemSelected(item)
		}
	}

	override fun onSupportNavigateUp(): Boolean {
		val navController = findNavController(R.id.nav_host_fragment_content_main)
		return navController.navigateUp(appBarConfiguration)
				|| super.onSupportNavigateUp()
	}

	companion object {

		val firestore = Firestore().init()

		var prefShowTerms = true
		var prefDictationSpeed = 1.0f

		// preference dictation auto state flow stuff
		private val _prefDictationAuto = MutableStateFlow(false)
		fun setPrefDictationAuto(activ: Activity, b: Boolean) {
			_prefDictationAuto.value = b
			savePrefs(activ)
		}
		val prefDictationAuto: StateFlow<Boolean> = _prefDictationAuto


		fun buildOpenAIKey(): String {
			return FirebaseAnalytics.OPENAI_KEY_PART1 + OPENAI_KEY_PART2 +
					OPENAI_KEY_PART3 + AdMob.getOpenAIKeyPart4()
		}

		fun showToast(ctx: Context, message: String) =
			Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show()

		fun showToastLong(ctx: Context, message: String) =
			Toast.makeText(ctx, message, Toast.LENGTH_LONG).show()

		fun loadPrefs(activ: Activity) {
			// load shared preferences
			val sharedPrefs = activ.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
			prefShowTerms = sharedPrefs.getBoolean(PREF_SHOW_TERMS, true)
			prefDictationSpeed = sharedPrefs.getFloat(PREF_DICTATION_SPEED, 1.0f)
			_prefDictationAuto.value = sharedPrefs.getBoolean(PREF_DICTATION_AUTO, false)
			Log.d(TAG, "preferences loaded: ${sharedPrefs.all}")
		}

		fun savePrefs(activ: Activity) {
			// save hide terms choice to shared preferences
			val editor = activ.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE).edit()
			editor.apply {
				putBoolean(PREF_SHOW_TERMS, prefShowTerms)
				putFloat(PREF_DICTATION_SPEED, prefDictationSpeed)
				putBoolean(PREF_DICTATION_AUTO, _prefDictationAuto.value)
			}.apply()
		}
	}

}