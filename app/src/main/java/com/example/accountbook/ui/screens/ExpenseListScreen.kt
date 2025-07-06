//package com.example.accountbook.ui.screens
//
//import androidx.compose.ui.Modifier
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Delete
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.runtime.livedata.observeAsState
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import com.example.accountbook.model.Expense
//import com.example.accountbook.view.ExpenseViewModel
//import java.text.NumberFormat
//import java.text.SimpleDateFormat
//import java.util.*
//
//@Composable
//fun ExpenseListScreen(
//    viewModel: ExpenseViewModel,
//    modifier: Modifier = Modifier,
//    onNavigateToAdd: () -> Unit
//) {
//    val expenses by viewModel.allExpensesWithCategory.observeAsState(emptyList())
//
//    Column(modifier = modifier.padding(16.dp)) {
//        // 총 지출 금액 표시
//        if (expenses.isNotEmpty()) {
//            val totalAmount = expenses.sumOf { it.amount }
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(bottom = 16.dp),
//                colors = CardDefaults.cardColors(
//                    containerColor = MaterialTheme.colorScheme.primaryContainer
//                )
//            ) {
//                Column(
//                    modifier = Modifier.padding(16.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Text(
//                        text = "총 지출",
//                        style = MaterialTheme.typography.titleMedium
//                    )
//                    Text(
//                        text = NumberFormat.getNumberInstance(Locale.KOREA).format(totalAmount) + "원",
//                        style = MaterialTheme.typography.headlineMedium,
//                        fontWeight = FontWeight.Bold,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                }
//            }
//        }
//
//        // 지출 목록
//        if (expenses.isEmpty()) {
//            // 빈 상태
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                    Text(
//                        text = "아직 지출 내역이 없습니다",
//                        style = MaterialTheme.typography.bodyLarge,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                    Spacer(modifier = Modifier.height(16.dp))
//                    Button(onClick = onNavigateToAdd) {
//                        Text("첫 지출 추가하기")
//                    }
//                }
//            }
//        } else {
//            LazyColumn(
//                verticalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                items(expenses) { expense ->
//                    ExpenseItem(
//                        expense = expense,
//                        onDelete = { viewModel.deleteExpense(expense) }
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun ExpenseItem(
//    expense: Expense,
//    onDelete: () -> Unit
//) {
//    val dateFormat = SimpleDateFormat("MM월 dd일 HH:mm", Locale.KOREA)
//
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Column(
//                modifier = Modifier.weight(1f)
//            ) {
//                Text(
//                    text = expense.productName,
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.Medium,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//
//                Spacer(modifier = Modifier.height(4.dp))
//
//                Row(
//                    horizontalArrangement = Arrangement.spacedBy(8.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        text = expense.category,
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                    Text(
//                        text = "•",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                    Text(
//                        text = dateFormat.format(Date(expense.date)),
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//            }
//
//            Column(
//                horizontalAlignment = Alignment.End,
//                verticalArrangement = Arrangement.spacedBy(4.dp)
//            ) {
//                Text(
//                    text = NumberFormat.getNumberInstance(Locale.KOREA).format(expense.amount) + "원",
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.Bold
//                )
//
//                IconButton(
//                    onClick = onDelete,
//                    modifier = Modifier.size(24.dp)
//                ) {
//                    Icon(
//                        Icons.Default.Delete,
//                        contentDescription = "삭제",
//                        tint = MaterialTheme.colorScheme.error,
//                        modifier = Modifier.size(20.dp)
//                    )
//                }
//            }
//        }
//    }
//}