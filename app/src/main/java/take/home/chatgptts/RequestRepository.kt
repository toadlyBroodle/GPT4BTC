package take.home.chatgptts

import com.theokanning.openai.OpenAiService
import com.theokanning.openai.completion.CompletionRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RequestRepository {

	val OPENAI_KEY = "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"
	val MODEL = "text-davinci-003"
	val MAX_TOKENS = 32

	suspend fun queryOpenAI(
		p: String
	): List<String> {

		// Move the execution of the coroutine to the I/O dispatcher
		return withContext(Dispatchers.IO) {
			val service = OpenAiService(OPENAI_KEY)
			val completionRequest = CompletionRequest.builder()
				.prompt(p)
				.model(MODEL)
				.maxTokens(MAX_TOKENS)
				.build()

			val result = service.createCompletion(completionRequest)
			print(result)

			val completionTokens = result.usage.completionTokens

			val listChoices = mutableListOf<String>()
			for (choice in result.choices) {
				var text = choice.text.removePrefix("\n\n")
				if (completionTokens >= MAX_TOKENS)
					text = "$text…"
				listChoices.add(text)
			}

			if (listChoices.isNotEmpty())
				listChoices
			else ""
		} as List<String>
	}

}

// Represents different states for the latest OpenAI result
/*
sealed class LatestResultUiState {
	data class Success(val result: List<String>): LatestResultUiState()
	data class Error(val exception: Throwable): LatestResultUiState()
}*/
