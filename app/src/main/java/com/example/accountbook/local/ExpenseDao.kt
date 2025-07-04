package com.example.accountbook.local


import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.accountbook.model.Expense

@Dao
interface ExpenseDao {

    //모든 지출 조회
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): LiveData<List<Expense>>

    // 사진이 있는 지출만 조회
    @Query("SELECT * FROM expenses WHERE photoUri IS NOT NULL ORDER BY date DESC")
    fun getExpensesWithPhotos(): LiveData<List<Expense>>

    // 특정 지출 조회
    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): Expense? //조회 결과 없을 수도 있기 때문에 ?(nullable) or Expense 반환
    //비동기 함수로 처리

    // 지출 추가 - Long 반환
    @Insert
    suspend fun insertExpense(expense: Expense): Long

    // 지출 수정
    @Update
    suspend fun updateExpense(expense: Expense)

    // 지출 삭제
    @Delete
    suspend fun deleteExpense(expense: Expense)

    // ID로 지출 삭제
    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)

    //기능 추가 고민
//    // 카테고리별 지출 조회
//    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY date DESC")
//    fun getExpensesByCategory(category: String): LiveData<List<Expense>>
//
//    // 날짜 범위로 지출 조회
//    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
//    fun getExpensesByDateRange(startDate: Long, endDate: Long): LiveData<List<Expense>>
}


