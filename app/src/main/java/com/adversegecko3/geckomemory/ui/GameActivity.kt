package com.adversegecko3.geckomemory.ui

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity.CENTER
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import com.adversegecko3.geckomemory.R
import com.adversegecko3.geckomemory.databinding.ActivityGameBinding
import com.google.android.material.imageview.ShapeableImageView
import java.lang.reflect.Field
import kotlin.properties.Delegates

class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding
    private var tableSize by Delegates.notNull<Int>()
    private var cards = mutableListOf<Card>()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val b = intent.extras
        val gameType = b!!.getInt("gameType")

        createTable(gameType)

        val images = chooseImages(getAllDrawables())
        setImages(images)

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.sbTime.setOnTouchListener { _, _ -> true }
    }

    private fun createTable(gameType: Int) {
        var col = 0
        var row = 0
        val screenWidth = resources.displayMetrics.widthPixels - (TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            8F,
            resources.displayMetrics
        ) * 2).toInt()

        when (gameType) {
            0 -> {
                col = 4
                row = 4
            }
            1 -> {
                col = 4
                row = 6
            }
            2 -> {
                col = 5
                row = 6
            }
        }
        binding.grlTable.apply {
            columnCount = col
            rowCount = row
        }
        tableSize = col * row

        for (i in 0 until tableSize) {
            cards.add(Card(i))

            val rv = RelativeLayout(this).apply {
                val layRV = LinearLayout.LayoutParams(
                    screenWidth / col,
                    screenWidth / col
                ).apply {
                    setPadding(8)
                }
                layoutParams = layRV
            }

            val cvFront = CardView(this).apply {
                val layCVF = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                layoutParams = layCVF
                radius = resources.getDimension(R.dimen.corner_radius)
                cardElevation = 0F
                alpha = 0F
                setCardBackgroundColor(Color.TRANSPARENT)
                tag = "cvFront$i"
            }

            val ivFront = ShapeableImageView(this).apply {
                val layIVF = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    gravity = CENTER
                }
                layoutParams = layIVF
                shapeAppearanceModel = this.shapeAppearanceModel.toBuilder()
                    .setAllCornerSizes(resources.getDimension(R.dimen.corner_radius))
                    .build()
                strokeWidth = resources.getDimension(R.dimen.stroke_width)
                strokeColor = ColorStateList.valueOf(resources.getColor(R.color.border, null))
                setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.card_front_image_f1_sainz,
                        null
                    )
                )
                tag = "ivFront$i"
            }
            cvFront.addView(ivFront)

            val cvBack = CardView(this).apply {
                val layCVB = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                layoutParams = layCVB
                radius = resources.getDimension(R.dimen.corner_radius)
                cardElevation = 0F
                setCardBackgroundColor(Color.TRANSPARENT)
                tag = "cvBack$i"
            }

            val ivBack = ShapeableImageView(this).apply {
                val layIVB = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    gravity = CENTER
                }
                layoutParams = layIVB
                shapeAppearanceModel = this.shapeAppearanceModel.toBuilder()
                    .setAllCornerSizes(resources.getDimension(R.dimen.corner_radius))
                    .build()
                strokeWidth = resources.getDimension(R.dimen.stroke_width)
                strokeColor = ColorStateList.valueOf(resources.getColor(R.color.border, null))
                setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.card_back,
                        null
                    )
                )
                tag = "ivBack$i"
            }
            cvBack.addView(ivBack)

            rv.addView(cvFront)
            rv.addView(cvBack)
            rv.setOnClickListener {
                val front = it.findViewWithTag<CardView>("cvFront$i")
                val back = it.findViewWithTag<CardView>("cvBack$i")

                val frontAnim = AnimatorInflater.loadAnimator(
                    applicationContext,
                    R.animator.animator_front
                ) as AnimatorSet
                val backAnim = AnimatorInflater.loadAnimator(
                    applicationContext,
                    R.animator.animator_back
                ) as AnimatorSet

                cards[i].isShown = if (cards[i].isShown) {
                    frontAnim.setTarget(front)
                    backAnim.setTarget(back)
                    frontAnim.start()
                    backAnim.start()
                    false
                } else {
                    frontAnim.setTarget(back)
                    backAnim.setTarget(front)
                    frontAnim.start()
                    backAnim.start()
                    true
                }
            }
            binding.grlTable.addView(rv)
        }
    }

    private fun chooseImages(allImages: MutableList<Int>): MutableList<Int> {
        val totalPairs = tableSize / 2
        allImages.shuffle()
        allImages.subList(totalPairs - 1, allImages.lastIndex).clear()
        allImages.addAll(allImages)
        return allImages
    }

    private fun getAllDrawables(): MutableList<Int> {
        val drawablesFields: Array<Field> = R.drawable::class.java.fields
        val drawables = mutableListOf<Int>()

        for (field in drawablesFields) {
            try {
                if (field.name.contains("card_front_image")) {
                    drawables.add(field.getInt(null))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return drawables
    }

    private fun setImages(images: MutableList<Int>) {
        for (i in 0 until binding.grlTable.childCount) {
            val rv = binding.grlTable.getChildAt(i) as RelativeLayout
            val iv = rv.findViewWithTag<ShapeableImageView>("ivFront$i")
            cards[i].image = images[i]
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