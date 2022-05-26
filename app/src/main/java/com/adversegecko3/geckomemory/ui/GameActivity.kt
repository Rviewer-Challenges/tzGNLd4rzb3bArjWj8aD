package com.adversegecko3.geckomemory.ui

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity.CENTER
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import com.adversegecko3.geckomemory.R
import com.adversegecko3.geckomemory.databinding.ActivityGameBinding
import java.lang.reflect.Field
import kotlin.properties.Delegates

class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding
    private var tableSize by Delegates.notNull<Int>()

    private lateinit var front_anim: AnimatorSet
    private lateinit var back_anim: AnimatorSet
    var isFront = false

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

    private fun createTable(gameType: Int) {
        var col = 0
        var row = 0
        val screenWidth = resources.displayMetrics.widthPixels

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

        for (i in 1..tableSize) {
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
                setCardBackgroundColor(Color.TRANSPARENT)
                id = R.id.cvCardFront
            }

            val ivFront = ImageView(this).apply {
                val layIVF = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                layIVF.gravity = CENTER
                layoutParams = layIVF
                background = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.card_front_border,
                    null
                )
                setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.card_front_image_f1_sainz,
                        null
                    )
                )
                id = R.id.ivCardFront
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
                id = R.id.cvCardBack
            }

            val ivBack = ImageView(this).apply {
                val layIVB = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                layIVB.gravity = CENTER
                layoutParams = layIVB
                background = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.card_front_border,
                    null
                )
                setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.card_back,
                        null
                    )
                )
                id = R.id.ivCardBack
            }
            cvBack.addView(ivBack)

            front_anim = AnimatorInflater.loadAnimator(
                applicationContext,
                R.animator.animator_front
            ) as AnimatorSet
            back_anim = AnimatorInflater.loadAnimator(
                applicationContext,
                R.animator.animator_back
            ) as AnimatorSet
            val scale = resources.displayMetrics.density

            rv.addView(cvFront)
            rv.addView(cvBack)
            rv.setOnClickListener {
                val front = it.findViewById<CardView>(R.id.cvCardFront)
                val back = it.findViewById<CardView>(R.id.cvCardBack)
                front.cameraDistance = 8000 * scale
                back.cameraDistance = 8000 * scale
                isFront = if (isFront) {
                    front_anim.setTarget(front)
                    back_anim.setTarget(back)
                    front_anim.start()
                    back_anim.start()
                    false
                } else {
                    front_anim.setTarget(back)
                    back_anim.setTarget(front)
                    front_anim.start()
                    back_anim.start()
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
        /*for (i in 0 until binding.grlTable.childCount) {
            val rv = binding.grlTable.getChildAt(i) as RelativeLayout
            println("H - ${rv.javaClass.simpleName} - ${rv.childCount}")
            println("RV - ${rv.getChildAt(0).id} - ${rv.getChildAt(1).id}")
            println("RV - ${rv.getChildAt(0).javaClass.simpleName} - ${rv.getChildAt(1).javaClass.simpleName}")
            val cv = rv.getChildAt(0) as CardView
            println("H - ${cv.javaClass.simpleName}")
            val iv = cv.findViewById<ImageView>(R.id.ivCardFront)
            println("H - ${iv.javaClass.simpleName}")
            iv.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    images[i % tableSize / 2],
                    null
                )
            )
        }*/
    }
}