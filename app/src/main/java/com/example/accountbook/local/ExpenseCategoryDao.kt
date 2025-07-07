package com.example.accountbook.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.accountbook.model.ExpenseCategory

@Dao
interface ExpenseCategoryDao {

    // 카테고리 추가
    @Insert
    suspend fun insertCategory(category: ExpenseCategory): Long

    // 카테고리 수정
    @Update
    suspend fun updateCategory(category: ExpenseCategory)

    // 카테고리 삭제
    @Delete
    suspend fun deleteCategory(category: ExpenseCategory)

    // 모든 카테고리 조회
    @Query("SELECT * FROM expense_categories ORDER BY createdAt ASC")
    fun getAllCategories(): LiveData<List<ExpenseCategory>>

    // ID로 카테고리 조회
    @Query("SELECT * FROM expense_categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): ExpenseCategory?

    // 이름으로 카테고리 조회 중복 체크용
    @Query("SELECT * FROM expense_categories WHERE name = :name")
    suspend fun getCategoryByName(name: String): ExpenseCategory?

}