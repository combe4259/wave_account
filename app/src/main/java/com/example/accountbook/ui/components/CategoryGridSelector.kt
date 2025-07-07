package com.example.accountbook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.accountbook.model.ExpenseCategory

/**
 * 카테고리를 3×N 그리드 형태로 선택할 수 있는 컴포넌트
 *
 * 이 컴포넌트는 다음과 같은 기능을 제공합니다:
 * 1. 카테고리들을 3열 그리드로 표시
 * 2. 선택된 카테고리 강조 표시
 * 3. 마지막에 "+" 버튼으로 새 카테고리 추가 가능
 * 4. "카테고리 없음" 옵션 제공
 */
@Composable
fun CategoryGridSelector(
    categories: List<ExpenseCategory>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit,
    onAddNewCategory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // 카테고리 선택 제목
        Text(
            text = "카테고리 선택",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 12.dp),
            color = MaterialTheme.colorScheme.onSurface
        )

        // 카테고리 그리드
        CategoryGrid(
            categories = categories,
            selectedCategoryId = selectedCategoryId,
            onCategorySelected = onCategorySelected,
            onAddNewCategory = onAddNewCategory
        )
    }
}

/**
 * 실제 카테고리 그리드
 */
@Composable
fun CategoryGrid(
    categories: List<ExpenseCategory>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit,
    onAddNewCategory: () -> Unit
) {
    // 모든 아이템들을 하나의 리스트로 만들어서 3개씩 묶어서 처리
    val allItems = mutableListOf<CategoryItemData>().apply {
        // 기존 카테고리들 추가
        categories.forEach { category ->
            add(CategoryItemData.Category(
                category = category,
                isSelected = selectedCategoryId == category.id
            ))
        }

        // 새 카테고리 추가 버튼
        add(CategoryItemData.AddNew)
    }

    // 3개씩 묶어서 행으로 만들기
    val rows = allItems.chunked(3)

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { itemData ->
                    Box(modifier = Modifier.weight(1f)) {
                        when (itemData) {

                            is CategoryItemData.Category -> {
                                CategoryGridItem(
                                    name = itemData.category.name,
                                    iconName = itemData.category.iconName,
                                    isSelected = itemData.isSelected,
                                    onClick = { onCategorySelected(itemData.category.id) }
                                )
                            }
                            is CategoryItemData.AddNew -> {
                                AddNewCategoryItem(onClick = onAddNewCategory)
                            }
                            else -> error("알 수 없는 카테고리 아이템 타입: $itemData")
                        }
                    }
                }

                // 마지막 행에서 3개가 안 되는 경우 빈 공간 채우기
                repeat(3 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * 카테고리 아이템의 데이터 타입을 정의하는 sealed class
 */
sealed class CategoryItemData {
    data class None(val isSelected: Boolean) : CategoryItemData()
    data class Category(val category: ExpenseCategory, val isSelected: Boolean) : CategoryItemData()
    object AddNew : CategoryItemData()
}

/**
 * 개별 카테고리 아이템을 표시하는 컴포넌트
 *
 * Material Design 3의 색상 시스템을 활용하여 자연스럽고 조화로운 디자인을 구현합니다.
 * 카테고리별 고유 색상은 아이콘과 선택 상태 표현에만 사용하여 시각적 균형을 유지합니다.
 */
@Composable
fun CategoryGridItem(
    name: String,
    iconName: String?,
    isSelected: Boolean,
    onClick: () -> Unit
) {



    val MainColor = Color(0xFF5E69EE)

    val backgroundColor = if (isSelected) {
        Color.White
    } else {
        Color.White
    }


    // 선택 상태는 카테고리 색상으로 미묘한 테두리 표현

    val borderColor = if (isSelected) {
        MainColor
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2.5f)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = backgroundColor,
        tonalElevation = if (isSelected) 2.dp else 0.dp
    ) {
        // Column을 Row로 변경하고 가운데 정렬
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp), // 패딩 조금 줄임
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 아이콘 영역
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .padding(bottom = 6.dp),
                contentAlignment = Alignment.Center,


            ) {
                if (iconName != null) {
                    Text(
                        text = getIconEmoji(iconName),
                        fontSize = 16.sp,
                    )
                } else {
                    Surface(
                        modifier = Modifier.size(16.dp),
                        shape = RoundedCornerShape(50),
                        color = Color.White
                    ) {
                        Text(
                            text = name.take(1),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 간격 추가
            Spacer(modifier = Modifier.width(4.dp))

            // 텍스트 부분
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 10.sp, // 크기 조금 줄임
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}



/**
 * 새 카테고리 추가 버튼 아이템
 */
@Composable
fun AddNewCategoryItem(
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2.5f)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), // 미묘한 테두리
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = Color.White,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 아이콘 영역
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .padding(bottom = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "새 카테고리 추가",
                    tint = MaterialTheme.colorScheme.primary, // 시스템 primary 색상 사용
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = "추가",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp
            )
        }
    }
}

fun getIconEmoji(iconName: String): String {
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
        "clothes" -> "👕"
        "gift" -> "🎁"
        "medical" -> "💊"
        "transport" -> "🚌"
        "entertainment" -> "🎮"
        else -> "📦" // 기본 아이콘
    }
}