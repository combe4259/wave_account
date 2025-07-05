package com.example.accountbook.ui.components


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/**
 * 새 카테고리 추가를 위한 다이얼로그
 *
 * 이 다이얼로그는 다음과 같은 기능을 제공합니다:
 * 1. 카테고리 이름 입력
 * 2. 아이콘 선택 (이모지 기반)
 * 3. 색상 선택 (미리 정의된 색상 팔레트)
 * 4. 실시간 미리보기
 * 5. 유효성 검사
 */
@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, iconName: String, colorHex: String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 다이얼로그 상태 관리
    var categoryName by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("more_horiz") }
    var selectedColor by remember { mutableStateOf("#2196F3") }

    // 유효성 검사
    val isNameValid = categoryName.trim().isNotEmpty()
    val canConfirm = isNameValid

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 다이얼로그 제목
                Text(
                    text = "새 카테고리 추가",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // 미리보기 섹션
                CategoryPreview(
                    name = categoryName.ifEmpty { "카테고리 이름" },
                    iconName = selectedIcon,
                    colorHex = selectedColor
                )

                // 카테고리 이름 입력
                CategoryNameInput(
                    name = categoryName,
                    onNameChange = { categoryName = it },
                    isValid = isNameValid
                )

                // 아이콘 선택 섹션
                IconSelectionSection(
                    selectedIcon = selectedIcon,
                    onIconSelected = { selectedIcon = it }
                )

                // 색상 선택 섹션
                ColorSelectionSection(
                    selectedColor = selectedColor,
                    onColorSelected = { selectedColor = it }
                )

                // 버튼 영역
                DialogButtons(
                    canConfirm = canConfirm,
                    onDismiss = onDismiss,
                    onConfirm = {
                        onConfirm(categoryName.trim(), selectedIcon, selectedColor)
                    }
                )
            }
        }
    }
}

/**
 * 카테고리 미리보기 컴포넌트
 *
 * 사용자가 선택한 설정이 실제로 어떻게 보일지 실시간으로 보여줍니다.
 * 이렇게 하면 사용자가 원하는 모양을 만들 때까지 조정할 수 있습니다.
 */
@Composable
private fun CategoryPreview(
    name: String,
    iconName: String,
    colorHex: String
) {
    val backgroundColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "미리보기",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.size(80.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
//                Text(
//                    text = getIconEmoji(iconName),
//                    fontSize = 24.sp
//                )
                Text(
                    text = name.take(4), // 최대 4글자만 표시
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * 카테고리 이름 입력 필드
 *
 * 사용자가 카테고리 이름을 입력할 수 있는 텍스트 필드입니다.
 * 유효성 검사 결과에 따라 에러 메시지를 표시합니다.
 */
@Composable
private fun CategoryNameInput(
    name: String,
    onNameChange: (String) -> Unit,
    isValid: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "카테고리 이름",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("예: 취미, 건강, 여행") },
            isError = name.isNotEmpty() && !isValid,
            singleLine = true
        )

        if (name.isNotEmpty() && !isValid) {
            Text(
                text = "카테고리 이름을 입력해주세요",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * 아이콘 선택 섹션
 *
 * 사용자가 카테고리에 사용할 아이콘을 선택할 수 있습니다.
 * 실제 프로젝트에서는 벡터 아이콘을 사용할 수 있지만,
 * 여기서는 이모지를 사용해서 간단하게 구현했습니다.
 */
@Composable
private fun IconSelectionSection(
    selectedIcon: String,
    onIconSelected: (String) -> Unit
) {
    val availableIcons = listOf(
        "restaurant", "coffee", "shopping_cart", "directions_car",
        "local_hospital", "movie", "book", "sports",
        "home", "work", "school", "phone",
        "beauty", "gas_station", "more_horiz"
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "아이콘 선택",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(120.dp)
        ) {
            items(availableIcons) { iconName ->
                IconOption(
                    iconName = iconName,
                    isSelected = selectedIcon == iconName,
                    onClick = { onIconSelected(iconName) }
                )
            }
        }
    }
}

/**
 * 개별 아이콘 선택 옵션
 */
@Composable
private fun IconOption(
    iconName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
//        Text(
//            text = getIconEmoji(iconName),
//            fontSize = 20.sp
//        )
    }
}

/**
 * 색상 선택 섹션
 *
 * 미리 정의된 색상 팔레트에서 카테고리 색상을 선택할 수 있습니다.
 * 각 색상은 가계부 앱에서 사용하기 적합한 색상들로 구성되어 있습니다.
 */
@Composable
private fun ColorSelectionSection(
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
    val availableColors = listOf(
        "#FF5722", "#E91E63", "#9C27B0", "#673AB7",
        "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4",
        "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
        "#FFEB3B", "#FFC107", "#FF9800", "#FF5722",
        "#795548", "#9E9E9E", "#607D8B", "#000000"
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "색상 선택",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(100.dp)
        ) {
            items(availableColors) { colorHex ->
                ColorOption(
                    colorHex = colorHex,
                    isSelected = selectedColor == colorHex,
                    onClick = { onColorSelected(colorHex) }
                )
            }
        }
    }
}

/**
 * 개별 색상 선택 옵션
 */
@Composable
private fun ColorOption(
    colorHex: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.onSurface
    } else {
        Color.Gray.copy(alpha = 0.3f)
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = borderColor,
                shape = CircleShape
            )
            .background(
                color = color,
                shape = CircleShape
            )
            .clickable { onClick() }
    )
}

/**
 * 다이얼로그 버튼들 (취소, 추가)
 */
@Composable
private fun DialogButtons(
    canConfirm: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f)
        ) {
            Text("취소")
        }

        Button(
            onClick = onConfirm,
            enabled = canConfirm,
            modifier = Modifier.weight(1f)
        ) {
            Text("추가")
        }
    }
}

/**
 * 아이콘 이름을 이모지로 변환하는 유틸리티 함수
 * (CategoryGridSelector와 동일한 함수)
 */
