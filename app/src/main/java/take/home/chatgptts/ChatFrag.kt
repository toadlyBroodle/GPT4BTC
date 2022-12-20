package take.home.chatgptts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import take.home.chatgptts.databinding.FragmentChatBinding


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ChatFrag : Fragment() {

	private var _binding: FragmentChatBinding? = null

	// This property is only valid between onCreateView and onDestroyView.
	private val binding get() = _binding!!

	lateinit var etPrompt: EditText
	lateinit var tvPrompt: TextView
	lateinit var tvAnswer: TextView

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
			if (!etPrompt.text.isNullOrBlank()) {
				val q = etPrompt.text.toString()
				etPrompt.text.clear()
				tvPrompt.text = q
				tvAnswer.text = getString(R.string.response_thinking)


				viewModel.sendPrompt(q)
			} else showToast("First, enter a prompt.")
		}

		lifecycleScope.launch {
			viewModel.uiState.collect { uiState ->
				if (uiState.isNotEmpty()) {
					val output = uiState[0]
					tvAnswer.text = output

					if (output.endsWith("…"))
						showToast("Response truncated, please upgrade to enable longer responses.")
				}
			}
		}

	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	fun showToast(message: String) =
			Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
