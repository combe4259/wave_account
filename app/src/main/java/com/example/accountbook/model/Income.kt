package com.example.accountbook.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "incomes",
    foreignKeys = [
        ForeignKey(
            entity = IncomeCategory::class,
            parentColumns = ["id"], //IncomeCategory의 id 참조
            childColumns = ["categoryId"], //Income의 categoryId가 category의 id 참조
            onDelete = ForeignKey.SET_NULL // 카테고리 삭제 시 데이터는 남김
        )
    ],
    indices = [Index(value = ["categoryId"])]
)
data class Income(
    @PrimaryKey(autoGenerate = true)
    val id : Long = 0L,
    val description: String,
    val amount: Double,
    val categoryId: Long? = null,
    val date: Long
)
