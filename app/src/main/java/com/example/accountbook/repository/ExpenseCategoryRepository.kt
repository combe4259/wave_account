package com.example.accountbook.repository

import androidx.lifecycle.LiveData
import com.example.accountbook.local.ExpenseCategoryDao
import com.example.accountbook.model.ExpenseCategory

class ExpenseCategoryRepository(private val expenseCategoryDao: ExpenseCategoryDao) {

    // 모든 카테고리 조회
    fun getAllCategories(): LiveData<List<ExpenseCategory>> = expenseCategoryDao.getAllCategories()

    // 카테고리 추가
    suspend fun insertCategory(category: ExpenseCategory): Long = expenseCategoryDao.insertCategory(category)
    // 새 카테고리 생성
    suspend fun createCategory(name: String, iconName: String): Long? {
        if (name.isBlank()) return null
        if (isCategoryNameExists(name.trim())) return null

        val category = ExpenseCategory(
            name = name.trim(),
            iconName = iconName
        )
        return insertCategory(category)
    }

    // 카테고리 수정
    suspend fun updateCategory(category: ExpenseCategory) = expenseCategoryDao.updateCategory(category)
    // 카테고리 이름 수정
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
    suspend fun deleteCategory(category: ExpenseCategory) = expenseCategoryDao.deleteCategory(category)

    // ID로 카테고리 조회
    suspend fun getCategoryById(id: Long): ExpenseCategory? = expenseCategoryDao.getCategoryById(id)

    // 이름으로 카테고리 조회
    suspend fun getCategoryByName(name: String): ExpenseCategory? = expenseCategoryDao.getCategoryByName(name)

    // 카테고리 이름 중복 체크
    suspend fun isCategoryNameExists(name: String): Boolean {
        return getCategoryByName(name) != null
    }

    // 기본 카테고리
    suspend fun insertDefaultExpenseCategories() {
        val defaultCategories = listOf(
            ExpenseCategory(name = "식비", iconName = "restaurant"),
            ExpenseCategory(name = "교통비", iconName = "directions_car"),
            ExpenseCategory(name = "쇼핑", iconName = "shopping_cart"),
            ExpenseCategory(name = "의료비", iconName = "local_hospital"),
            ExpenseCategory(name = "문화생활", iconName = "movie"),
            ExpenseCategory(name = "기타", iconName = "more_horiz")
        )
        defaultCategories.forEach { category ->
            // 중복 체크 후 추가
            if (!isCategoryNameExists(category.name)) {
                insertCategory(category)
            }
        }
    }




}