package org.bitanon.chatgpt3

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val TAG = "ChatViewModel"
class ChatViewModel(): ViewModel() {

	val requestRepository = RequestRepository()

	// Backing property to avoid state updates from other classes
	private val _uiState = MutableStateFlow(emptyList<String>())
	// The UI collects from this StateFlow to get its state updates
	val uiState: StateFlow<List<String>> = _uiState

	fun sendPrompt(p: String) {

		// Create a new coroutine on the UI thread
		viewModelScope.launch { // can get job with: val job =
			// Make the network call and suspend execution until it finishes
			var result: List<String>? = null
			try {
				result = requestRepository.queryOpenAI(p)
			} catch (e: Exception) {
				Log.e(TAG, "Error request result: $e")
				if (e.message?.contains("HTTP 401") == true) {
					Firebase.logCustomEvent(OPENAI_UNAUTHORIZED_ACCESS)
					throw Exception ("HTTP 401: openai unauthorized access")
				}
			}

			if (result != null) {
				_uiState.value = result
			}

		}
	}
}