package com.adversegecko3.geckomemory.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.adversegecko3.geckomemory.databinding.ActivityGameBinding

class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val b = intent.extras
        val gameType = b!!.getInt("gameType")

        binding.btnBack.setOnClickListener {
            CustomDialog(
                "Leave game?",
                "Are you sure you want to leave the game?",
                "Yes",
                "No",
                onSubmitClickListener = {
                    onBackPressed()
                }
            ).show(supportFragmentManager, "dialog")
        }

        binding.sbTime.setOnTouchListener { _, _ -> true }
    }
}