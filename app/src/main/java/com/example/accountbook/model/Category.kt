package com.example.accountbook.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val iconName: String, // 아이콘 이름을 문자열로 저장
    val colorHex: String, // 색상을 hex 문자열로 저장
    val createdAt: Long = System.currentTimeMillis()
)

