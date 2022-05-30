package com.adversegecko3.geckomemory.ui.viewmodel

import android.animation.Animator
import android.animation.AnimatorSet
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adversegecko3.geckomemory.R
import com.adversegecko3.geckomemory.data.Card
import com.adversegecko3.geckomemory.ui.GeckoMemoryApp.Companion.mAppContext
import com.adversegecko3.geckomemory.ui.GeckoMemoryApp.Companion.mResources
import com.adversegecko3.geckomemory.utils.loadBackAnimator
import com.adversegecko3.geckomemory.utils.loadFrontAnimator
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.reflect.Field
import kotlin.properties.Delegates

class GameViewModel : ViewModel() {
    val isLoading = MutableLiveData<Boolean>()
    val gameTableSize = MutableLiveData<ArrayList<Int>>()
    val gameTableViews = MutableLiveData<ArrayList<RelativeLayout>>()
    val cardImages = MutableLiveData<ArrayList<Int>>()

    private var tableSize by Delegates.notNull<Int>()
    var cards = mutableListOf<Card>()
    var col by Delegates.notNull<Int>()
    var isLocked = false

    private val flippedCards = mutableListOf<Int>()

    fun onCreate(gameType: Int) {
        viewModelScope.launch {
            isLoading.postValue(true)
            withContext(Dispatchers.IO) {
                gameTableSize.postValue(setTableSize(gameType))
                gameTableViews.postValue(createTableViews())
                cardImages.postValue(chooseImages(getAllDrawables()))
            }

            isLoading.postValue(false)
        }
    }

    private fun setTableSize(gameType: Int): ArrayList<Int> {
        var col = 0
        var row = 0

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

        this.col = col
        tableSize = col * row

        return arrayListOf(col, row)
    }

    private fun createTableViews(): ArrayList<RelativeLayout> {
        val screenWidth = mResources.displayMetrics.widthPixels - (TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            8F,
            mResources.displayMetrics
        ) * 2).toInt()

        val alLayouts = arrayListOf<RelativeLayout>()
        for (i in 0 until tableSize) {
            cards.add(Card(i))

            val rv = RelativeLayout(mAppContext).apply {
                val layRV = LinearLayout.LayoutParams(
                    screenWidth / col,
                    screenWidth / col
                ).apply {
                    setPadding(8)
                }
                layoutParams = layRV
            }

            val cvFront = CardView(mAppContext).apply {
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

            val ivFront = ShapeableImageView(mAppContext).apply {
                val layIVF = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    gravity = Gravity.CENTER
                }
                layoutParams = layIVF
                shapeAppearanceModel = this.shapeAppearanceModel.toBuilder()
                    .setAllCornerSizes(resources.getDimension(R.dimen.corner_radius))
                    .build()
                strokeWidth = resources.getDimension(R.dimen.stroke_width)
                strokeColor = ColorStateList.valueOf(resources.getColor(R.color.border, null))
                tag = "ivFront$i"
            }
            cvFront.addView(ivFront)

            val cvBack = CardView(mAppContext).apply {
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

            val ivBack = ShapeableImageView(mAppContext).apply {
                val layIVB = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    gravity = Gravity.CENTER
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
            alLayouts.add(rv)
        }
        return alLayouts
    }

    private fun chooseImages(allImages: ArrayList<Int>): ArrayList<Int> {
        val totalPairs = tableSize / 2
        allImages.shuffle()
        allImages.subList(totalPairs - 1, allImages.lastIndex).clear()
        allImages.addAll(allImages)
        allImages.shuffle()
        return allImages
    }

    private fun getAllDrawables(): ArrayList<Int> {
        val drawablesFields: Array<Field> = R.drawable::class.java.fields
        val drawables = arrayListOf<Int>()

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

    fun managePairs(gl: GridLayout) {
        for (i in 0 until gl.childCount) {
            val rv = gl.getChildAt(i)
            rv.setOnClickListener { rl ->
                if (!isLocked) {
                    if (!cards[i].isShown) {
                        Log.d("GALog", "Cards is not shown")
                        val currentCardFront = rl.findViewWithTag<CardView>("cvFront$i")
                        val currentCardBack = rl.findViewWithTag<CardView>("cvBack$i")

                        val currentFrontAnim = mAppContext.loadFrontAnimator.apply {
                            setTarget(currentCardBack)
                        }
                        val currentBackAnim = mAppContext.loadBackAnimator.apply {
                            setTarget(currentCardFront)
                        }
                        val currentAnimatorSet = AnimatorSet()
                        currentAnimatorSet.play(currentFrontAnim).with(currentBackAnim)
                        currentAnimatorSet.start()

                        cards[i].isShown = true
                        flippedCards.add(i)
                        Log.d("GALog", "flippedCards: $flippedCards")
                        if (flippedCards.size == 2) {
                            if (cards[flippedCards[0]].image == cards[flippedCards[1]].image) {
                                Log.d("GALog", "Cards match")
                                flippedCards.clear()
                            } else {
                                Log.d("GALog", "Cards do not match")
                                Handler(Looper.getMainLooper()).postDelayed({
                                    val rvLast = gl.getChildAt(flippedCards[0])

                                    val alCards = arrayListOf<CardView>()
                                    alCards.add(rvLast.findViewWithTag("cvFront${flippedCards[0]}"))
                                    alCards.add(rvLast.findViewWithTag("cvBack${flippedCards[0]}"))
                                    alCards.add(currentCardFront)
                                    alCards.add(currentCardBack)

                                    val alAnimators = arrayListOf<Animator>()
                                    alCards.forEachIndexed { index, cardView ->
                                        if (index % 2 == 0) {
                                            println("in0")
                                            val frontAnimA = mAppContext.loadFrontAnimator.apply {
                                                setTarget(cardView)
                                            }
                                            alAnimators.add(frontAnimA)
                                        } else {
                                            println("in1")
                                            val backAnimA = mAppContext.loadBackAnimator.apply {
                                                setTarget(cardView)
                                            }
                                            alAnimators.add(backAnimA)
                                        }
                                    }

                                    val aSet = AnimatorSet()
                                    aSet.play(alAnimators[0]).with(alAnimators[1])
                                    aSet.play(alAnimators[1]).with(alAnimators[2])
                                    aSet.play(alAnimators[2]).with(alAnimators[3])
                                    aSet.start()

                                    cards[flippedCards[0]].isShown = false
                                    cards[flippedCards[1]].isShown = false
                                    flippedCards.clear()
                                    isLocked = false
                                    Log.d("GALog", "isLocked f")
                                    Log.d("GALog", "flippedCards: $flippedCards")
                                }, 500)
                                isLocked = true
                                Log.d("GALog", "isLocked t")
                            }
                        }
                    }
                }
            }
        }
    }
}