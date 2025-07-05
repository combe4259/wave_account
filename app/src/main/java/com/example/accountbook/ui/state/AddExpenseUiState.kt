package com.example.accountbook.ui.state

import android.net.Uri


data class AddExpenseUiState(
    val productName: String = "", //입력 상품명
    val amount: String = "", //지출 금액
    val selectedCategoryId: Long ?= null, //선택된 카테고리 기본값 식비
    val selectedDate: Long = System.currentTimeMillis(), //선택된 날짜 기본값 오늘
    val selectedImageUri: Uri? = null, //선택된 이미지 URI
    val tempCameraUri: Uri? = null, //임시 URI
    val showDatePicker: Boolean = false,  //날짜 선택 다이얼로그가 열려 있는지 여부
    val isExpanded: Boolean = false, //카테고리 드롭다운이 펼쳐져 있는지 여부
    val showImageOptionsDialog: Boolean = false, //카메라 또는 앨범 선택 다이얼로그가 열려 있는지 여부
    val showPermissionDialog: Boolean = false //권한 요청 다이얼로그 여부
) {
    val isValid: Boolean //상품명과 금액이 채워져있어야함
        get() = productName.isNotBlank() && amount.isNotBlank()

    val amountAsDouble: Double
        get() = amount.toDoubleOrNull() ?: 0.0
}
