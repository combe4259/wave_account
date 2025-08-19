package com.example.accountbook.data.util

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import androidx.lifecycle.asFlow

/**
 * LiveData를 Flow로 변환하는 확장 함수
 */
fun <T> LiveData<T>.asFlow(): Flow<T> = this.asFlow()