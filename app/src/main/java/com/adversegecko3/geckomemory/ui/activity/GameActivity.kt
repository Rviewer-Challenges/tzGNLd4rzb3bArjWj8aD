package com.adversegecko3.geckomemory.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.adversegecko3.geckomemory.R
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

        // Get difficulty sent by MainActivity
        val gameType = intent.extras!!.getInt("gameType")

        // Load LoadingDialog
        val loadingDialog = LoadingDialog(this)

        // Observe isLoading from VM and depending the boolean
        // show or dismiss LoadingDialog
        gameViewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                loadingDialog.show()
            } else {
                loadingDialog.dismiss()
            }
        }

        // Observe gameTableSize from VM and set GridLayout col and row count
        gameViewModel.gameTableSize.observe(this) { alSize ->
            binding.grlTable.apply {
                columnCount = alSize[0]
                rowCount = alSize[1]
            }
        }

        // Observe gameTableViews from VM and add views to GridLayout
        // When done, call VM managePairs
        gameViewModel.gameTableViews.observe(this) { rl ->
            rl.forEach {
                binding.grlTable.addView(it)
            }
            gameViewModel.managePairs(binding.grlTable)
        }

        // Observe cardImages from VM and call setImages
        gameViewModel.cardImages.observe(this) { alImages ->
            setImages(alImages)
        }

        // Observe gameMoves from VM and update moves TextView
        gameViewModel.gameMoves.observe(this) { moves ->
            binding.tvMoves.text = resources.getString(R.string.ga_moves, moves)
        }

        // Observe gamePairs from VM and update pairs TextView
        gameViewModel.gamePairs.observe(this) { pairs ->
            binding.tvPairs.text = resources.getString(R.string.ga_pairs, pairs)
        }

        // Observe gameTimer from VM and update timer ProgressBar
        gameViewModel.gameTimer.observe(this) { progress ->
            binding.pbTime.progress = progress.toInt()
        }

        // Observe endGame from VM and show CustomDialog with its corresponding title and content
        // Neutral Button returns to MainActivity by calling super.onBackPressed()
        gameViewModel.endGame.observe(this) { endType ->
            lateinit var title: String
            lateinit var content: String
            when (endType) {
                0 -> {
                    title = "GAME OVER"
                    content = "Sorry, time is gone..."
                }
                1 -> {
                    title = "Congratulations!"
                    content = "You won! You made it!"
                }
            }
            CustomDialog(
                title = title,
                content = content,
                positiveButton = "Return to menu",
                onSubmitClickListener = {
                    super.onBackPressed()
                },
                isEndDialog = true
            ).show(supportFragmentManager, "endDialog")
        }

        // Control back click by calling onBackPressed()
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        // Finally load onCreate function from VM
        gameViewModel.onCreate(gameType)
    }

    private fun setImages(images: MutableList<Int>) {
        // Loop through every item in images MutableList (each one is a drawable ID)
        // Get every front ShapeableImageView, save the ID to the cards list on the VM and load the current drawable ID
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
        // When back is pressed, first check if game has already started. If so, pause the timer
        if (gameViewModel.gameHasStarted) {
            gameViewModel.pauseTimer()
        }
        // Then, show Custom Dialog with Leave game texts
        // Positive button calls super.onBackPressed() to return to MainActivity
        // Negative Button returns to game and resumes the timer if game has already started
        CustomDialog(
            title = "Leave game?",
            content = "Are you sure you want to leave the game?",
            positiveButton = "Yes",
            onSubmitClickListener = {
                super.onBackPressed()
            },
            negativeButton = "No",
            onDismissClickListener = {
                if (gameViewModel.gameHasStarted) {
                    gameViewModel.resumeTimer()
                }
            }
        ).show(supportFragmentManager, "leaveDialog")
    }

    override fun onPause() {
        // If activity is paused, call onBackPressed()
        super.onPause()
        onBackPressed()
    }
}