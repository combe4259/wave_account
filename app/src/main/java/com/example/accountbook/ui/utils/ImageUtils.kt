package com.example.accountbook.ui.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log

object ImageUtils {
    
    /**
     * 갤러리나 카메라에서 선택한 이미지를 앱 내부 저장소로 복사
     * @param context Context
     * @param sourceUri 원본 이미지 URI
     * @return 복사된 이미지의 파일 경로 (영구 저장)
     */
    fun copyImageToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            // 저장 공간 확인
            val storageDir = File(context.filesDir, "Pictures")
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }
            
            // 사용 가능한 공간 확인 (50MB 이상 필요)
            val availableSpace = storageDir.usableSpace
            if (availableSpace < 50 * 1024 * 1024) {
                android.util.Log.e("ImageUtils", "저장 공간 부족: ${availableSpace / 1024 / 1024}MB")
                return null
            }
            
            // 고유한 파일명 생성
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "IMG_$timeStamp.jpg"
            
            // 대상 파일 생성
            val destFile = File(storageDir, fileName)
            
            // 이미지 크기 확인 및 압축
            val bitmap = android.graphics.BitmapFactory.decodeStream(
                context.contentResolver.openInputStream(sourceUri)
            )
            
            if (bitmap != null) {
                // 이미지가 너무 크면 리사이징 (2048px 최대)
                val maxDimension = 2048
                val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                    val scale = Math.min(
                        maxDimension.toFloat() / bitmap.width,
                        maxDimension.toFloat() / bitmap.height
                    )
                    android.graphics.Bitmap.createScaledBitmap(
                        bitmap,
                        (bitmap.width * scale).toInt(),
                        (bitmap.height * scale).toInt(),
                        true
                    )
                } else {
                    bitmap
                }
                
                // JPEG로 저장 (80% 품질)
                FileOutputStream(destFile).use { outputStream ->
                    scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
                }
                
                // 메모리 해제
                if (scaledBitmap != bitmap) {
                    scaledBitmap.recycle()
                }
                bitmap.recycle()
                
                // 파일 경로 반환
                destFile.absolutePath
            } else {
                null
            }
            
        } catch (e: Exception) {
            android.util.Log.e("ImageUtils", "이미지 저장 실패", e)
            null
        }
    }
    
    /**
     * 카메라 촬영용 임시 파일 생성
     * @param context Context
     * @return FileProvider URI
     */
    fun createTempCameraFile(context: Context): Uri? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            
            val tempFile = File.createTempFile(
                "TEMP_${timeStamp}_",
                ".jpg",
                storageDir
            )
            
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 파일 경로를 URI로 변환 (이미지 표시용)
     * @param filePath 파일 경로
     * @return Uri
     */
    fun getUriFromFilePath(filePath: String): Uri {
        return Uri.fromFile(File(filePath))
    }
    
    /**
     * 이미지 파일 삭제
     * @param filePath 삭제할 파일 경로
     * @return 삭제 성공 여부
     */
    fun deleteImageFile(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 파일이 존재하는지 확인
     * @param filePath 파일 경로
     * @return 존재 여부
     */
    fun isFileExists(filePath: String?): Boolean {
        if (filePath.isNullOrEmpty()) return false
        return File(filePath).exists()
    }
}