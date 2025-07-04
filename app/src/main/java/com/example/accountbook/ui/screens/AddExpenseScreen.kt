package com.example.accountbook.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.accountbook.model.Expense
import com.example.accountbook.view.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)//실험기능 사용
@Composable
fun AddExpenseScreen(viewModel: ExpenseViewModel, modifier: Modifier = Modifier, onNavigateBack: () -> Unit) {
    var productName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("식비") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    val categories = listOf("식비", "교통비", "쇼핑", "문화생활", "의료", "기타")
    val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA)

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate
    )


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

    // 실제 달력이 포함된 날짜 선택 다이얼로그
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { selectedDateMillis ->
                // 사용자가 날짜를 선택했을 때 실행되는 코드
                selectedDateMillis?.let {
                    selectedDate = it
                }
                showDatePicker = false
            },
            onDismiss = {
                // 다이얼로그를 닫을 때 실행되는 코드
                showDatePicker = false
            },
            datePickerState = datePickerState
        )
    }
}

/**
 * 재사용 가능한 날짜 선택 다이얼로그 컴포넌트
 * 이 컴포넌트는 Material Design 3의 DatePicker를 사용하여
 * 사용자 친화적인 달력 인터페이스를 제공합니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    datePickerState: DatePickerState
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // 다이얼로그 제목
                Text(
                    text = "날짜 선택",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 실제 달력 UI
                DatePicker(
                    state = datePickerState,
                    modifier = Modifier.fillMaxWidth()
                )

                // 버튼들
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    // 취소 버튼
                    TextButton(onClick = onDismiss) {
                        Text("취소")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // 확인 버튼
                    TextButton(
                        onClick = {
                            onDateSelected(datePickerState.selectedDateMillis)
                        }
                    ) {
                        Text("확인")
                    }
                }
            }
        }
    }
}