package org.bitanon.chatgpt3

import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
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
import kotlinx.coroutines.launch
import org.bitanon.chatgpt3.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
	private val TAG = "MainActivity"

	private lateinit var appBarConfiguration: AppBarConfiguration
	private lateinit var binding: ActivityMainBinding

	// Using the viewModels() Kotlin property delegate from the activity-ktx
	// artifact to retrieve the ViewModel in the activity scope
	private val viewModel: ChatViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		AdMob.init(this)

		setSupportActionBar(binding.toolbar)

		val navController = findNavController(R.id.nav_host_fragment_content_main)
		appBarConfiguration = AppBarConfiguration(navController.graph)
		setupActionBarWithNavController(navController, appBarConfiguration)

/*		binding.fab.setOnClickListener { view ->
			Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
				.setAction("Action", null).show()
		}*/

		// Create a ViewModel the first time the system calls an activity's onCreate() method.
		// Re-created activities receive the same ChatViewModel instance created by the first activity.
		// Use the 'by viewModels()' Kotlin property delegate from the activity-ktx artifact
		lifecycleScope.launch {
			repeatOnLifecycle(Lifecycle.State.STARTED) {
				viewModel.uiState.collect {
				}
			}
		}

		// get openai links
		val message = SpannableString(getString(R.string.privacy_agreement_message)
				+ "\n" + getString(R.string.openai_link_terms_of_use)
				+ "\n" + getString(R.string.openai_link_privacy_policy))
		Linkify.addLinks(message, Linkify.WEB_URLS)

		val d: AlertDialog = AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(getString(R.string.terms_agreement))
			.setPositiveButton(
				getString(R.string.accept)
			) { dialog, which ->
				Log.d(TAG, "User accepted terms agreement")
			}
			.setNegativeButton(getString(R.string.exit)) { dialog, which ->
				// user rejects, exit app
				finishAndRemoveTask()
			}
			.setMessage(message)
			.create()

		d.show()
		// Make the textview clickable. Must be called after show()
		(d.findViewById<View>(android.R.id.message) as TextView?)!!.movementMethod =
			LinkMovementMethod.getInstance()
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
			R.id.action_settings -> true
			else -> super.onOptionsItemSelected(item)
		}
	}

	override fun onSupportNavigateUp(): Boolean {
		val navController = findNavController(R.id.nav_host_fragment_content_main)
		return navController.navigateUp(appBarConfiguration)
				|| super.onSupportNavigateUp()
	}
}