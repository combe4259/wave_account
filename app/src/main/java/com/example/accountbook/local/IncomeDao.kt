package com.example.accountbook.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.accountbook.dto.IncomeWithCategory
import com.example.accountbook.model.Income

@Dao
interface IncomeDao {

    //모든 수입 조회
    @Query("SELECT * FROM incomes ORDER BY date DESC")
    fun getAllIncomes(): LiveData<List<Income>>

    //카테고리 포함 모든 수입 조회
    @Query("""
        SELECT i.*, c.name as categoryName, c.iconName
        FROM incomes i
        LEFT JOIN income_categories c ON i.categoryId = c.id
        ORDER BY i.date DESC
    """)
    fun getAllIncomesWithCategory(): LiveData<List<IncomeWithCategory>>

    //수입 추가
    @Insert
    suspend fun insertIncome(income : Income): Long

    //수입 수정
    @Update
    suspend fun updateIncome(income: Income)

    //수입 삭제
    @Delete
    suspend fun deleteIncome(income: Income)




}