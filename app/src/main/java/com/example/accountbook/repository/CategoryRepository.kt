package com.example.accountbook.repository

import androidx.lifecycle.LiveData
import com.example.accountbook.local.CategoryDao
import com.example.accountbook.model.Category

class CategoryRepository(private val categoryDao: CategoryDao) {

    // 모든 카테고리 조회
    fun getAllCategories(): LiveData<List<Category>> = categoryDao.getAllCategories()

    // 카테고리 추가
    suspend fun insertCategory(category: Category): Long = categoryDao.insertCategory(category)
    // 새 카테고리 생성
    suspend fun createCategory(name: String, iconName: String, colorHex: String): Long? {
        // 이름이 비어있는지 체크
        if (name.isBlank()) return null
        // 중복 체크
        if (isCategoryNameExists(name.trim())) return null

        val category = Category(
            name = name.trim(),
            iconName = iconName,
            colorHex = colorHex
        )
        return insertCategory(category)
    }

    // 카테고리 수정
    suspend fun updateCategory(category: Category) = categoryDao.updateCategory(category)
    // 카테고리 이름 수정 (중복 체크 포함)
    suspend fun updateCategoryName(categoryId: Long, newName: String): Boolean {
        val category = getCategoryById(categoryId) ?: return false

        // 같은 이름이면 수정할 필요 없음
        if (category.name == newName.trim()) return true

        // 중복 체크
        val existingCategory = getCategoryByName(newName.trim())
        if (existingCategory != null && existingCategory.id != categoryId) {
            return false
        }

        val updatedCategory = category.copy(name = newName.trim())
        updateCategory(updatedCategory)
        return true
    }

    // 카테고리 삭제
    suspend fun deleteCategory(category: Category) = categoryDao.deleteCategory(category)

    // ID로 카테고리 조회
    suspend fun getCategoryById(id: Long): Category? = categoryDao.getCategoryById(id)

    // 이름으로 카테고리 조회 (중복 체크용)
    suspend fun getCategoryByName(name: String): Category? = categoryDao.getCategoryByName(name)

    // 카테고리 이름 중복 체크
    suspend fun isCategoryNameExists(name: String): Boolean {
        return getCategoryByName(name) != null
    }

    // 기본 카테고리
    suspend fun insertDefaultCategories() {
        val defaultCategories = listOf(
            Category(name = "식비", iconName = "restaurant", colorHex = "#FF5722"),
            Category(name = "교통비", iconName = "directions_car", colorHex = "#2196F3"),
            Category(name = "쇼핑", iconName = "shopping_cart", colorHex = "#E91E63"),
            Category(name = "의료비", iconName = "local_hospital", colorHex = "#4CAF50"),
            Category(name = "문화생활", iconName = "movie", colorHex = "#9C27B0"),
            Category(name = "기타", iconName = "more_horiz", colorHex = "#607D8B")
        )
        defaultCategories.forEach { category ->
            // 중복 체크 후 추가
            if (!isCategoryNameExists(category.name)) {
                insertCategory(category)
            }
        }
    }




}