package com.example.accountbook.repository

import androidx.lifecycle.LiveData
import com.example.accountbook.local.IncomeCategoryDao
import com.example.accountbook.model.ExpenseCategory
import com.example.accountbook.model.IncomeCategory

class IncomeCategoryRepository(private val incomeCategoryDao: IncomeCategoryDao){

    //모든 카테고리 조회
    fun getAllCategories(): LiveData<List<IncomeCategory>> = incomeCategoryDao.getAllCategories()
    //카테고리 추가
    suspend fun insertCategory(category: IncomeCategory): Long = incomeCategoryDao.insertCategory(category)
    //카테고리 수정
    suspend fun updateCategory(category: IncomeCategory) = incomeCategoryDao.updateCategory(category)
    //카테고리 삭제
    suspend fun deleteCategory(category: IncomeCategory) = incomeCategoryDao.deleteCategory(category)

    //중복 체크용
    suspend fun getCategoryByName(name: String): IncomeCategory? = incomeCategoryDao.getCategoryByName(name)
    //카테고리 이름 중복 체크
    suspend fun isCategoryNameExists(name: String): Boolean{
        return getCategoryByName(name) !=null
    }

    suspend fun insertDefaultIncomeCategories(){
        val defaultCategories = listOf(
            IncomeCategory(name = "월급", iconName = "work"),
            IncomeCategory(name = "부수입", iconName = "trending_up"),
            IncomeCategory(name = "용돈", iconName = "emoji_people"),
            IncomeCategory(name = "금융소득", iconName = "saving"),
            IncomeCategory(name = "기타", iconName = "attach_money")
        )
        defaultCategories.forEach{ category ->
            if(!isCategoryNameExists(category.name)){
                insertCategory(category)
            }

        }
    }
}