package org.bitanon.gpt4btc

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.slider.Slider
import org.bitanon.gpt4btc.MainActivity.Companion.prefDictationAuto
import org.bitanon.gpt4btc.MainActivity.Companion.prefDictationSpeed
import org.bitanon.gpt4btc.MainActivity.Companion.setPrefDictationAuto
import org.bitanon.gpt4btc.databinding.ActivitySettingsBinding

private const val TAG = "SettingsActivity"
class SettingsActivity : AppCompatActivity() {

	private lateinit var binding: ActivitySettingsBinding

	private lateinit var switchDictateAuto: SwitchCompat
	private lateinit var sliderSpeechSpeed: Slider


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivitySettingsBinding.inflate(layoutInflater)
		setContentView(binding.root)

		supportActionBar?.setDisplayHomeAsUpEnabled(true)

	}

	override fun onStart() {
		super.onStart()

		AdMob.loadNewInterstitial(baseContext)

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

		// try showing ad
		AdMob.showInterstitial(this)
	}
}