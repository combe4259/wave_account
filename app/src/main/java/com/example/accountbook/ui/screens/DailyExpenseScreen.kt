package com.example.accountbook.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.accountbook.dto.ExpenseWithCategory
import com.example.accountbook.dto.IncomeWithCategory
import com.example.accountbook.view.ExpenseViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import coil.compose.AsyncImage
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.accountbook.ui.theme.MainColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyExpenseScreen(
    selectedDate: Long,
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onNavigateToAdd: (Long) -> Unit
) {

    val allExpensesWithCategory by viewModel.allExpensesWithCategory.observeAsState(emptyList())
    val allIncomesWithCategory by viewModel.allIncomesWithCategory.observeAsState(emptyList())

    //팝업창 관리
    var selectedExpenseForDetail by remember { mutableStateOf<ExpenseWithCategory?>(null) }
    var selectedIncomeForDetail by remember { mutableStateOf<IncomeWithCategory?>(null) }

    // 선택된 날짜의 지출과 수입 필터링
    val dailyExpenses = remember(allExpensesWithCategory, selectedDate) {
        filterExpensesByDate(allExpensesWithCategory, selectedDate)
    }

    val dailyIncomes = remember(allIncomesWithCategory, selectedDate) {
        filterIncomesByDate(allIncomesWithCategory, selectedDate)
    }

    //삭제 확인 다이얼로그 상태 관리
    var expenseToDelete by remember { mutableStateOf<ExpenseWithCategory?>(null) }
    var incomeToDelete by remember { mutableStateOf<IncomeWithCategory?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // 헬퍼 함수들
    fun requestDeleteExpense(expense: ExpenseWithCategory) {
        expenseToDelete = expense
        showDeleteConfirmDialog = true
    }

    fun requestDeleteIncome(income: IncomeWithCategory) {
        incomeToDelete = income
        showDeleteConfirmDialog = true
    }

    // 실제 삭제를 수행하는 함수
    fun confirmDelete() {
        expenseToDelete?.let { expense ->
            viewModel.deleteExpense(expense.toExpense())
        }
        incomeToDelete?.let { income ->
            viewModel.deleteIncome(income.toIncome())
        }
        // 상태 초기화
        expenseToDelete = null
        incomeToDelete = null
        showDeleteConfirmDialog = false
        selectedExpenseForDetail = null
        selectedIncomeForDetail = null
    }

    val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 (E)", Locale.KOREA)
    val totalDailyExpense = dailyExpenses.sumOf { it.amount }
    val totalDailyIncome = dailyIncomes.sumOf { it.amount }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(dateFormat.format(Date(selectedDate)))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToAdd(selectedDate) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add,
                    contentDescription = "지출 추가",
                    tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 일일 지출 요약 카드
            DailySummaryCard(
                totalExpense = totalDailyExpense,
                totalIncome = totalDailyIncome,
                expenseCount = dailyExpenses.size,
                incomeCount = dailyIncomes.size
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 수입/지출 목록
            if (dailyExpenses.isEmpty() && dailyIncomes.isEmpty()) {
                EmptyDayState(
                    onAddExpense = { onNavigateToAdd(selectedDate) }
                )
            } else {
                // 수입 목록
                if (dailyIncomes.isNotEmpty()) {
                    Text(
                        text = "수입",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFff4949),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    DailyIncomeList(
                        incomes = dailyIncomes,
                        onDeleteIncome = { income ->
                            requestDeleteIncome(income)
                        },
                        onIncomeClick = { income ->
                            selectedIncomeForDetail = income
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 지출 목록
                if (dailyExpenses.isNotEmpty()) {
                    Text(
                        text = "지출",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    DailyExpenseList(
                        expenses = dailyExpenses,
                        onDeleteExpense = { expense ->
                            requestDeleteExpense(expense)
                        },
                        onExpenseClick = { expense ->
                            selectedExpenseForDetail = expense
                        }
                    )
                }
            }
        }

        // 지출 상세 다이얼로그
        selectedExpenseForDetail?.let { expense ->
            ExpenseDetailDialog(
                expense = expense,
                onDismiss = { selectedExpenseForDetail = null },
                onDelete = {
                    requestDeleteExpense(expense)
                }
            )
        }

        // 수입 상세 다이얼로그
        selectedIncomeForDetail?.let { income ->
            IncomeDetailDialog(
                income = income,
                onDismiss = { selectedIncomeForDetail = null },
                onDelete = {
                    requestDeleteIncome(income)
                }
            )
        }

        // 삭제 확인 다이얼로그
        if (showDeleteConfirmDialog) {
            val itemName = expenseToDelete?.productName ?: incomeToDelete?.description ?: ""
            val itemType = if (expenseToDelete != null) "지출" else "수입"

            DeleteConfirmDialog(
                expenseName = itemName,
                itemType = itemType,
                onConfirm = {
                    confirmDelete()
                },
                onDismiss = {
                    expenseToDelete = null
                    incomeToDelete = null
                    showDeleteConfirmDialog = false
                }
            )
        }
    }
}

@Composable
fun DailySummaryCard(
    totalExpense: Double,
    totalIncome: Double,
    expenseCount: Int,
    incomeCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "오늘의 내역",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${incomeCount + expenseCount}건",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            if (totalIncome > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "수입 (${incomeCount}건)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFff4949)
                        )
                    }
                    Text(
                        text = "${NumberFormat.getNumberInstance(Locale.KOREA).format(totalIncome)}원",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFff4949)
                    )
                }
            }

            if (totalExpense > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "지출 (${expenseCount}건)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "${NumberFormat.getNumberInstance(Locale.KOREA).format(totalExpense)}원",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyDayState(
    onAddExpense: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "이 날짜에는 내역이 없습니다",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onAddExpense,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("내역 추가하기")
            }
        }
    }
}

@Composable
fun DailyIncomeList(
    incomes: List<IncomeWithCategory>,
    onDeleteIncome: (IncomeWithCategory) -> Unit,
    onIncomeClick: (IncomeWithCategory) -> Unit
) {
    val sortedIncomes = remember(incomes) {
        incomes.sortedByDescending { it.date }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(sortedIncomes) { income ->
            DailyIncomeItem(
                income = income,
                onDelete = { onDeleteIncome(income) },
                onClick = { onIncomeClick(income) }
            )
        }
    }
}

@Composable
fun DailyExpenseList(
    expenses: List<ExpenseWithCategory>,
    onDeleteExpense: (ExpenseWithCategory) -> Unit,
    onExpenseClick: (ExpenseWithCategory) -> Unit
) {
    val sortedExpenses = remember(expenses) {
        expenses.sortedByDescending { it.date }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(sortedExpenses) { expense ->
            DailyExpenseItem(
                expense = expense,
                onDelete = { onDeleteExpense(expense) },
                onClick = { onExpenseClick(expense) }
            )
        }
    }
}

@Composable
fun DailyExpenseItem(
    expense: ExpenseWithCategory,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.KOREA)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                expense.iconName?.let { iconName ->
                    Text(
                        text = getIconEmoji(iconName),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Column {
                    Text(
                        text = expense.productName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = expense.categoryName ?: "카테고리 없음",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = timeFormat.format(Date(expense.date)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${NumberFormat.getNumberInstance(Locale.KOREA).format(expense.amount)}원",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "삭제",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun IncomeDetailDialog(
    income: IncomeWithCategory,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH:mm", Locale.KOREA)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = "수입 상세 정보",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailRow(
                    label = "수입원",
                    value = income.description
                )

                DetailRow(
                    label = "카테고리",
                    value = income.categoryName ?: "카테고리 없음"
                )

                DetailRow(
                    label = "금액",
                    value = "${NumberFormat.getNumberInstance(Locale.KOREA).format(income.amount)}원"
                )

                DetailRow(
                    label = "날짜",
                    value = dateFormat.format(Date(income.date))
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("삭제")
            }
        }
    )
}

@Composable
fun ExpenseDetailDialog(
    expense: ExpenseWithCategory,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH:mm", Locale.KOREA)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = "지출 상세 정보",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailRow(
                    label = "상품명",
                    value = expense.productName
                )

                DetailRow(
                    label = "카테고리",
                    value = expense.categoryName ?: "카테고리 없음"
                )

                DetailRow(
                    label = "금액",
                    value = "${NumberFormat.getNumberInstance(Locale.KOREA).format(expense.amount)}원"
                )

                DetailRow(
                    label = "날짜",
                    value = dateFormat.format(Date(expense.date))
                )

                expense.photoUri?.let { imagePath ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "첨부 이미지",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    AsyncImage(
                        model = imagePath,
                        contentDescription = "지출 이미지",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = android.R.drawable.ic_menu_report_image),
                        placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("삭제")
            }
        }
    )
}

@Composable
fun DetailRow(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DeleteConfirmDialog(
    expenseName: String,
    itemType: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "$itemType 내역 삭제",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "다음 $itemType 내역을 삭제하시겠습니까?",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "\"$expenseName\"",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "삭제된 내역은 복구할 수 없습니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = "삭제",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

// 유틸리티 함수들
fun filterExpensesByDate(allExpensesWithCategory: List<ExpenseWithCategory>, targetDate: Long): List<ExpenseWithCategory> {
    val targetCalendar = Calendar.getInstance().apply { timeInMillis = targetDate }
    val targetYear = targetCalendar.get(Calendar.YEAR)
    val targetMonth = targetCalendar.get(Calendar.MONTH)
    val targetDay = targetCalendar.get(Calendar.DAY_OF_MONTH)

    return allExpensesWithCategory.filter { expense ->
        val expenseCalendar = Calendar.getInstance().apply { timeInMillis = expense.date }
        expenseCalendar.get(Calendar.YEAR) == targetYear &&
                expenseCalendar.get(Calendar.MONTH) == targetMonth &&
                expenseCalendar.get(Calendar.DAY_OF_MONTH) == targetDay
    }.sortedByDescending { it.date }
}

fun filterIncomesByDate(allIncomesWithCategory: List<IncomeWithCategory>, targetDate: Long): List<IncomeWithCategory> {
    val targetCalendar = Calendar.getInstance().apply { timeInMillis = targetDate }
    val targetYear = targetCalendar.get(Calendar.YEAR)
    val targetMonth = targetCalendar.get(Calendar.MONTH)
    val targetDay = targetCalendar.get(Calendar.DAY_OF_MONTH)

    return allIncomesWithCategory.filter { income ->
        val incomeCalendar = Calendar.getInstance().apply { timeInMillis = income.date }
        incomeCalendar.get(Calendar.YEAR) == targetYear &&
                incomeCalendar.get(Calendar.MONTH) == targetMonth &&
                incomeCalendar.get(Calendar.DAY_OF_MONTH) == targetDay
    }.sortedByDescending { it.date }
}

private fun getIconEmoji(iconName: String): String {
    return when (iconName) {
        "restaurant" -> "🍽️"
        "directions_car" -> "🚗"
        "shopping_cart" -> "🛒"
        "local_hospital" -> "🏥"
        "movie" -> "🎬"
        "more_horiz" -> "📦"
        "coffee" -> "☕"
        "home" -> "🏠"
        "work" -> "💼"
        "school" -> "🏫"
        "sports" -> "⚽"
        "beauty" -> "💄"
        "gas_station" -> "⛽"
        "phone" -> "📱"
        "book" -> "📚"
        else -> "📦"
    }
}

// 확장 함수들
fun ExpenseWithCategory.toExpense() = com.example.accountbook.model.Expense(
    id = this.id,
    productName = this.productName,
    amount = this.amount,
    categoryId = this.categoryId,
    date = this.date,
    photoUri = this.photoUri
)



