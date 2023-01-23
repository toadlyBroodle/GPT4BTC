package org.bitanon.chatgpt3

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val TAG = "ChatViewModel"
class ChatViewModel : ViewModel() {

	// Backing property to avoid state updates from other classes
	private val _uiState = MutableStateFlow(emptyList<String>())
	// The UI collects from this StateFlow to get its state updates
	val uiState: StateFlow<List<String>> = _uiState

	fun sendPrompt(activ: Activity, p: String) {

		// Create a new coroutine on the UI thread
		viewModelScope.launch { // can get job with: val job =
			// Make the network call and suspend execution until it finishes
			var result: List<String>? = null
			try {
				result = RequestRepository.queryOpenAI(activ, p)
			} catch (e: Exception) {
				Log.e(TAG, "Error request result: $e")

				if (e.message?.contains("timeout", ignoreCase = true) == true) {
					FirebaseAnalytics.logCustomEvent(EXCEPTION_SOCKET_TIMEOUT)
					result = listOf(activ.getString(R.string.exception_socket_timeout))
				} else
				if (e.message?.contains("HTTP 401") == true) {
					FirebaseAnalytics.logCustomEvent(OPENAI_UNAUTHORIZED_ACCESS)
					result = listOf(activ.getString(R.string.exception_unauthorized_access) +
							"\n" + activ.getString(R.string.chatgpt3_playstore_link))
				}
			}

			if (result != null) {
				_uiState.value = result
			}

		}
	}
}