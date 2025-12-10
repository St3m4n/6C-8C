package com.example.a6c8c

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

object BlockedNumbersRepository {
    private const val PREFS_NAME = "blocked_numbers_prefs"
    private const val KEY_BLOCK_600 = "block_600"
    private const val KEY_BLOCK_809 = "block_809"
    private const val KEY_BLOCK_UNKNOWN = "block_unknown"
    private const val KEY_BLOCKED_NUMBERS = "blocked_numbers_list"

    val blockedNumbers = mutableStateListOf<String>()
    
    var block600 by mutableStateOf(false)
    var block809 by mutableStateOf(false)
    var blockUnknown by mutableStateOf(false)

    fun load(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        block600 = prefs.getBoolean(KEY_BLOCK_600, false)
        block809 = prefs.getBoolean(KEY_BLOCK_809, false)
        blockUnknown = prefs.getBoolean(KEY_BLOCK_UNKNOWN, false)
        
        val savedNumbers = prefs.getStringSet(KEY_BLOCKED_NUMBERS, emptySet()) ?: emptySet()
        blockedNumbers.clear()
        blockedNumbers.addAll(savedNumbers)
    }

    fun save(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean(KEY_BLOCK_600, block600)
            putBoolean(KEY_BLOCK_809, block809)
            putBoolean(KEY_BLOCK_UNKNOWN, blockUnknown)
            putStringSet(KEY_BLOCKED_NUMBERS, blockedNumbers.toSet())
            apply()
        }
    }

    fun addNumber(context: Context, number: String) {
        if (!blockedNumbers.contains(number)) {
            blockedNumbers.add(number)
            save(context)
        }
    }

    fun removeNumber(context: Context, number: String) {
        if (blockedNumbers.remove(number)) {
            save(context)
        }
    }

    fun isBlocked(number: String): Boolean {
        return blockedNumbers.contains(number)
    }
}
