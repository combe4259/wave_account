package com.example.accountbook.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.accountbook.model.Expense
import com.example.accountbook.ui.state.AddExpenseUiState
import com.example.accountbook.ui.utils.ImageHandler
import com.example.accountbook.view.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.example.accountbook.ui.components.inputs.*;
import com.example.accountbook.ui.components.images.*;
import com.example.accountbook.ui.components.dialogs.*;
import com.example.accountbook.ui.utils.rememberImageHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    initialDate: Long? = null
) {
    var uiState by remember {
        mutableStateOf(
            AddExpenseUiState(
                selectedDate = initialDate ?: System.currentTimeMillis()
            )
        )
    }

    val imageHandler = rememberImageHandler(
        onImageSelected = { uri -> uiState = uiState.copy(selectedImageUri = uri) },
        onShowPermissionDialog = { uiState = uiState.copy(showPermissionDialog = true) },
        onSetTempUri = { uri -> uiState = uiState.copy(tempCameraUri = uri) }
    )

    Scaffold(
        topBar = {
            AddExpenseTopBar(
                initialDate = initialDate,
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        AddExpenseContent(
            uiState = uiState,
            onUiStateChange = { uiState = it },
            onSaveExpense = { expense ->
                viewModel.insertExpense(expense)
                onNavigateBack()
            },
            imageHandler = imageHandler,
            modifier = modifier.padding(paddingValues)
        )
    }

    AddExpenseDialogs(
        uiState = uiState,
        onUiStateChange = { uiState = it },
        imageHandler = imageHandler
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExpenseTopBar(
    initialDate: Long?,
    onNavigateBack: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA)

    TopAppBar(
        title = {
            Text(
                text = if (initialDate != null) {
                    "${dateFormat.format(Date(initialDate))} 지출 추가"
                } else {
                    "지출 추가"
                }
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
            }
        }
    )
}

@Composable
private fun AddExpenseContent(
    uiState: AddExpenseUiState,
    onUiStateChange: (AddExpenseUiState) -> Unit,
    onSaveExpense: (Expense) -> Unit,
    imageHandler: ImageHandler,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProductNameInput(
            value = uiState.productName,
            onValueChange = { onUiStateChange(uiState.copy(productName = it)) }
        )

        AmountInput(
            value = uiState.amount,
            onValueChange = { onUiStateChange(uiState.copy(amount = it)) }
        )

        CategorySelector(
            selectedCategory = uiState.selectedCategory,
            isExpanded = uiState.isExpanded,
            onCategorySelected = { category ->
                onUiStateChange(uiState.copy(selectedCategory = category, isExpanded = false))
            },
            onExpandedChange = { expanded ->
                onUiStateChange(uiState.copy(isExpanded = expanded))
            }
        )

        DateSelector(
            selectedDate = uiState.selectedDate,
            onDateClick = { onUiStateChange(uiState.copy(showDatePicker = true)) }
        )

        ImageSection(
            selectedImageUri = uiState.selectedImageUri,
            onImageClick = { onUiStateChange(uiState.copy(showImageOptionsDialog = true)) },
            onImageRemove = { onUiStateChange(uiState.copy(selectedImageUri = null)) }
        )

        Spacer(modifier = Modifier.weight(1f))

        SaveButton(
            uiState = uiState,
            onSaveExpense = onSaveExpense
        )
    }
}




@Composable
private fun AddExpenseDialogs(
    uiState: AddExpenseUiState,
    onUiStateChange: (AddExpenseUiState) -> Unit,
    imageHandler: ImageHandler
) {
    if (uiState.showDatePicker) {
        DatePickerDialog(
            selectedDate = uiState.selectedDate,
            onDateSelected = { date ->
                onUiStateChange(uiState.copy(selectedDate = date ?: uiState.selectedDate, showDatePicker = false))
            },
            onDismiss = { onUiStateChange(uiState.copy(showDatePicker = false)) }
        )
    }

    if (uiState.showImageOptionsDialog) {
        ImageOptionsDialog(
            onCameraClick = {
                onUiStateChange(uiState.copy(showImageOptionsDialog = false))
                imageHandler.requestCameraPermission()
            },
            onGalleryClick = {
                onUiStateChange(uiState.copy(showImageOptionsDialog = false))
                imageHandler.launchGallery()
            },
            onDismiss = { onUiStateChange(uiState.copy(showImageOptionsDialog = false)) }
        )
    }

    if (uiState.showPermissionDialog) {
        PermissionDialog(
            onDismiss = { onUiStateChange(uiState.copy(showPermissionDialog = false)) }
        )
    }
}
