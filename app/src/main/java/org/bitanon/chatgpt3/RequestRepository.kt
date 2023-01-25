package org.bitanon.chatgpt3

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.theokanning.openai.OpenAiService
import com.theokanning.openai.completion.CompletionRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject
import ru.gildor.coroutines.okhttp.await

private const val MODEL = "text-davinci-003"
private const val TIMEOUT = 30 //secs
private const val REQUEST_LNURL = "https://getalby.com/lnurlp/bitanon/callback?amount=1000000&comment=hello" // amount is in millisats

private const val TAG = "RequestRepository"
class RequestRepository {

	companion object {

		suspend fun queryOpenAI(activ: Activity, p: String): List<String>? {

			// Move the execution of the coroutine to the I/O dispatcher
			return withContext(Dispatchers.IO) {
				val service = OpenAiService(MainActivity.buildOpenAIKey(), TIMEOUT)
				val completionRequest = CompletionRequest.builder()
					.prompt(p)
					.model(MODEL)
					.maxTokens(AccountActivity.getMaxResponseTokens())
					.build()

				val result = service.createCompletion(completionRequest)
				print(result.choices[0].text)

					val completionTokens = result.usage.completionTokens

					val listChoices = mutableListOf<String>()
					for (choice in result.choices) {
						var text = choice.text.removePrefix("\n\n")
						if (completionTokens >= AccountActivity.getMaxResponseTokens()) {
							// add elipsis and newlines to denote to ChatFrag a truncated response
							text = "$text…\n\n"
						}
						listChoices.add(text)

						// consume purchased words
						val wordsToConsume = AccountActivity.getConsumedWords(result.usage.totalTokens)
						MainActivity.firestore.consumePurchasedWords(activ, wordsToConsume)
					}

				listChoices.ifEmpty { null }
			}
		}

		fun isOnline(context: Context): Boolean {
			val connectivityManager =
				context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
			val capabilities =
				connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
			if (capabilities != null) {
				if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
					Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
					return true
				} else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
					Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
					return true
				} else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
					Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
					return true
				}
			}
			return false
		}

		suspend fun getLNInvoice(email: String?): AlbyLnurlCallback {
			// Move the execution of the coroutine to the I/O dispatcher
			return withContext(Dispatchers.IO) {

				val url = REQUEST_LNURL.replace("comment=hello", "comment=GPT4BTC:$email")

				val client = OkHttpClient()
				val request = Request.Builder().url(url).build()
				val response = client.newCall(request).await()

				val albyCallback = parseAlbyLnurlCallback(response.body()?.string())
				Log.d(TAG, "Successful Response: $url\nAlbyCallback=$albyCallback")

				albyCallback
			}

		}

	}
}

@Keep // Do not obfuscate! Variable names are needed for parsers
data class AlbyLnurlCallback(
	val status: String,
	val successAction: JSONObject,
	val verify: String,
	val routes: List<String>,
	val pr: String
	)
fun parseAlbyLnurlCallback(json: String?): AlbyLnurlCallback {
	val typeToken = object : TypeToken<AlbyLnurlCallback>() {}.type
	return Gson().fromJson(json, typeToken)
}
