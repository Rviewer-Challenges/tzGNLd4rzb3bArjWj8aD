package com.adversegecko3.geckomemory.utils

import android.animation.Animator
import android.animation.AnimatorInflater
import android.content.Context
import com.adversegecko3.geckomemory.R

// Extensions to load the animator_front and animator_back animators
val Context.loadFrontAnimator: Animator
    get() {
        return AnimatorInflater.loadAnimator(
            this,
            R.animator.animator_front
        )
    }

val Context.loadBackAnimator: Animator
    get() {
        return AnimatorInflater.loadAnimator(
            this,
            R.animator.animator_back
        )
    }

