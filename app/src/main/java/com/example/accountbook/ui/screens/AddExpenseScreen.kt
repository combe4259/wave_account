package com.example.accountbook.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.accountbook.model.Expense
import com.example.accountbook.view.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit
) {
    var productName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("식비") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    val categories = listOf("식비", "교통비", "쇼핑", "문화생활", "의료", "기타")
    val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA)

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 상품명 입력
        OutlinedTextField(
            value = productName,
            onValueChange = { productName = it },
            label = { Text("상품명") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // 금액 입력
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("금액") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            suffix = { Text("원") }
        )

        // 카테고리 선택
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = !isExpanded }
        ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = { },
                readOnly = true,
                label = { Text("카테고리") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            selectedCategory = category
                            isExpanded = false
                        }
                    )
                }
            }
        }

        // 날짜 선택
        OutlinedTextField(
            value = dateFormat.format(Date(selectedDate)),
            onValueChange = { },
            label = { Text("날짜") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = "날짜 선택")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        // 버튼들
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("취소")
            }

            Button(
                onClick = {
                    if (productName.isNotBlank() && amount.isNotBlank()) {
                        val expense = Expense(
                            productName = productName,
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            category = selectedCategory,
                            date = selectedDate
                        )
                        viewModel.insertExpense(expense)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = productName.isNotBlank() && amount.isNotBlank()
            ) {
                Text("추가")
            }
        }
    }

    // 날짜 선택 다이얼로그 (간단한 버전)
    if (showDatePicker) {
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text("날짜 선택") },
            text = { Text("현재는 오늘 날짜로 설정됩니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDate = System.currentTimeMillis()
                        showDatePicker = false
                    }
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("취소")
                }
            }
        )
    }
}