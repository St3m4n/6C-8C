package com.example.a6c8c

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

object BlockedNumbersRepository {
    val blockedNumbers = mutableStateListOf<String>()
    
    var block600 by mutableStateOf(false)
    var block809 by mutableStateOf(false)
    var blockUnknown by mutableStateOf(false)

    fun addNumber(number: String) {
        if (!blockedNumbers.contains(number)) {
            blockedNumbers.add(number)
        }
    }

    fun removeNumber(number: String) {
        blockedNumbers.remove(number)
    }

    fun isBlocked(number: String): Boolean {
        return blockedNumbers.contains(number)
    }
}
