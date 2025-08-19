package com.example.accountbook.domain.usecase.transaction

import com.example.accountbook.domain.model.Transaction
import com.example.accountbook.domain.repository.TransactionRepository
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction): Result<Long> {
        return try {
            require(transaction.amount > 0) { "금액은 0보다 커야 합니다" }
            require(transaction.description.isNotBlank()) { "설명을 입력해주세요" }
            
            val id = repository.addTransaction(transaction)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}