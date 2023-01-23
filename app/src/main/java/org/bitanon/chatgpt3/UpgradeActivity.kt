package org.bitanon.chatgpt3

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import org.bitanon.chatgpt3.databinding.ActivityUpgradeBinding


//private const val TAG = "UpgradeActivity"
class UpgradeActivity : AppCompatActivity() {

	private lateinit var binding: ActivityUpgradeBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityUpgradeBinding.inflate(layoutInflater)
		setContentView(binding.root)

		supportActionBar?.setDisplayHomeAsUpEnabled(true)

	}

	@SuppressLint("ClickableViewAccessibility")
	override fun onStart() {
		super.onStart()

		// add account email to required info description
		val tvReqDescAccEmail = binding.requiredDescriptionAccountEmail
		tvReqDescAccEmail.text = getString(R.string.purchase_required_info).format(Firestore.getUserEmail())

		// button send ln payment on click listener
		val buttonSendLNPayment = binding.upgradeButtonSendLnPayment
		buttonSendLNPayment.setOnClickListener {
			sendLNPayment()
		}

		// Make links clickable and log clicks
		val linkSendLNPayment = binding.upgradeLinkSendLightningPayment
		linkSendLNPayment.setTextIsSelectable(true)
		linkSendLNPayment.movementMethod = LinkMovementMethod.getInstance()
		linkSendLNPayment.setOnTouchListener { v, event ->
			when (event?.action) {
				MotionEvent.ACTION_DOWN -> {
					FirebaseAnalytics.logCustomEvent(UPGRADE_LN_PAYMENT_SEND_CLICK)
				}
			}
			v?.onTouchEvent(event) ?: true
		}
		val linkGetAlby = binding.upgradeLinkGetalby
		linkGetAlby.movementMethod = LinkMovementMethod.getInstance()
		linkGetAlby.setOnTouchListener { v, event ->
			when (event?.action) {
				MotionEvent.ACTION_DOWN ->
					FirebaseAnalytics.logCustomEvent(UPGRADE_LN_WALLET_GETALBY_CLICK)
			}
			v?.onTouchEvent(event) ?: true
		}
		val linkNonCustodialWallets = binding.upgradeLinkNoncustodialLightningWallets
		linkNonCustodialWallets.movementMethod = LinkMovementMethod.getInstance()
		linkNonCustodialWallets.setOnTouchListener { v, event ->
			when (event?.action) {
				MotionEvent.ACTION_DOWN ->
					FirebaseAnalytics.logCustomEvent(UPGRADE_LN_WALLET_NONCUSTODIAL_CLICK)
			}
			v?.onTouchEvent(event) ?: true
		}

	}

	private fun sendLNPayment() {
		val intent = Intent(Intent.ACTION_VIEW).apply {
			data = Uri.parse("lightning:bitanon@getalby.com")  // only choose from lightning wallets
		}
		if (intent.resolveActivity(packageManager) != null) {
			startActivity(intent)
		}
	}
}