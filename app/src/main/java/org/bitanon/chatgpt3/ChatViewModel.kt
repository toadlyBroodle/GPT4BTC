package org.bitanon.chatgpt3

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class ChatViewModel(): ViewModel() {

	private val requestRepository = RequestRepository()

	// Backing property to avoid state updates from other classes
	private val _uiState = MutableStateFlow(emptyList<String>())
	// The UI collects from this StateFlow to get its state updates
	val uiState: StateFlow<List<String>> = _uiState

	fun sendPrompt(p: String) {
		// Create a new coroutine on the UI thread
		val job = viewModelScope.launch {
			// Make the network call and suspend execution until it finishes
			var result: List<String>? = null
			try {
				result = requestRepository.queryOpenAI(p)
			} catch (e: IOException) {
				println("IOException: %s\n%s".format(e.cause, e.message))
			}

			if (result != null) {
				_uiState.value = result
			}

		}
	}
}