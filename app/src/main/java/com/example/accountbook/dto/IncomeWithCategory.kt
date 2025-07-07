package com.example.accountbook.dto

data class IncomeWithCategory(
    val id: Long,
    val description: String,
    val amount: Double,
    val categoryId: Long?,
    val date: Long,
    val categoryName: String?,
    val iconName: String?,

)

