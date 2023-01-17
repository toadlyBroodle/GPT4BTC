package org.bitanon.chatgpt3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import org.bitanon.chatgpt3.MainActivity.Companion.prefDictateAuto
import org.bitanon.chatgpt3.MainActivity.Companion.setPrefDictateAuto
import org.bitanon.chatgpt3.databinding.ActivitySettingsBinding

private const val TAG = "SettingsActivity"
class SettingsActivity : AppCompatActivity() {

	private lateinit var binding: ActivitySettingsBinding

	private lateinit var switchDictateAuto: SwitchMaterial


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivitySettingsBinding.inflate(layoutInflater)
		setContentView(binding.root)

		supportActionBar?.setDisplayHomeAsUpEnabled(true)

	}

	override fun onStart() {
		super.onStart()

		switchDictateAuto = binding.settingsSwitchDictateAuto

		// set widget states and listeners
		switchDictateAuto.isChecked = prefDictateAuto.value
		switchDictateAuto.setOnCheckedChangeListener { _, isChecked ->
			setPrefDictateAuto(this, isChecked)
		}

	}
}