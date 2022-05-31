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

        // Create GameActivity sending an Int parameter depending on difficulty
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
        // Intent GameActivity with the desired parameter
        val i = Intent(this, GameActivity::class.java)
        i.putExtra("gameType", gameType)
        startActivity(i)
    }

    private fun updateWins() {
        // Update wins TextView
        binding.tvHighScore.text = resources.getString(R.string.ma_wins, mPrefs.getWins())
    }

    override fun onResume() {
        // When app is resumed (mainly when launching app or returning from GameActivity)
        // call update Wins
        super.onResume()
        updateWins()
    }
}