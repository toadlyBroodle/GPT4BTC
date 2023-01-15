package org.bitanon.chatgpt3

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
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
const val AD_ID_PART2 = "9043912704472803/"

//private const val TAG = "ChatFrag"
class ChatFrag : Fragment() {
	private var sessionPromptCount = 0

	private var _binding: FragmentChatBinding? = null

	// This property is only valid between onCreateView and onDestroyView.
	private val binding get() = _binding!!

	private lateinit var tvPromptLabelName: TextView
	private lateinit var tvPrompt: TextView
	private lateinit var tvAnswer: TextView
	private lateinit var etPrompt: EditText

	private val viewModel: ChatViewModel by viewModels()

	private val firestore = Firestore()


	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {

		_binding = FragmentChatBinding.inflate(inflater, container, false)
		return binding.root
	}


	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		tvPromptLabelName = binding.textviewLabelName
		tvPrompt = binding.textviewQuestion
		tvAnswer = binding.textviewAnswer
		etPrompt = binding.edittextAskQuestion

		// set prompt button on click listener logic
		binding.buttonPrompt.setOnClickListener {
			// check for internet connection
			if (!RequestRepository.isOnline(requireContext())) {
				// toast user to connect
				MainActivity.showToast(requireContext(),
					getString(R.string.toast_no_internet))
				return@setOnClickListener
			}

			if (!etPrompt.text.isNullOrBlank()) {
				FirebaseAnalytics.logCustomEvent(BUTTON_PROMPT_SEND)

				val q = etPrompt.text.toString()
				etPrompt.text.clear()
				tvPrompt.text = q
				tvAnswer.text = getString(R.string.response_thinking)

				viewModel.sendPrompt(requireContext(), q)

				// show interstitial ad every three prompts
				if (sessionPromptCount % 3 == 0)
					AdMob.show(activity)
				else  { // load new interstitial after last one shown
					if (sessionPromptCount % 3 == 1)
						AdMob.init(requireContext())
				}
				sessionPromptCount++

			} else MainActivity.showToast(requireContext(), getString(R.string.toast_enter_prompt))
		}

		// set prompt button on click listener logic
		binding.buttonAudioDictation.setOnClickListener {
			MainActivity.showToast(requireContext(),
				getString(R.string.response_dictation_requires_subscription))
		}

		// set prompt edit text text changed listener and logic
		etPrompt.addTextChangedListener(object : TextWatcher {
			override fun afterTextChanged(s: Editable) {}
			override fun beforeTextChanged(s: CharSequence, start: Int,
										   count: Int, after: Int) {}
			override fun onTextChanged(s: CharSequence, start: Int,
									   before: Int, count: Int) {
				if (s.length >= AccountActivity.getMaxPromptChars()) {
					FirebaseAnalytics.logCustomEvent(NOTIFICATION_PROMPT_TRUNCATED)
					MainActivity.showToast(requireContext(),
						getString(R.string.toast_prompt_truncated))
				}
			}
		})

		// collect changes to UI state
		lifecycleScope.launch {
			viewModel.uiState.collect { uiState ->
				if (uiState.isNotEmpty()) {
					var output = uiState[0]

					// add truncated notification when response longer than max allowed
					if (output.endsWith("…\n\n")) {
						FirebaseAnalytics.logCustomEvent(NOTIFICATION_RESPONSE_TRUNCATED)
						output += getString(R.string.append_response_truncated)
					}

					FirebaseAnalytics.logCustomEvent(OPENAI_RESPONSE_SHOW)
					tvAnswer.text = output

					// increment promptCount in firestore
					firestore.incrementUserPrompts()
				}
			}
		}

		// collect changes to user state
		lifecycleScope.launch {
			Firestore.userState.collect { user ->

				// set prompt label display name
				tvPromptLabelName.text = "${user?.displayName ?: getString(R.string.anon)}:"

				// set max length of prompt edittext
				etPrompt.filters = arrayOf(InputFilter.LengthFilter(AccountActivity.getMaxPromptChars()))
			}
		}

	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}
