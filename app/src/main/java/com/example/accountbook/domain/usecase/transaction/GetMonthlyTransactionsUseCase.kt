package com.example.accountbook.domain.usecase.transaction

import com.example.accountbook.domain.model.Transaction
import com.example.accountbook.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

class GetMonthlyTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(year: Int, month: Int): Flow<List<Transaction>> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val startDate = calendar.timeInMillis
        
        calendar.add(Calendar.MONTH, 1)
        val endDate = calendar.timeInMillis - 1
        
        return repository.getTransactionsByDateRange(startDate, endDate)
    }
}