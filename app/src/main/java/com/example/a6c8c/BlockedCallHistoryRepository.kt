package com.example.a6c8c

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import org.json.JSONArray
import org.json.JSONObject

object BlockedCallHistoryRepository {
    private const val PREFS_NAME = "blocked_call_history"
    private const val KEY_HISTORY = "history_list"
    private var isLoaded = false

    val history = mutableStateListOf<BlockedCall>()

    fun load(context: Context) {
        if (isLoaded) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_HISTORY, "[]")
        
        history.clear()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val number = obj.getString("number")
                val typeStr = obj.getString("type")
                val timestamp = obj.getLong("timestamp")
                val type = try {
                    BlockType.valueOf(typeStr)
                } catch (e: IllegalArgumentException) {
                    BlockType.TYPE_OTHER
                }
                history.add(BlockedCall(number, type, timestamp))
            }
            // Sort by timestamp descending
            history.sortByDescending { it.timestamp }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        isLoaded = true
    }

    fun save(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        history.forEach { call ->
            val obj = JSONObject()
            obj.put("number", call.number)
            obj.put("type", call.type.name)
            obj.put("timestamp", call.timestamp)
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_HISTORY, jsonArray.toString()).apply()
    }

    fun addCall(context: Context, call: BlockedCall) {
        if (!isLoaded) load(context)
        history.add(0, call)
        save(context)
    }
}
