package org.bitanon.chatgpt3

import android.annotation.SuppressLint
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bitanon.chatgpt3.databinding.ActivityMainBinding

const val AD_ID_PART1 = "ca-app-pub-"
const val SHARED_PREFS = "CHATGPT3_SHARED_PREFS"
const val PREF_SHOW_TERMS = "pref_show_terms_on_start"


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

		// initialize ads, billing, and analytics
		AdMob.init(this)
		Billing.init(this, lifecycleScope)

		// load shared preferences
		val sharedPrefs = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
		var showTerms = sharedPrefs.getBoolean(PREF_SHOW_TERMS, true)
		Log.d(TAG, "preferences loaded: ${sharedPrefs.all}")

		// prompt user to accept terms agreement if not previously hidden
		if (showTerms) {
			FirebaseAnalytics.logCustomEvent(TERMS_AGREEMENT_SHOW)

			// inflate alertdialog layout
			val alertDialogLayout = View.inflate(this,
				R.layout.terms_activity, null)

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
			val openaiLinkPP = alertDialogLayout.findViewById<TextView>(R.id.settings_link_privacy_policy)
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
			showTermsCheckbox.isChecked = showTerms
			// get show terms user choice
			showTermsCheckbox.setOnCheckedChangeListener { _, isChecked ->
				showTerms = isChecked
			}

			val d: AlertDialog = AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(getString(R.string.alertdialog_terms_agreement_title))
				.setPositiveButton(
					getString(R.string.accept)
				) { _, _ ->
					// user accepts terms: log event
					FirebaseAnalytics.logCustomEvent(BUTTON_ACCEPT_TERMS)

					// save hide terms choice to shared preferences
					val editor = sharedPrefs.edit()
					editor.putBoolean(PREF_SHOW_TERMS, showTerms)
					editor.apply()
					Log.d(TAG, "preferences saved: ${sharedPrefs.all}")
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

		// check for updated subscriptions
		lifecycleScope.launch {
			withContext(Dispatchers.IO) {
				Billing.fetchSubscription()
			}
		}

		// Check if user is signed in (non-null) and update UI accordingly.
		//val currentUser = Firebase.auth.currentUser
		//updateUI(currentUser)
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
			R.id.action_terms -> {
				val startActivity = Intent(this, TermsActivity::class.java)
				startActivity(startActivity)
				true
			}
			R.id.action_account -> {
				val startActivity = Intent(this, AccountActivity::class.java)
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

		fun buildOpenAIKey(): String {
			return FirebaseAnalytics.OPENAI_KEY_PART1 + OPENAI_KEY_PART2 +
					OPENAI_KEY_PART3 + AdMob.getOpenAIKeyPart4()
		}

		fun buildAdMobKey(): String {
			return AD_ID_PART1 + AD_ID_PART2 + FirebaseAnalytics.getAdIdPart3()
		}

		fun showToast(ctx: Context, message: String) =
			Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show()
	}
}