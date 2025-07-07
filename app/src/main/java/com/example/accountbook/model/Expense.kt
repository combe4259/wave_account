package com.example.accountbook.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = ExpenseCategory::class,
            parentColumns = ["id"], //ExpenseCategory의 id 참조
            childColumns = ["categoryId"], //expense의 categoryId가 category의 id 참조
            onDelete = ForeignKey.SET_NULL // 카테고리 삭제 시 지출 데이터는 남김
        )
    ],
    indices = [Index(value = ["categoryId"])]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val productName: String,     // 상품명
    val amount: Double,          // 금액
    val categoryId: Long? = null, //카테고리는 삭제가능
    val date: Long,              // 날짜
    val photoUri: String? = null // 이미지 URI
)
