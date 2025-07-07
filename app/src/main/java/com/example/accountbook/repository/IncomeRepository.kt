package com.example.accountbook.repository

import androidx.lifecycle.LiveData
import com.example.accountbook.dto.IncomeWithCategory
import com.example.accountbook.local.IncomeDao
import com.example.accountbook.model.Income

class IncomeRepository(private val incomeDao: IncomeDao) {

    //모든 수입 데이터
    val allIncomes: LiveData<List<Income>> = incomeDao.getAllIncomes()

    //카테고리 포함 모든 수입 조회
    fun getAllIncomesWithCategory(): LiveData<List<IncomeWithCategory>>{
        return incomeDao.getAllIncomesWithCategory()
    }

    //수입 추가
    suspend fun insertIncome(income: Income): Long{
        return incomeDao.insertIncome(income)
    }
    //수입 수정
    suspend fun updateIncome(income: Income){
        incomeDao.updateIncome(income)
    }
    //수입 삭제
    suspend fun deleteIncome(income: Income){
        incomeDao.deleteIncome(income)
    }



}