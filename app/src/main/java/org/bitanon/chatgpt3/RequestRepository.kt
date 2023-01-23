package org.bitanon.chatgpt3

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.theokanning.openai.OpenAiService
import com.theokanning.openai.completion.CompletionRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val MODEL = "text-davinci-003"
private const val TIMEOUT = 30 //secs

//private const val TAG = "RequestRepository"
class RequestRepository {

	companion object {

		suspend fun queryOpenAI(
			p: String
		): List<String>? {

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
						MainActivity.firestore.consumePurchasedWords(wordsToConsume)
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

	}
}

// Represents different states for the latest OpenAI result
/*
sealed class LatestResultUiState {
	data class Success(val result: List<String>): LatestResultUiState()
	data class Error(val exception: Throwable): LatestResultUiState()
}*/
