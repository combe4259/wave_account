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

    // === 기존 메서드들 (그대로 유지) ===

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

    // === ViewModel에서 필요로 하는 새로운 메서드들 추가 ===

    // 특정 카테고리의 지출들 조회 (카테고리 정보 포함)
    // 사용자가 "식비만 보고 싶다"고 할 때 사용하는 기능
    @Query("""
        SELECT e.*, c.name as categoryName, c.iconName, c.colorHex 
        FROM expenses e 
        LEFT JOIN categories c ON e.categoryId = c.id 
        WHERE e.categoryId = :categoryId 
        ORDER BY e.date DESC
    """)
    fun getExpensesByCategory(categoryId: Long): LiveData<List<ExpenseWithCategory>>

    // 특정 날짜 범위의 지출들 조회 (카테고리 정보 포함)
    // 월별 지출 내역이나 특정 기간 분석할 때 사용하는 기능
    @Query("""
        SELECT e.*, c.name as categoryName, c.iconName, c.colorHex 
        FROM expenses e 
        LEFT JOIN categories c ON e.categoryId = c.id 
        WHERE e.date BETWEEN :startDate AND :endDate 
        ORDER BY e.date DESC
    """)
    fun getExpensesByDateRange(startDate: Long, endDate: Long): LiveData<List<ExpenseWithCategory>>

    // === 추가적인 유용한 메서드들 ===

    // 월별 지출 합계 계산 - 통계나 요약 정보를 위한 메서드
    @Query("""
        SELECT SUM(amount) 
        FROM expenses 
        WHERE date BETWEEN :startDate AND :endDate
    """)
    suspend fun getMonthlyTotal(startDate: Long, endDate: Long): Double?

    // 카테고리별 지출 합계 - 파이 차트나 카테고리 분석에 유용
    @Query("""
        SELECT c.name as categoryName, c.iconName, c.colorHex, SUM(e.amount) as totalAmount
        FROM expenses e
        LEFT JOIN categories c ON e.categoryId = c.id
        WHERE e.date BETWEEN :startDate AND :endDate
        GROUP BY e.categoryId
        ORDER BY totalAmount DESC
    """)
    fun getCategoryTotals(startDate: Long, endDate: Long): LiveData<List<CategoryTotal>>
}

// 카테고리별 합계 정보를 담는 데이터 클래스
// 이 클래스는 통계 화면에서 "어떤 카테고리에 얼마나 썼는지" 보여줄 때 사용
data class CategoryTotal(
    val categoryName: String?,
    val iconName: String?,
    val colorHex: String?,
    val totalAmount: Double
)