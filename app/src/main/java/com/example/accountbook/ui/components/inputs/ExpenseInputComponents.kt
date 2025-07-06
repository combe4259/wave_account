package com.example.accountbook.ui.components.inputs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.example.accountbook.model.Expense
import com.example.accountbook.dto.AddExpenseUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProductNameInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange, //텍스트 필도 변경시 전달 -> value로 전달 -> 리렌더링
        label = { Text("상품명") },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text("예: 점심식사, 지하철비") },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                // 다음 입력 필드로 포커스 이동
                focusManager.moveFocus(FocusDirection.Down)
            }
        ),
    )
}

@Composable
fun AmountInput(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("금액") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        suffix = { Text("원") },
        placeholder = { Text("0") }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(
    selectedCategory: String,
    isExpanded: Boolean,
    onCategorySelected: (String) -> Unit,
    onExpandedChange: (Boolean) -> Unit
) {
    val categories = listOf("식비", "교통비", "쇼핑", "문화생활", "의료", "기타")

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = onExpandedChange
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
            onDismissRequest = { onExpandedChange(false) }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = { onCategorySelected(category) }
                )
            }
        }
    }
}

@Composable
fun DateSelector(
    selectedDate: Long,
    onDateClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA)

    OutlinedTextField(
        value = dateFormat.format(Date(selectedDate)),
        onValueChange = { },
        label = { Text("날짜") },
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = onDateClick) {
                Icon(Icons.Default.DateRange, contentDescription = "날짜 선택")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun SaveButton(
    uiState: AddExpenseUiState,
    onSaveExpense: (Expense) -> Unit
) {
    Button(
        onClick = {
            if (uiState.isValid) {
                val expense = Expense(
                    productName = uiState.productName,
                    amount = uiState.amountAsDouble,
                    //category = uiState.selectedCategory,
                    date = uiState.selectedDate,
                    photoUri = uiState.selectedImageUri?.toString()
                )
                onSaveExpense(expense)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = uiState.isValid
    ) {
        Text("지출 추가하기")
    }
}