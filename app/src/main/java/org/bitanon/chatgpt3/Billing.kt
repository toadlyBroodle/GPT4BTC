package org.bitanon.chatgpt3

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*

val SUBSCRIPTION_PRODUCT_ID = "chatgpt3_subscription"

class Billing {

	companion object {
		private val TAG = "Billing"

		var billingClient: BillingClient? = null
		var subscriptionDetails: ProductDetails? = null

		fun init(ctx: Context): Companion { //, lifecycleScope: LifecycleCoroutineScope) {

			val purchasesUpdatedListener =
				PurchasesUpdatedListener { billingResult, purchases ->
					if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
						for (purchase in purchases) {
							// TODO handlePurchase(purchase)
						}
					} else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
						// Handle an error caused by a user cancelling the purchase flow.
						Log.d(TAG, "User cancelled purchase flow")
					} else {
						// Handle any other error codes.
						Log.d(TAG, "Error BillingResponseCode: " + billingResult.responseCode)
					}

				}

			// get billingClient
			billingClient = BillingClient.newBuilder(ctx)
				.setListener(purchasesUpdatedListener)
				.enablePendingPurchases()
				.build()

			// connect to Google Play
			billingClient!!.startConnection(object : BillingClientStateListener {
				override fun onBillingSetupFinished(billingResult: BillingResult) {
					if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
						Log.d(TAG, "The BillingClient is ready")
					} else Log.d(TAG, "billingResult: " + billingResult)
				}
				override fun onBillingServiceDisconnected() {
					Log.d(TAG, "The billing service disconnected")
					// Try to restart the connection on the next request to
					// Google Play by calling the startConnection() method.
				}
			})

			// get subscription available to buy
			val queryProductDetailsParams =
				QueryProductDetailsParams.newBuilder()
					.setProductList(
						listOf(
							QueryProductDetailsParams.Product.newBuilder()
								.setProductId(SUBSCRIPTION_PRODUCT_ID)
								.setProductType(BillingClient.ProductType.SUBS)
								.build()))
					.build()

			billingClient!!.queryProductDetailsAsync(queryProductDetailsParams) {
					billingResult,
					productDetailsList ->
				Log.d(TAG, "billingResult: " + billingResult.responseCode)

				// get subscription details, if product list not empty
				if (productDetailsList.isNotEmpty())
					subscriptionDetails = productDetailsList[0]
			}

			// do same as above with kotlin extensions?
			/*lifecycleScope.launch {
				val product = Product.newBuilder().setProductId(SUBSCRIPTION_PRODUCT_ID)
					.setProductType(BillingClient.ProductType.SUBS)
				val productList = ArrayList<Product>()
				productList.add(product.build())

				val params = QueryProductDetailsParams.newBuilder()
				params.setProductList(productList)

				// leverage queryProductDetails Kotlin extension function
				productDetailsResult = withContext(Dispatchers.IO) {
					billingClient!!.queryProductDetails(params.build())
				}

				val firstProductDetails = productDetailsResult.productDetailsList[0]

				val productDetailsParamsList = listOf(
					BillingFlowParams.ProductDetailsParams.newBuilder()
						// retrieve a value for "productDetails" by calling queryProductDetailsAsync()
						.setProductDetails(firstProductDetails)
						// to get an offer token, call ProductDetails.subscriptionOfferDetails()
						// for a list of offers that are available to the user
						.setOfferToken(firstProductDetails.subscriptionOfferDetails.toString())
						.build()
				)

				val billingFlowParams = BillingFlowParams.newBuilder()
					.setProductDetailsParamsList(productDetailsParamsList)
					.build()

				// Launch the billing flow
				val billingResult = billingClient!!.launchBillingFlow(ctx as Activity, billingFlowParams)

				Log.d(TAG, "billingResult: " + billingResult.responseCode)
			}*/
			return this
		}

		fun subscribe(activ: Activity) {

			if (subscriptionDetails == null) {
				MainActivity.showToast(activ.baseContext, activ.baseContext.getString(R.string.toast_billing_error))
					return
			}

			val productDetailsParamsList = listOf(
				BillingFlowParams.ProductDetailsParams.newBuilder()
						// retrieve a value for "productDetails" by calling queryProductDetailsAsync()
						.setProductDetails(subscriptionDetails!!)
						// to get an offer token, call ProductDetails.subscriptionOfferDetails()
						// for a list of offers that are available to the user
						.setOfferToken(subscriptionDetails!!.subscriptionOfferDetails.toString())
						.build()
			)

			val billingFlowParams = BillingFlowParams.newBuilder()
				.setProductDetailsParamsList(productDetailsParamsList)
				.build()

			// Launch the billing flow
			val billingResult = billingClient?.launchBillingFlow(activ, billingFlowParams)

		}
	}
}