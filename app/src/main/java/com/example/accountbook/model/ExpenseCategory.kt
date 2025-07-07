package com.example.accountbook.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_categories")
data class ExpenseCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val iconName: String, // 아이콘 이름을 문자열로 저장
    val createdAt: Long = System.currentTimeMillis()
)

