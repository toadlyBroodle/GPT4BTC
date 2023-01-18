package org.bitanon.chatgpt3

import android.annotation.SuppressLint
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.bitanon.chatgpt3.MainActivity.Companion.prefDictationAuto
import org.bitanon.chatgpt3.MainActivity.Companion.prefDictationSpeed
import org.bitanon.chatgpt3.databinding.FragmentChatBinding
import java.util.*

const val OPENAI_KEY_PART2 = "EjCe9iICSpXhT3Blbk"
const val AD_ID_PART2 = "9043912704472803/"
const val TTS_PITCH = 0.85f


//private const val TAG = "ChatFrag"
class ChatFrag : Fragment(), TextToSpeech.OnInitListener {
	private var _binding: FragmentChatBinding? = null
	// This property is only valid between onCreateView and onDestroyView.
	private val binding get() = _binding!!

	private val viewModel: ChatViewModel by viewModels()

	private lateinit var tvPromptLabelName: TextView
	private lateinit var tvPrompt: TextView
	private lateinit var tvResponse: TextView
	private lateinit var etPrompt: EditText

	private var tts: TextToSpeech? = null
	private var ttsAvailable = false

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {

		_binding = FragmentChatBinding.inflate(inflater, container, false)
		return binding.root
	}


	@SuppressLint("SetTextI18n", "DiscouragedApi")
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		tvPromptLabelName = binding.textviewLabelName
		tvPrompt = binding.textviewQuestion
		tvResponse = binding.textviewResponse
		etPrompt = binding.edittextAskQuestion

		// init tts
		tts = TextToSpeech(requireContext(), this)

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
				tvResponse.text = getString(R.string.response_thinking)

				viewModel.sendPrompt(requireContext(), q)

				// try showing ad
				AdMob.show(activity)

			} else MainActivity.showToast(requireContext(), getString(R.string.toast_enter_prompt))
		}

		// collect changes to prefDictateAuto and update button state
		lifecycleScope.launch {
			prefDictationAuto.collect {
				// update widget states from preferences
				binding.buttonLeftAudioDictation.text =
					if (it) getString(R.string.audio_icon_left_auto)
					else getString(R.string.audio_icon_left_manual)
			}
		}

		// set dictation button listener logic
		binding.buttonLeftAudioDictation.setOnClickListener {
			// if already dictating, then stop
			if (tts?.isSpeaking == true) {
				tts?.stop()
				return@setOnClickListener
			}

			// start response dictation
			if (ttsAvailable) {
				FirebaseAnalytics.logCustomEvent(BUTTON_PROMPT_DICTATE)
				dictate(tvResponse.text.toString())
			} else // or toast that language is not supported
				MainActivity.showToast(requireContext(),
					getString(R.string.toast_tts_not_supported))
		}

		// set random prompt button on click listener logic
		binding.buttonLeftPromptRandom.setOnClickListener {
			FirebaseAnalytics.logCustomEvent(BUTTON_PROMPT_RANDOM)

			if (Firestore.isUserLoggedIn()) {
				val rand = (1..19).random()
				val strRes = "random_prompt_$rand"
				etPrompt.setText(resources.getIdentifier(strRes, "string",
					requireContext().packageName))
			} else MainActivity.showToast(requireContext(),
				getString(R.string.toast_login_generate_random_prompts))
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
					tvResponse.text = output

					// increment promptCount in firestore
					MainActivity.firestore.incrementUserPrompts()

					// autoDictate if enabled
					if (prefDictationAuto.value)
						dictate(output)
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

		// shutdown TextToSpeech
		if (tts != null) {
			tts?.stop()
			tts?.shutdown()
		}

		// unbind
		_binding = null
	}

	// TTS initialized
	override fun onInit(status: Int) {
		if (status == TextToSpeech.SUCCESS) {

			// try setting device default language and check if supported
			val result = tts!!.setLanguage(Locale.getDefault())
			ttsAvailable = if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.e("TTS","Language not supported in TTS")
				false
			} else
				true

			// set dictation attributes
			tts?.setPitch(TTS_PITCH)
			tts?.setSpeechRate(prefDictationSpeed)
		}
	}

	private fun dictate(text: String) {
		if (text.isEmpty()) {
			tts?.speak(getString(R.string.toast_enter_prompt), TextToSpeech.QUEUE_FLUSH,
				null,"")
		} else
			tts?.speak(text, TextToSpeech.QUEUE_FLUSH,null,"")
	}
}
