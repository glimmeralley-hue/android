package com.example.wellnessapp

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ChatPersistence {
    fun saveMessages(context: Context, key: String, messages: List<ChatMessage>) {
        val sharedPref = context.getSharedPreferences("ChatHistory", Context.MODE_PRIVATE)
        val json = Gson().toJson(messages)
        sharedPref.edit().putString(key, json).apply()
    }

    fun loadMessages(context: Context, key: String): MutableList<ChatMessage> {
        val sharedPref = context.getSharedPreferences("ChatHistory", Context.MODE_PRIVATE)
        val json = sharedPref.getString(key, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<ChatMessage>>() {}.type
        return Gson().fromJson(json, type)
    }
}
