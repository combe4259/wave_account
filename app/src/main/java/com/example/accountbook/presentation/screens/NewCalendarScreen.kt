package com.example.accountbook.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.accountbook.domain.model.Transaction
import com.example.accountbook.presentation.main.MainViewModel
import com.example.accountbook.presentation.main.MainUiState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NewCalendarScreen(
    mainViewModel: MainViewModel,
    uiState: MainUiState,
    transactions: List<Transaction>,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // 월 표시
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = SimpleDateFormat("yyyy년 MM월", Locale.KOREAN)
                        .format(Date(uiState.selectedDate)),
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 월 예산
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("월 예산")
                    Text("${String.format("%,d", uiState.monthlyBudget)}원")
                }
                
                // 총 지출
                val totalExpense = transactions
                    .filterIsInstance<Transaction.Expense>()
                    .sumOf { it.amount }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("총 지출")
                    Text("${String.format("%,.0f", totalExpense)}원")
                }
                
                // 총 수입
                val totalIncome = transactions
                    .filterIsInstance<Transaction.Income>()
                    .sumOf { it.amount }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("총 수입")
                    Text("${String.format("%,.0f", totalIncome)}원")
                }
            }
        }
        
        // 거래 목록
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("거래 내역이 없습니다")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transactions.size) { index ->
                    TransactionItem(
                        transaction = transactions[index],
                        onClick = { onDateSelected(transactions[index].date) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = SimpleDateFormat("MM월 dd일", Locale.KOREAN)
                        .format(Date(transaction.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = when (transaction) {
                    is Transaction.Income -> "+${String.format("%,.0f", transaction.amount)}원"
                    is Transaction.Expense -> "-${String.format("%,.0f", transaction.amount)}원"
                },
                color = when (transaction) {
                    is Transaction.Income -> MaterialTheme.colorScheme.primary
                    is Transaction.Expense -> MaterialTheme.colorScheme.error
                },
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}