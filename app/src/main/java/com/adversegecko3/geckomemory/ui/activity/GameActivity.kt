package com.adversegecko3.geckomemory.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.adversegecko3.geckomemory.databinding.ActivityGameBinding
import com.adversegecko3.geckomemory.ui.dialog.CustomDialog
import com.adversegecko3.geckomemory.ui.dialog.LoadingDialog
import com.adversegecko3.geckomemory.ui.viewmodel.GameViewModel
import com.google.android.material.imageview.ShapeableImageView

class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding
    private val gameViewModel: GameViewModel by viewModels()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gameType = intent.extras!!.getInt("gameType")

        val dialog = LoadingDialog(this)
        gameViewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                dialog.show()
            } else {
                dialog.dismiss()
            }
        }

        gameViewModel.gameTableSize.observe(this) { alSize ->
            binding.grlTable.apply {
                columnCount = alSize[0]
                rowCount = alSize[1]
            }
        }

        gameViewModel.gameTableViews.observe(this) { rl ->
            rl.forEach {
                binding.grlTable.addView(it)
            }
            gameViewModel.managePairs(binding.grlTable)
        }

        gameViewModel.cardImages.observe(this) { alImages ->
            setImages(alImages)
        }

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.sbTime.setOnTouchListener { _, _ -> true }

        gameViewModel.onCreate(gameType)
    }

    private fun setImages(images: MutableList<Int>) {
        for (i in 0 until binding.grlTable.childCount) {
            val rv = binding.grlTable.getChildAt(i) as RelativeLayout
            val iv = rv.findViewWithTag<ShapeableImageView>("ivFront$i")
            gameViewModel.cards[i].image = images[i]
            iv.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    images[i],
                    null
                )
            )
        }
    }

    override fun onBackPressed() {
        CustomDialog(
            "Leave game?",
            "Are you sure you want to leave the game?",
            "Yes",
            "No",
            onSubmitClickListener = {
                super.onBackPressed()
            }
        ).show(supportFragmentManager, "dialog")
    }
}