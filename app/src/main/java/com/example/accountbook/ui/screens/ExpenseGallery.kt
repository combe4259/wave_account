package com.example.accountbook.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.accountbook.dto.ExpenseWithCategory
import com.example.accountbook.view.ExpenseViewModel
import com.example.accountbook.view.ExpenseGalleryViewModel
import com.example.accountbook.view.GalleryUiState
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

//갤러리 메인 화면
@Composable
fun ExpenseGalleryScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val expensesWithImages by viewModel.expensesWithPhotosAndCategory.observeAsState(emptyList())

    var isLoading by remember { mutableStateOf(true) }
    var selectedExpense by remember { mutableStateOf<ExpenseWithCategory?>(null) }
    var showDetailDialog by remember { mutableStateOf(false) }

    // 데이터가 로드시 로딩 상태를 해제
    LaunchedEffect(expensesWithImages) {
        isLoading = false
    }

    GalleryContent(
        isLoading = isLoading,
        expensesWithImages = expensesWithImages,
        onImageClick = { expense ->
            selectedExpense = expense
            showDetailDialog = true
        },
        onRefresh = {
            isLoading = true
        },
        modifier = modifier
    )

    // 상세보기 다이얼로그
    if (showDetailDialog && selectedExpense != null) {
        ExpenseDetailDialog(
            expense = selectedExpense!!,
            onDismiss = {
                selectedExpense = null
                showDetailDialog = false
            }
        )
    }
}

/**
 * 갤러리의 메인 컨텐츠
 * 이제 개별 매개변수들을 받아서 처리하도록 변경되었습니다
 */
@Composable
private fun GalleryContent(
    isLoading: Boolean,  // GalleryUiState 대신 개별 매개변수들을 받습니다
    expensesWithImages: List<ExpenseWithCategory>,
    onImageClick: (ExpenseWithCategory) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            isLoading -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "갤러리를 불러오는 중...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            expensesWithImages.isEmpty() -> {  // 직접 매개변수를 사용합니다
                EmptyGalleryState(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                Column {
                    GalleryHeader(
                        imageCount = expensesWithImages.size,  // 직접 매개변수를 사용합니다
                        modifier = Modifier.padding(16.dp)
                    )

                    ImageGrid(
                        expenses = expensesWithImages,  // 직접 매개변수를 사용합니다
                        onImageClick = onImageClick,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

/**
 * 갤러리 상단 헤더
 */
@Composable
private fun GalleryHeader(
    imageCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.PhotoLibrary,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "갤러리",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "총 ${imageCount}개의 사진이 있습니다",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 이미지가 없을 때 표시되는 빈 상태 화면
 */
@Composable
private fun EmptyGalleryState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.PhotoLibrary,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "아직 사진이 없어요",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "지출을 기록할 때 사진을 함께 업로드하면\n이곳에서 모아볼 수 있어요",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Text(
                text = " 팁: 가계부 화면에서 '지출 추가'를 눌러 업로드해보세요",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(12.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 이미지들을 격자 형태로 표시하는 컴포넌트
 */
@Composable
private fun ImageGrid(
    expenses: List<ExpenseWithCategory>,
    onImageClick: (ExpenseWithCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = expenses,
            key = { expense -> expense.id }
        ) { expense ->
            GalleryImageItem(
                expense = expense,
                onClick = { onImageClick(expense) }
            )
        }
    }
}

/**
 * 개별 이미지 아이템
 */
@Composable
private fun GalleryImageItem(
    expense: ExpenseWithCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Box {
            // 메인 이미지
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(Uri.parse(expense.photoUri))
                    .crossfade(300)
                    .build(),
                contentDescription = "${expense.productName} 사진",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // 그라데이션 오버레이 효과를 위한 반투명 배경
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .align(Alignment.BottomCenter)
            )

            // 정보 오버레이
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = MaterialTheme.shapes.extraSmall
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    // 상품명
                    Text(
                        text = expense.productName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // 금액
                        Text(
                            text = NumberFormat.getInstance().format(expense.amount) + "원",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        // 카테고리
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text(
                                text = expense.categoryName ?: "미분류",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 이미지 상세보기 다이얼로그
 */
@Composable
private fun ExpenseDetailDialog(
    expense: ExpenseWithCategory,
    onDismiss: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 (E)", Locale.KOREA)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 16.dp,
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // 큰 이미지
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(Uri.parse(expense.photoUri))
                            .crossfade(300)
                            .build(),
                        contentDescription = "${expense.productName} 원본",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 제목
                Text(
                    text = expense.productName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 정보 섹션
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        DetailInfoRow(
                            label = "💰 금액",
                            value = NumberFormat.getInstance().format(expense.amount) + "원",
                            isHighlight = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        DetailInfoRow(
                            label = "📂 카테고리",
                            value = expense.categoryName ?: "미분류"
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        DetailInfoRow(
                            label = "📅 날짜",
                            value = dateFormat.format(Date(expense.date))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 버튼 영역
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("닫기", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

/**
 * 상세보기 다이얼로그의 정보 행 컴포넌트
 */
@Composable
private fun DetailInfoRow(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}