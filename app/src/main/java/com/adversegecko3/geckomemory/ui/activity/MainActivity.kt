package com.adversegecko3.geckomemory.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.adversegecko3.geckomemory.R
import com.adversegecko3.geckomemory.databinding.ActivityMainBinding
import com.adversegecko3.geckomemory.ui.GeckoMemoryApp.Companion.mPrefs

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvHighScore.text =
            resources.getString(R.string.ma_high_score, mPrefs.getHighScore())

        binding.apply {
            btnEasy.setOnClickListener {
                launchGameActivity(0)
            }
            btnMedium.setOnClickListener {
                launchGameActivity(1)
            }
            btnHard.setOnClickListener {
                launchGameActivity(2)
            }
        }
    }

    private fun launchGameActivity(gameType: Int) {
        val i = Intent(this, GameActivity::class.java)
        i.putExtra("gameType", gameType)
        startActivity(i)
    }
}