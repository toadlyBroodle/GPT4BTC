package org.bitanon.chatgpt3

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.bitanon.chatgpt3.databinding.FragmentChatBinding

const val OPENAI_KEY_PART2 = "EjCe9iICSpXhT3Blbk"

//private const val TAG = "ChatFrag"
class ChatFrag : Fragment() {
	private var promptCount = 0

	private var _binding: FragmentChatBinding? = null

	// This property is only valid between onCreateView and onDestroyView.
	private val binding get() = _binding!!

	private lateinit var etPrompt: EditText
	private lateinit var tvPrompt: TextView
	private lateinit var tvAnswer: TextView

	private val viewModel: ChatViewModel by viewModels()


	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {

		_binding = FragmentChatBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

/*		binding.buttonFirst.setOnClickListener {
			findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
		}*/

		etPrompt = binding.edittextAskQuestion
		tvPrompt = binding.textviewQuestion
		tvAnswer = binding.textviewAnswer

		binding.buttonPrompt.setOnClickListener {
			// check for internet connection
			if (!viewModel.requestRepository.isOnline(requireContext())) {
				// toast user to connect
				MainActivity.showToast(requireContext(),
					getString(R.string.toast_no_internet))
				return@setOnClickListener
			}

			if (!etPrompt.text.isNullOrBlank()) {
				Firebase.logCustomEvent(BUTTON_PROMPT_SEND)

				val q = etPrompt.text.toString()
				etPrompt.text.clear()
				tvPrompt.text = q
				tvAnswer.text = getString(R.string.response_thinking)

				viewModel.sendPrompt(q)

				// show interstitial ad every three prompts
				if (promptCount % 3 == 0)
					AdMob.show(activity)
				else  { // load new interstitial after last one shown
					if (promptCount % 3 == 1)
						AdMob.init(requireContext())
				}
				promptCount++

			} else MainActivity.showToast(requireContext(), getString(R.string.toast_enter_prompt))
		}

		etPrompt.addTextChangedListener(object : TextWatcher {
			override fun afterTextChanged(s: Editable) {}
			override fun beforeTextChanged(s: CharSequence, start: Int,
										   count: Int, after: Int) {}
			override fun onTextChanged(s: CharSequence, start: Int,
									   before: Int, count: Int) {
				if (s.length >= resources.getInteger(R.integer.chat_edit_text_max_length)) {
					Firebase.logCustomEvent(NOTIFICATION_PROMPT_TRUNCATED)
					MainActivity.showToast(requireContext(), getString(R.string.toast_prompt_truncated))
				}
			}
		})

		lifecycleScope.launch {
			viewModel.uiState.collect { uiState ->
				if (uiState.isNotEmpty()) {
					var output = uiState[0]

					// add truncated notification when response longer than max allowed
					if (output.endsWith("…\n\n")) {
						Firebase.logCustomEvent(NOTIFICATION_RESPONSE_TRUNCATED)
						output += getString(R.string.append_response_truncated)
					}

					Firebase.logCustomEvent(OPENAI_RESPONSE_SHOW)
					tvAnswer.text = output
				}
			}
		}

	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}
