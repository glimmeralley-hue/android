package com.example.wellnessapp

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val uid: String? = null,
    val name: String? = null,
    val email: String? = null,
    val weight: String? = null,
    val age: String? = null,
    val goal: String? = null,
    val medicalHistory: String? = null,
    val createdAt: Long? = null
)

@IgnoreExtraProperties
data class Biostats(
    val weight: String? = null,
    val height: String? = null,
    val bmi: Float? = null,
    val timestamp: Long? = null
)

@IgnoreExtraProperties
data class ProductModel(
    val id: String? = null,
    val name: String? = null,
    val price: String? = null,
    val description: String? = null,
    val imageUrl: String? = null
)

@IgnoreExtraProperties
data class TransactionRecord(
    val transactionId: String? = null,
    val userId: String? = null,
    val productName: String? = null,
    val amount: String? = null,
    val phoneNumber: String? = null,
    val status: String? = "Pending",
    val timestamp: Long? = null
)
