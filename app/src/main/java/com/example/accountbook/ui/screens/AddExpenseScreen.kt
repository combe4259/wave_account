package com.example.accountbook.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult  // 추가!
import androidx.activity.result.contract.ActivityResultContracts   // 추가!
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.accountbook.ui.components.CategoryGridSelector
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.accountbook.model.Expense
import com.example.accountbook.model.Category
import com.example.accountbook.view.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*


import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons

import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.example.accountbook.ui.components.images.ImageOptionsDialog
import com.example.accountbook.ui.state.AddExpenseUiState
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.accountbook.ui.components.AddCategoryDialog
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    initialDate: Long? = null
) {
    val context = LocalContext.current

    // 상태 관리
    var uiState by remember {
        mutableStateOf(
            AddExpenseUiState(
                selectedDate = initialDate ?: System.currentTimeMillis()
            )
        )
    }
    //카테고리 다이얼로그
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { name, iconName, colorHex ->
                // 새 카테고리 객체 생성
                val newCategory = Category(
                    name = name,
                    iconName = iconName,
                    colorHex = colorHex
                )
                // 카테고리 추가
                viewModel.insertCategory(newCategory)
                // 다이얼로그 닫기
                showAddCategoryDialog = false
            }
        )
    }


    //임시 파일 생성
    fun createTempImageFile(context: Context): Uri? {
        return try {
            val photoFile = File.createTempFile(
                "photo_${System.currentTimeMillis()}",
                ".jpg",
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            )

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    // 이미지 선택을 위한 Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            uiState = uiState.copy(selectedImageUri = uri)
        }
    }
    //카메라 Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            // 촬영 성공시 임시 URI를 실제 URI로 설정
            uiState = uiState.copy(
                selectedImageUri = uiState.tempCameraUri,
                tempCameraUri = null
            )
        } else {
            // 촬영 실패시 임시 URI 제거
            uiState = uiState.copy(tempCameraUri = null)
        }
    }

    //권한 Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 권한이 허용되면 카메라 실행
            createTempImageFile(context)?.let { uri ->
                uiState = uiState.copy(tempCameraUri = uri)
                cameraLauncher.launch(uri)
            }
        } else {
            // 권한이 거부되면 다이얼로그 표시
            uiState = uiState.copy(showPermissionDialog = true)
        }
    }
    if (uiState.showPermissionDialog) {
        PermissionDialog(
            onDismiss = { uiState = uiState.copy(showPermissionDialog = false) }
        )
    }


//카메라 실행
        fun handleCameraClick() {
            when (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)) {
                PackageManager.PERMISSION_GRANTED -> {
                    createTempImageFile(context)?.let { uri ->
                        uiState = uiState.copy(tempCameraUri = uri)
                        cameraLauncher.launch(uri)
                    }
                }
                else -> {
                    // 권한 요청
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }


        // 카테고리 데이터 가져오기
    val categories by viewModel.allCategories.observeAsState(emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (initialDate != null) {
                            "${SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA).format(Date(initialDate))} 지출 추가"
                        } else {
                            "지출 추가"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 상품명과 금액 입력
            item {
                MainInfoCard(
                    productName = uiState.productName,
                    amount = uiState.amount,
                    onProductNameChange = { uiState = uiState.copy(productName = it) },
                    onAmountChange = { uiState = uiState.copy(amount = it) }
                )
            }

            // 카테고리 선택
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                    ) {
                        CategoryGridSelector(
                            categories = categories,
                            selectedCategoryId = uiState.selectedCategoryId,
                            onCategorySelected = { categoryId ->
                                uiState = uiState.copy(selectedCategoryId = categoryId)
                            },
                            onAddNewCategory = {
                                showAddCategoryDialog = true
                            }
                        )
                    }
                }
            }

// 날짜 선택 (별도 item)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clickable { uiState = uiState.copy(showDatePicker = true) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "날짜",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = SimpleDateFormat("MM/dd", Locale.KOREA).format(Date(uiState.selectedDate)),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = SimpleDateFormat("yyyy년", Locale.KOREA).format(Date(uiState.selectedDate)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 이미지 섹션
            item {
                ImageSectionCard(
                    selectedImageUri = uiState.selectedImageUri,
                    onImageClick = { uiState = uiState.copy(showImageOptionsDialog = true) },
                    onImageRemove = { uiState = uiState.copy(selectedImageUri = null) }
                )
            }

            // 저장 버튼
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Button(
                    onClick = {
                        if (uiState.isValid) {
                            val expense = Expense(
                                productName = uiState.productName,
                                amount = uiState.amountAsDouble,
                                categoryId = uiState.selectedCategoryId,
                                date = uiState.selectedDate,
                                photoUri = uiState.selectedImageUri?.toString()
                            )
                            viewModel.insertExpense(expense)
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.isValid
                ) {
                    Text("지출 추가하기")
                }
            }
        }
    }

    // 다이얼로그들
    if (uiState.showDatePicker) {
        DatePickerDialog(
            selectedDate = uiState.selectedDate,
            onDateSelected = { date ->
                uiState = uiState.copy(
                    selectedDate = date ?: uiState.selectedDate,
                    showDatePicker = false
                )
            },
            onDismiss = { uiState = uiState.copy(showDatePicker = false) }
        )
    }

    if (uiState.showImageOptionsDialog) {
        ImageOptionsDialog(
            onCameraClick = {
                uiState = uiState.copy(showImageOptionsDialog = false)
                handleCameraClick()
            },
            onGalleryClick = {
                uiState = uiState.copy(showImageOptionsDialog = false)
                galleryLauncher.launch("image/*")
            },
            onDismiss = { uiState = uiState.copy(showImageOptionsDialog = false) }
        )
    }
}

@Composable
private fun MainInfoCard(
    productName: String,
    amount: String,
    onProductNameChange: (String) -> Unit,
    onAmountChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChange,
                label = { Text("금액") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                suffix = { Text("원") },
                placeholder = { Text("0") }
            )

            OutlinedTextField(
                value = productName,
                onValueChange = onProductNameChange,
                label = { Text("상품명") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("예: 점심식사, 커피") }
            )
        }
    }
}
//Todo
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun CategorySelectorCompact(
//    categories: List<Category>,
//    selectedCategoryId: Long?,
//    isExpanded: Boolean,
//    onCategorySelected: (Long?) -> Unit,
//    onExpandedChange: (Boolean) -> Unit
//) {
//    val selectedCategory = categories.find { it.id == selectedCategoryId }
//
//    ExposedDropdownMenuBox(
//        expanded = isExpanded,
//        onExpandedChange = onExpandedChange
//    ) {
//        Text(
//            text = selectedCategory?.name ?: "선택하세요",
//            style = MaterialTheme.typography.titleMedium,
//            modifier = Modifier
//                .fillMaxWidth()
//                .menuAnchor()
//                .clickable { onExpandedChange(true) }
//        )
//
//        ExposedDropdownMenu(
//            expanded = isExpanded,
//            onDismissRequest = { onExpandedChange(false) }
//        ) {
//            DropdownMenuItem(
//                text = { Text("카테고리 없음") },
//                onClick = { onCategorySelected(null) }
//            )
//
//            categories.forEach { category ->
//                DropdownMenuItem(
//                    text = { Text(category.name) },
//                    onClick = { onCategorySelected(category.id) }
//                )
//            }
//        }
//    }
//}


@Composable
private fun ImageSectionCard(
    selectedImageUri: Uri?,
    onImageClick: () -> Unit,
    onImageRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        if (selectedImageUri != null) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "선택된 사진",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )

                IconButton(
                    onClick = onImageRemove,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "이미지 제거", tint = Color.White)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clickable { onImageClick() }
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "사진 추가", modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("사진 추가")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    selectedDate: Long,
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onDateSelected(datePickerState.selectedDateMillis) }) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        },
        text = {
            DatePicker(state = datePickerState)
        }
    )
}

@Composable
private fun ImageOptionsDialog(
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("사진 선택") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        },
        text = {
            Column {
                TextButton(
                    onClick = onCameraClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("카메라로 촬영")
                }
                TextButton(
                    onClick = onGalleryClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("갤러리에서 선택")
                }
            }
        }
    )
}

@Composable
private fun PermissionDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("카메라 권한 필요") },
        text = {
            Text("사진을 촬영하려면 카메라 권한이 필요합니다. 설정에서 권한을 허용해주세요.")
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("확인")
            }
        }
    )
}