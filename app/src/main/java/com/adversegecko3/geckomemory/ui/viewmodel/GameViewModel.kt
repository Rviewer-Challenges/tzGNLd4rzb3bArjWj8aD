package com.adversegecko3.geckomemory.ui.viewmodel

import android.animation.Animator
import android.animation.AnimatorSet
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.CountDownTimer
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
import com.adversegecko3.geckomemory.ui.GeckoMemoryApp.Companion.mPrefs
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
    val gameMoves = MutableLiveData<Int>()
    val gamePairs = MutableLiveData<Int>()
    val gameTimer = MutableLiveData<Long>()
    val endGame = MutableLiveData<Int>()

    private var tableSize by Delegates.notNull<Int>()
    var cards = mutableListOf<Card>()
    private var col by Delegates.notNull<Int>()
    private var isLocked = false
    private var moves = 0
    private var pairs = 0
    private lateinit var timer: CountDownTimer
    var currentTimerMillis = 0L
    var gameHasStarted = false

    private val flippedCards = mutableListOf<Int>()

    fun onCreate(gameType: Int) {
        viewModelScope.launch {
            // First send true isLoading to GameActivity observer
            isLoading.postValue(true)
            withContext(Dispatchers.IO) {
                var appearanceOk = false
                try {
                    while (!appearanceOk) {
                        // Then, on the background thread, send GridLayout Table Size
                        gameTableSize.postValue(setTableSize(gameType))
                        // Send pairs to GameActivity
                        gamePairs.postValue(pairs)
                        // Send the needed tableViews
                        gameTableViews.postValue(createTableViews())
                        // Load drawable images, choose them and send them
                        cardImages.postValue(chooseImages(getAllDrawables()))
                        // Send moves to GameActivity
                        gameMoves.postValue(moves)
                        appearanceOk = true
                    }
                } catch (e: IndexOutOfBoundsException) {
                    Log.e("IOOBE", "meh")
                }
            }
            // Outside of the background thread, setup the 1 minute timer
            setupTimer()
            // Finally send false isLoading to GameActivity
            isLoading.postValue(false)
        }
    }

    private fun setTableSize(gameType: Int): ArrayList<Int> {
        // Depending game difficulty, set columns and rows
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

        // Save columns, tableSize and left pairs
        this.col = col
        tableSize = col * row
        pairs = tableSize / 2

        return arrayListOf(col, row)
    }

    private fun setupTimer() {
        // Create a CountDownTimer object of 60000 millis (60s) with a 100ms tick rate
        // On each tick save current millis and sent them to GameActivity
        // On finish send tick 0, and show Game Over dialog
        timer = object : CountDownTimer(60000, 100) {
            override fun onTick(tick: Long) {
                currentTimerMillis = tick
                gameTimer.postValue(tick)
            }

            override fun onFinish() {
                gameTimer.postValue(0)
                endGame.postValue(0)
            }
        }
    }

    private fun startTimer() {
        // Start the created CountDownTimer object
        timer.start()
    }

    fun pauseTimer() {
        // Pause the created CountDownTimer object. Actually, cancel it
        timer.cancel()
    }

    fun resumeTimer() {
        // This should be called after the timer has been started and paused
        // On each Tick it saved the current millis, so replace with a new CountDownTimer from the saved current millis
        // OnTick and OnFinish are the same as above
        // Also start the timer
        timer = object : CountDownTimer(currentTimerMillis, 100) {
            override fun onTick(tick: Long) {
                currentTimerMillis = tick
                gameTimer.postValue(tick)
            }

            override fun onFinish() {
                gameTimer.postValue(0)
                endGame.postValue(0)
            }
        }.start()
    }

    private fun createTableViews(): ArrayList<RelativeLayout> {
        // First get phone screen width pixels minis padding from each side
        val screenWidth = mResources.displayMetrics.widthPixels - (TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            8F,
            mResources.displayMetrics
        ) * 2).toInt()

        // Then loop through tableSize
        val alLayouts = arrayListOf<RelativeLayout>()
        for (i in 0 until tableSize) {
            // Add a card object with the index of the loop
            cards.add(Card(i))

            // First create a RelativeLayout with the width and height
            // of the above calculated screenWidth, divided by the desired columns
            val rv = RelativeLayout(mAppContext).apply {
                val layRV = LinearLayout.LayoutParams(
                    screenWidth / col,
                    screenWidth / col
                ).apply {
                    setPadding(8)
                }
                layoutParams = layRV
            }

            // Next create CardView with a ShapeableImageView to make the card front
            // and another CardView with also a ShapeableImageView to make the card back
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
                shapeAppearanceModel = shapeAppearanceModel.toBuilder()
                    .setAllCornerSizes(resources.getDimension(R.dimen.corner_radius))
                    .build()
                strokeWidth = resources.getDimension(R.dimen.stroke_width)
                strokeColor = ColorStateList.valueOf(resources.getColor(R.color.border, null))
                tag = "ivFront$i"
            }

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
                shapeAppearanceModel = shapeAppearanceModel.toBuilder()
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

            // Add its ShapeableImageView to the CardView
            cvFront.addView(ivFront)
            cvBack.addView(ivBack)

            // Then add both CardView to the RelativeLayout, and add them to an ArrayList
            rv.addView(cvFront)
            rv.addView(cvBack)
            alLayouts.add(rv)
        }
        return alLayouts
    }

    private fun getAllDrawables(): ArrayList<Int> {
        // First load all drawable fields and loop them
        // Check if the name starts with "card_front_image" and save its ID
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

    private fun chooseImages(allImages: ArrayList<Int>): ArrayList<Int> {
        // First calculate the needed pairs and shuffle the given list
        // Then get the needed pairs the list, add the list to the list to have real pairs and shuffle again
        val totalPairs = tableSize / 2
        allImages.shuffle()
        allImages.subList(totalPairs - 1, allImages.lastIndex).clear()
        allImages.addAll(allImages)
        allImages.shuffle()
        return allImages
    }

    fun managePairs(gl: GridLayout) {
        // Loop through every child of the GridLayout (each RelativeLayout)
        for (i in 0 until gl.childCount) {
            // Get the RelativeLayout and add OnClickListener
            val rv = gl.getChildAt(i)
            rv.setOnClickListener { rl ->
                // First check if game isLocked (animation is being displayed)
                if (!isLocked) {
                    // If game has not been started start the timer and change the boolean value
                    if (!gameHasStarted) {
                        startTimer()
                        gameHasStarted = true
                    }
                    if (!cards[i].isShown) {
                        // If the clicked card is not shown get its back and front CardView
                        val currentCardFront = rl.findViewWithTag<CardView>("cvFront$i")
                        val currentCardBack = rl.findViewWithTag<CardView>("cvBack$i")

                        // Apply animations and flip the card (from back to front)
                        val currentFrontAnim = mAppContext.loadFrontAnimator.apply {
                            setTarget(currentCardBack)
                        }
                        val currentBackAnim = mAppContext.loadBackAnimator.apply {
                            setTarget(currentCardFront)
                        }
                        val currentAnimatorSet = AnimatorSet()
                        currentAnimatorSet.play(currentFrontAnim).with(currentBackAnim)
                        currentAnimatorSet.start()

                        // Change card state to true and add the current card to the flippedCards list
                        cards[i].isShown = true
                        flippedCards.add(i)
                        if (flippedCards.size == 2) {
                            // If flippedCards size is 2, check if the images are the same or not
                            if (cards[flippedCards[0]].image == cards[flippedCards[1]].image) {
                                // If both images are the same, send pairs - 1 to GameActivity and clear flippedCards
                                gamePairs.postValue(--pairs)
                                flippedCards.clear()
                                if (pairs == 0) {
                                    // If there are no more pairs means th game has been concluded (won)
                                    // So pause the timer (cancel it) add 1 win to the Preferences and send Win dialog to GameActivity
                                    pauseTimer()
                                    mPrefs.setWins(mPrefs.getWins() + 1)
                                    endGame.postValue(1)
                                }
                            } else {
                                // If both images are not the same create a Handler of 500ms (duration of the flip animation)
                                Handler(Looper.getMainLooper()).postDelayed({
                                    // Get both flipped CardViews with the front and back views
                                    val rvLast = gl.getChildAt(flippedCards[0])
                                    val alCards = arrayListOf<CardView>()
                                    alCards.add(rvLast.findViewWithTag("cvFront${flippedCards[0]}"))
                                    alCards.add(rvLast.findViewWithTag("cvBack${flippedCards[0]}"))
                                    alCards.add(currentCardFront)
                                    alCards.add(currentCardBack)

                                    // Loop every CardView part, and depending the position add the needed animation to an ArrayList
                                    val alAnimators = arrayListOf<Animator>()
                                    alCards.forEachIndexed { index, cardView ->
                                        if (index % 2 == 0) {
                                            val frontAnimA = mAppContext.loadFrontAnimator.apply {
                                                setTarget(cardView)
                                            }
                                            alAnimators.add(frontAnimA)
                                        } else {
                                            val backAnimA = mAppContext.loadBackAnimator.apply {
                                                setTarget(cardView)
                                            }
                                            alAnimators.add(backAnimA)
                                        }
                                    }

                                    // Create an animatorSet and play the animations
                                    val aSet = AnimatorSet()
                                    aSet.play(alAnimators[0]).with(alAnimators[1])
                                    aSet.play(alAnimators[1]).with(alAnimators[2])
                                    aSet.play(alAnimators[2]).with(alAnimators[3])
                                    aSet.start()

                                    // And set both cards state to false, clear flippedCards and change isLocked to false
                                    cards[flippedCards[0]].isShown = false
                                    cards[flippedCards[1]].isShown = false
                                    flippedCards.clear()
                                    isLocked = false
                                }, 500)
                                // Set isLocked to true
                                // As the Handler code will be executed after 500ms, the next line will be read first
                                // This prevents from the user to click another cards and not being able to animate the next cards
                                isLocked = true
                            }
                            // Finally send moves + 1 to GameActivity
                            gameMoves.postValue(++moves)
                        }
                    }
                }
            }
        }
    }
}