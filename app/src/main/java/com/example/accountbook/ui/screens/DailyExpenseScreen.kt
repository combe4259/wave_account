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
import com.example.accountbook.model.Expense
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




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyExpenseScreen(
    selectedDate: Long,
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onNavigateToAdd: (Long) -> Unit
) {
    val allExpenses by viewModel.allExpenses.observeAsState(emptyList())

    //팝업창 관리
    var selectedExpenseForDetail by remember { mutableStateOf<Expense?>(null) }



    // 선택된 날짜의 지출만 필터링
    val dailyExpenses = remember(allExpenses, selectedDate) {
        filterExpensesByDate(allExpenses, selectedDate)
    }

    val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 (E)", Locale.KOREA)
    val totalDailyExpense = dailyExpenses.sumOf { it.amount }

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
                expenseCount = dailyExpenses.size
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 지출 목록
            if (dailyExpenses.isEmpty()) {
                EmptyDayState(
                    onAddExpense = { onNavigateToAdd(selectedDate) }
                )
            } else {
                DailyExpenseList(
                    expenses = dailyExpenses,
                    onDeleteExpense = { expense ->
                        viewModel.deleteExpense(expense)
                    },
                    onExpenseClick = { expense ->
                        selectedExpenseForDetail = expense
                    }
                )
            }
        }

        selectedExpenseForDetail?.let { expense ->
            ExpenseDetailDialog(
                expense = expense,
                onDismiss = { selectedExpenseForDetail = null },
                onDelete = {
                    viewModel.deleteExpense(expense)
                    selectedExpenseForDetail = null
                }
            )
        }
    }
}


@Composable
fun DailySummaryCard(
    totalExpense: Double,
    expenseCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "오늘의 지출",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${expenseCount}건",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Text(
                text = NumberFormat.getNumberInstance(Locale.KOREA).format(totalExpense) + "원",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
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
                text = "이 날짜에는 지출 내역이 없습니다",
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
                Text("지출 추가하기")
            }
        }
    }
}


@Composable
fun DailyExpenseList(
    expenses: List<Expense>,
    onDeleteExpense: (Expense) -> Unit,
    onExpenseClick: (Expense) -> Unit
) {
    //시간 정렬
    //expense가 바뀔 때만 블록 안의 코드 실행
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
//    @Composable
//    fun CategoryHeader(
//        category: String,
//        totalAmount: Double,
//        count: Int
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 8.dp),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                text = "$category ($count)개",
//                style = MaterialTheme.typography.titleSmall,
//                fontWeight = FontWeight.Medium,
//                color = MaterialTheme.colorScheme.primary
//            )
//
//            Text(
//                text = NumberFormat.getNumberInstance(Locale.KOREA).format(totalAmount) + "원",
//                style = MaterialTheme.typography.titleSmall,
//                fontWeight = FontWeight.Medium,
//                color = MaterialTheme.colorScheme.primary
//            )
//        }
//    }


@Composable
fun ExpenseDetailDialog(
    expense: ExpenseWithCategory, // 타입 변경!
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH:mm", Locale.KOREA)

    AlertDialog(
        onDismissRequest = onDismiss,
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
                // 상품명
                DetailRow(
                    label = "상품명",
                    value = expense.productName
                )

                // 카테고리
                DetailRow(
                    label = "카테고리",
                    value = expense.categoryName ?: "카테고리 없음"
                )

                // 금액
                DetailRow(
                    label = "금액",
                    value = NumberFormat.getNumberInstance(Locale.KOREA)
                        .format(expense.amount) + "원"
                )

                // 날짜
                DetailRow(
                    label = "날짜",
                    value = dateFormat.format(Date(expense.date))
                )

                // 이미지 (있는 경우만)
                expense.photoUri?.let { imagePath ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "첨부 이미지",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 이미지 표시
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
fun DailyExpenseItem(
    : ExpenseWithCategory,expense
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
        val timeFormat = SimpleDateFormat("HH:mm", Locale.KOREA)

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            onClick = onClick
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = expense.productName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = timeFormat.format(Date(expense.date)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = NumberFormat.getNumberInstance(Locale.KOREA)
                            .format(expense.amount) + "원",
                        style = MaterialTheme.typography.bodyLarge,
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
    fun ExpenseDetailDialog(
        expense: Expense,
        onDismiss: () -> Unit,
        onDelete: () -> Unit
    ) {
        val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH:mm", Locale.KOREA)

        AlertDialog(
            onDismissRequest = onDismiss,
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
                    // 상품명
                    DetailRow(
                        label = "상품명",
                        value = expense.productName
                    )

                    // 카테고리
//                    DetailRow(
//                        label = "카테고리",
//                        //value = expense.category
//                    )

                    // 금액
                    DetailRow(
                        label = "금액",
                        value = NumberFormat.getNumberInstance(Locale.KOREA)
                            .format(expense.amount) + "원"
                    )

                    // 날짜
                    DetailRow(
                        label = "날짜",
                        value = dateFormat.format(Date(expense.date))
                    )


                    // 이미지 (있는 경우만)
                    expense.photoUri?.let { imagePath ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "첨부 이미지",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // 이미지 표시
                        AsyncImage(
                            model = imagePath,
                            contentDescription = "지출 이미지",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = android.R.drawable.ic_menu_report_image), // 기본 에러 아이콘 사용
                            placeholder = painterResource(id = android.R.drawable.ic_menu_gallery) // 기본 갤러리 아이콘 사용
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


    // 유틸리티 함수
    fun filterExpensesByDate(expenses: List<Expense>, targetDate: Long): List<Expense> {
        val targetCalendar = Calendar.getInstance().apply { timeInMillis = targetDate }
        val targetYear = targetCalendar.get(Calendar.YEAR)
        val targetMonth = targetCalendar.get(Calendar.MONTH)
        val targetDay = targetCalendar.get(Calendar.DAY_OF_MONTH)

        return expenses.filter { expense ->
            val expenseCalendar = Calendar.getInstance().apply { timeInMillis = expense.date }
            expenseCalendar.get(Calendar.YEAR) == targetYear &&
                    expenseCalendar.get(Calendar.MONTH) == targetMonth &&
                    expenseCalendar.get(Calendar.DAY_OF_MONTH) == targetDay
        }.sortedByDescending { it.date }
    }

