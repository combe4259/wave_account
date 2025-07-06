package com.example.accountbook.dto

data class MonthlyExpenseData(
    val totalAmount: Double,                              // 총 지출 금액
    val expenseCount: Int,                               // 총 지출 건수
    val expenseDays: Int,                                // 지출한 날짜 수
    val dailyTotals: Map<Int, Double>,                   // 날짜별 총액 (달력용)
    val allExpenses: List<ExpenseWithCategory>,          // 모든 지출 항목 (일일 탭용)
    val month: java.util.Calendar                                  // 해당 월 정보
)