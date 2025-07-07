package com.example.accountbook.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.accountbook.model.ExpenseCategory
import com.example.accountbook.model.IncomeCategory

@Dao
interface IncomeCategoryDao {

    //카테고리 추가
    @Insert
    suspend fun insertCategory(category: IncomeCategory): Long

    //카테고리 수정
    @Update
    suspend fun updateCategory(category: IncomeCategory)
    //카테고리 삭제
    @Delete
    suspend fun deleteCategory(category: IncomeCategory)

    //모든 카테고리 조회
    @Query("SELECT * FROM income_categories ORDER BY createdAt ASC")
    fun getAllCategories(): LiveData<List<IncomeCategory>>

    //Id로 카테고리 조회
    @Query("SELECT * FROM income_categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): IncomeCategory?

    @Query("SELECT * FROM income_categories WHERE name = :name")
    suspend fun getCategoryByName(name: String): IncomeCategory?
}