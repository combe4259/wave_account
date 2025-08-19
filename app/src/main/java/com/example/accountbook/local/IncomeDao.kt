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
    
    // ID로 수입 삭제
    @Query("DELETE FROM incomes WHERE id = :id")
    suspend fun deleteIncomeById(id: Long)
    
    // 날짜 범위로 수입 조회 (카테고리 정보 포함)
    @Query("""
        SELECT i.*, c.name as categoryName, c.iconName
        FROM incomes i
        LEFT JOIN income_categories c ON i.categoryId = c.id
        WHERE i.date >= :startDate AND i.date <= :endDate
        ORDER BY i.date DESC
    """)
    fun getIncomesByDateRange(startDate: Long, endDate: Long): LiveData<List<IncomeWithCategory>>
    
    // 카테고리별 수입 조회
    @Query("""
        SELECT i.*, c.name as categoryName, c.iconName
        FROM incomes i
        LEFT JOIN income_categories c ON i.categoryId = c.id
        WHERE i.categoryId = :categoryId
        ORDER BY i.date DESC
    """)
    fun getIncomesByCategory(categoryId: Long): LiveData<List<IncomeWithCategory>>




}