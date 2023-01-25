package org.bitanon.chatgpt3

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.bitanon.chatgpt3.databinding.ActivityUpgradeBinding


private const val TAG = "UpgradeActivity"
class UpgradeActivity : AppCompatActivity() {

	private lateinit var binding: ActivityUpgradeBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityUpgradeBinding.inflate(layoutInflater)
		setContentView(binding.root)

		supportActionBar?.setDisplayHomeAsUpEnabled(true)

	}

	@SuppressLint("ClickableViewAccessibility", "SetTextI18n")
	override fun onStart() {
		super.onStart()

		// button send ln payment on click listener
		val buttonSendLNPayment = binding.upgradeButtonSendLnPayment
		buttonSendLNPayment.setOnClickListener {
			FirebaseAnalytics.logCustomEvent(UPGRADE_LN_PAYMENT_SEND_CLICK)

			lifecycleScope.launch {
				val albyCallback = RequestRepository.getLNInvoice(Firestore.getUserEmail())
				sendLNPayment(albyCallback.pr)
			}
		}

		// Make links clickable and log clicks
		val linkPhoenix = binding.upgradeLinkPhoenix
		linkPhoenix.movementMethod = LinkMovementMethod.getInstance()
		linkPhoenix.setOnTouchListener { v, event ->
			when (event?.action) {
				MotionEvent.ACTION_DOWN ->
					FirebaseAnalytics.logCustomEvent(UPGRADE_LN_WALLET_NONCUSTODIAL_CLICK)
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
		val linkSendManualLNPayment = binding.manualTransactionLnurlAddress
		linkSendManualLNPayment.setTextIsSelectable(true)
		linkSendManualLNPayment.movementMethod = LinkMovementMethod.getInstance()
		linkSendManualLNPayment.setOnTouchListener { v, event ->
			when (event?.action) {
				MotionEvent.ACTION_DOWN -> {
					FirebaseAnalytics.logCustomEvent(UPGRADE_LN_MANUAL_PAYMENT_SEND_CLICK)
				}
			}
			v?.onTouchEvent(event) ?: true
		}

		// add account email to required info description
		val tvReqDescComment = binding.manualTransactionLnurlComment
		tvReqDescComment.text = "GPT4BTC:${Firestore.getUserEmail()}"

	}

	private fun sendLNPayment(uri: String?) {

		val intent = Intent(Intent.ACTION_VIEW).apply {
			data = Uri.parse("lightning:$uri")
		}
		if (intent.resolveActivity(packageManager) != null) {
			startActivity(intent)
		}
	}
}