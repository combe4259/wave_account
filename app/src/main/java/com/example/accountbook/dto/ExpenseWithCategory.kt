package com.example.accountbook.dto

data class ExpenseWithCategory(
    val id: Long,
    val productName: String,
    val amount: Double,
    val categoryId: Long?,
    val date: Long,
    val photoUri: String?,
    val categoryName: String?,
    val iconName: String?,
    val colorHex: String?
)