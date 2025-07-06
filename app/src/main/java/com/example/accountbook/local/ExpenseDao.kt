package com.example.accountbook.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.accountbook.model.Expense
import com.example.accountbook.dto.ExpenseWithCategory

@Dao
interface ExpenseDao {
    //모든 지출 조회
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): LiveData<List<Expense>>

    //카테고리 포함 모든 지출 조회
    @Query("""
        SELECT e.*, c.name as categoryName, c.iconName, c.colorHex 
        FROM expenses e 
        LEFT JOIN categories c ON e.categoryId = c.id 
        ORDER BY e.date DESC
    """)
    fun getAllExpensesWithCategory(): LiveData<List<ExpenseWithCategory>>

    // 사진이 있는 지출만 조회
    @Query("SELECT * FROM expenses WHERE photoUri IS NOT NULL ORDER BY date DESC")
    fun getExpensesWithPhotos(): LiveData<List<Expense>>

    // 사진이 있는 지출만 조회 카테고리 정보 포함
    @Query("""
        SELECT e.*, c.name as categoryName, c.iconName, c.colorHex 
        FROM expenses e 
        LEFT JOIN categories c ON e.categoryId = c.id 
        WHERE e.photoUri IS NOT NULL 
        ORDER BY e.date DESC
    """)
    fun getExpensesWithPhotosAndCategory(): LiveData<List<ExpenseWithCategory>>

    // 특정 지출 조회
    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): Expense? // 조회 결과 없을 수도 있기 때문에 nullable 타입 사용

    // 지출 추가 - Long 반환 (생성된 지출의 ID를 반환)
    @Insert
    suspend fun insertExpense(expense: Expense): Long

    // 지출 수정
    @Update
    suspend fun updateExpense(expense: Expense)

    // 지출 삭제
    @Delete
    suspend fun deleteExpense(expense: Expense)

    // ID로 지출 삭제 - 더 직접적인 삭제 방법
    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)

    // 특정 카테고리의 지출들 조회
    @Query("""
        SELECT e.*, c.name as categoryName, c.iconName, c.colorHex 
        FROM expenses e 
        LEFT JOIN categories c ON e.categoryId = c.id 
        WHERE e.categoryId = :categoryId 
        ORDER BY e.date DESC
    """)
    fun getExpensesByCategory(categoryId: Long): LiveData<List<ExpenseWithCategory>>

    // 특정 날짜 범위의 지출들 조회
    // 월별 지출 내역이나 특정 기간 분석
    @Query("""
        SELECT e.*, c.name as categoryName, c.iconName, c.colorHex 
        FROM expenses e 
        LEFT JOIN categories c ON e.categoryId = c.id 
        WHERE e.date BETWEEN :startDate AND :endDate 
        ORDER BY e.date DESC
    """)
    fun getExpensesByDateRange(startDate: Long, endDate: Long): LiveData<List<ExpenseWithCategory>>

    // 월별 지출 합계 계산
    @Query("""
        SELECT SUM(amount) 
        FROM expenses 
        WHERE date BETWEEN :startDate AND :endDate
    """)
    suspend fun getMonthlyTotal(startDate: Long, endDate: Long): Double?

//    // 카테고리별 지출 합계 - 파이 차트나 카테고리 분석
//    @Query("""
//        SELECT c.name as categoryName, c.iconName, c.colorHex, SUM(e.amount) as totalAmount
//        FROM expenses e
//        LEFT JOIN categories c ON e.categoryId = c.id
//        WHERE e.date BETWEEN :startDate AND :endDate
//        GROUP BY e.categoryId
//        ORDER BY totalAmount DESC
//    """)
//    fun getCategoryTotals(startDate: Long, endDate: Long): LiveData<List<CategoryTotal>>
}

//data class CategoryTotal(
//    val categoryName: String?,
//    val iconName: String?,
//    val colorHex: String?,
//    val totalAmount: Double
//)