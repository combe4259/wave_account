package com.example.accountbook.ui.components

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.accountbook.model.Category

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
    categories: List<Category>,
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
            modifier = Modifier.padding(bottom = 12.dp)
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
 * 실제 카테고리 그리드를 그리는 컴포넌트
 * LazyVerticalGrid 대신 일반 Column과 Row 조합을 사용해서
 * LazyColumn 안에서도 안전하게 사용할 수 있도록 만들었습니다.
 */
@Composable
fun CategoryGrid(
    categories: List<Category>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit,
    onAddNewCategory: () -> Unit
) {
    // 모든 아이템들을 하나의 리스트로 만들어서 3개씩 묶어서 처리
    val allItems = mutableListOf<CategoryItemData>().apply {
        // "카테고리 없음" 옵션 추가
        add(CategoryItemData.None(isSelected = selectedCategoryId == null))

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
                            is CategoryItemData.None -> {
                                CategoryGridItem(
                                    name = "없음",
                                    iconName = null,
                                    colorHex = "#9E9E9E",
                                    isSelected = itemData.isSelected,
                                    onClick = { onCategorySelected(null) }
                                )
                            }
                            is CategoryItemData.Category -> {
                                CategoryGridItem(
                                    name = itemData.category.name,
                                    iconName = itemData.category.iconName,
                                    colorHex = itemData.category.colorHex,
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
 * 이렇게 하면 각각 다른 타입의 아이템들을 안전하게 처리할 수 있습니다.
 */
sealed class CategoryItemData {
    data class None(val isSelected: Boolean) : CategoryItemData()
    data class Category(val category: com.example.accountbook.model.Category, val isSelected: Boolean) : CategoryItemData()
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
    colorHex: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {

    val MainColor = Color(0xFF5E69EE)

    val backgroundColor = if (isSelected) {
        Color.White
    } else {
        Color.White
    }

    // Material Design 3의 surface 시스템을 활용한 자연스러운 배경색
    val backgroundColor = if (isSelected) {
        // 선택시에는 카테고리 색상을 매우 미묘하게 블렌딩
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f)
    } else {
        // 기본 상태에서는 시스템 표면 색상 사용
        MaterialTheme.colorScheme.surfaceContainer
    }

    // 선택 상태는 카테고리 색상으로 미묘한 테두리 표현
    val borderColor = if (isSelected) {
        categoryColor.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // 정사각형 비율 유지 - 이것이 핵심!
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 아이콘 부분
            if (iconName != null) {
                Text(
                    text = getIconEmoji(iconName),
                    fontSize = 16.sp, // 크기 조금 줄임
                )
            } else {
                Surface(
                    modifier = Modifier.size(16.dp), // 크기 줄임
                    shape = RoundedCornerShape(50),
                    color = Color.White
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = name.take(1), // 첫 글자만 표시
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = categoryColor
                            )
                        }
                    }
                }
            }

            // 카테고리 이름 - 시스템 텍스트 색상 사용으로 가독성 보장
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface, // 시스템 텍스트 색상 사용
                fontSize = 11.sp
            )
        }
    }
}

/**
 * 새 카테고리 추가 버튼 아이템
 *
 */
@Composable
fun AddNewCategoryItem(
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // 다른 카테고리들과 동일한 정사각형 비율
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

/**
 * 아이콘 이름을 이모지로 변환하는 유틸리티 함수
 *
 * 실제 프로젝트에서는 벡터 아이콘이나 이미지 리소스를 사용할 수 있지만,
 * 간단한 구현을 위해 이모지를 사용합니다.
 *
 * 각 카테고리의 성격을 잘 나타내는 이모지를 선택했으며,
 * 새로운 아이콘이 필요한 경우 여기에 추가하면 됩니다.
 */
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
        "clothes" -> "👕"
        "gift" -> "🎁"
        "medical" -> "💊"
        "transport" -> "🚌"
        "entertainment" -> "🎮"
        else -> "📦" // 기본 아이콘
    }
}