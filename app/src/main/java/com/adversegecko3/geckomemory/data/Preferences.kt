package com.adversegecko3.geckomemory.data

import android.content.Context
import com.adversegecko3.geckomemory.R
import com.adversegecko3.geckomemory.ui.GeckoMemoryApp.Companion.mResources

class Preferences(context: Context) {
    private val userPrefs = context.getSharedPreferences(
        context.getString(R.string.pref_key), Context.MODE_PRIVATE
    )!!

    fun getHighScore(): Int {
        return userPrefs.getInt(mResources.getString(R.string.pref_high_score), 0)
    }

    fun setHighScore(highScore: Int) {
        userPrefs
            .edit()
            .putInt(mResources.getString(R.string.pref_high_score), highScore)
            .apply()
    }
}