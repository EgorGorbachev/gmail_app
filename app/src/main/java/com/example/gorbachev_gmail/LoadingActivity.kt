package com.example.gorbachev_gmail

import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.github.ybq.android.spinkit.sprite.Sprite
import com.github.ybq.android.spinkit.style.Wave
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class LoadingActivity : AppCompatActivity() {
	
	private lateinit var progressBar: ProgressBar
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_loading)
		
		val intent = Intent(this, MainActivity::class.java)
		GlobalScope.launch {
			delay(2000)
			startActivity(intent)
			finish()
		}
		
		progressBar = findViewById(R.id.spin_kit)
		val wave: Sprite = Wave()
		progressBar.indeterminateDrawable = wave
	}
}