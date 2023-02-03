package org.bitanon.chatgpt3

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
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

	@SuppressLint("ClickableViewAccessibility", "SetTextI18n")
	override fun onStart() {
		super.onStart()

		// button send ln payment on click listener
		val buttonSendLNPayment = binding.upgradeButtonSendLnPayment
		buttonSendLNPayment.setOnClickListener {
			// inflate dialog layout
			val dialogPurchaseAmount = View.inflate(this,
				R.layout.dialog_purchase_amount, null)
			// get edittext amount
			val etAmount = dialogPurchaseAmount.findViewById<EditText>(
				R.id.dialog_purchase_words_amount_edittext)

			// show dialog to get payment amount
			val d: AlertDialog = AlertDialog.Builder(this)
				.setTitle(getString(R.string.purchase_words))
				.setPositiveButton(
					getString(R.string.send_ln_payment)
				) { _, _ ->
					// user clicks send ln payment: log event
					FirebaseAnalytics.logCustomEvent(UPGRADE_LN_PAYMENT_SEND_CLICK)

					lifecycleScope.launch {
						// get payment amount
						val amount = etAmount.text.toString().toLong()
						// get ln invoice from Alby
						val albyLnurlCallback = RequestRepository.getLNInvoice(Firestore.getUserEmail(), amount)
						val verifyUrl = albyLnurlCallback?.verify
						// get verify response from Alby
						val albyLNVerifyResponse = RequestRepository.getLNPaymentVerify(verifyUrl)
						// get new payment verify object
						if (albyLNVerifyResponse != null) {
							// add payment to database
							MainActivity.firestore.addLNPayment(verifyUrl, amount, albyLNVerifyResponse)
							// launch intent to device's ln wallet
							sendLNPayment(albyLNVerifyResponse.pr)

						} else {
							MainActivity.showToastLong(this@UpgradeActivity,
								getString(R.string.toast_purchase_words_verification_error))
						}
					}
				}
				.setNegativeButton(getString(R.string.cancel)) { _, _ ->}
				.setView(dialogPurchaseAmount)
				.create()
			d.show()
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