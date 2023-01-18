package org.bitanon.chatgpt3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial
import org.bitanon.chatgpt3.MainActivity.Companion.prefDictationAuto
import org.bitanon.chatgpt3.MainActivity.Companion.prefDictationSpeed
import org.bitanon.chatgpt3.MainActivity.Companion.setPrefDictationAuto
import org.bitanon.chatgpt3.databinding.ActivitySettingsBinding

private const val TAG = "SettingsActivity"
class SettingsActivity : AppCompatActivity() {

	private lateinit var binding: ActivitySettingsBinding

	private lateinit var switchDictateAuto: SwitchMaterial
	private lateinit var sliderSpeechSpeed: Slider


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivitySettingsBinding.inflate(layoutInflater)
		setContentView(binding.root)

		supportActionBar?.setDisplayHomeAsUpEnabled(true)

	}

	override fun onStart() {
		super.onStart()

		switchDictateAuto = binding.settingsSwitchDictateAuto
		sliderSpeechSpeed = binding.settingsSliderSpeechSpeed

		// set widget states and listeners
		switchDictateAuto.isChecked = prefDictationAuto.value
		switchDictateAuto.setOnCheckedChangeListener { _, isChecked ->
			setPrefDictationAuto(this, isChecked)
		}
		sliderSpeechSpeed.value = prefDictationSpeed
		sliderSpeechSpeed.addOnChangeListener { _, value, _ ->
			prefDictationSpeed = value
		}

	}

	override fun onPause() {
		super.onPause()
		MainActivity.savePrefs(this)
	}
}