package com.example.a6c8c

enum class BlockType {
    TYPE_600,
    TYPE_809,
    TYPE_OTHER
}

data class BlockedCall(
    val number: String,
    val type: BlockType,
    val timestamp: Long
)
