package com.example.accountbook.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val productName: String,     // 상품명
    val amount: Double,          // 금액
    val category: String,        // 카테고리
    val date: Long,              // 날짜
    val photoUri: String? = null // 이미지 URI
)
