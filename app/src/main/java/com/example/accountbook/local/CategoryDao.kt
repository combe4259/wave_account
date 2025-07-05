package com.example.accountbook.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.accountbook.model.Category

@Dao
interface CategoryDao {

    // 카테고리 추가
    @Insert
    suspend fun insertCategory(category: Category): Long

    // 카테고리 수정
    @Update
    suspend fun updateCategory(category: Category)

    // 카테고리 삭제
    @Delete
    suspend fun deleteCategory(category: Category)

    // 모든 카테고리 조회
    @Query("SELECT * FROM categories ORDER BY createdAt ASC")
    fun getAllCategories(): LiveData<List<Category>>

    // ID로 카테고리 조회
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): Category?

    // 이름으로 카테고리 조회 중복 체크용
    @Query("SELECT * FROM categories WHERE name = :name")
    suspend fun getCategoryByName(name: String): Category?

}