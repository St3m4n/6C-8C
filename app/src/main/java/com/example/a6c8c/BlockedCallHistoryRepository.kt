package com.example.a6c8c

import androidx.compose.runtime.mutableStateListOf

object BlockedCallHistoryRepository {
    val history = mutableStateListOf<BlockedCall>()

    fun addCall(call: BlockedCall) {
        history.add(0, call)
    }
}
