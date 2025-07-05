package com.example.accountbook.ui.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//이미지 처리를 위한 핸들러
class ImageHandler(
    private val context: Context,
    private val galleryLauncher: (String) -> Unit,
    private val cameraLauncher: (Uri) -> Unit,
    private val permissionLauncher: (String) -> Unit,
    private val onImageSelected: (Uri?) -> Unit,
    private val onShowPermissionDialog: () -> Unit,
    private val onSetTempUri: (Uri?) -> Unit
) {

    private var tempCameraUri: Uri? = null

    //갤러리 열기
    fun launchGallery() {
        galleryLauncher("image/*")
    }

    //카메라
    fun launchCamera() {
        createImageUri { uri ->
            tempCameraUri = uri
            onSetTempUri(uri)
            cameraLauncher(uri)
        }
    }

    fun requestCameraPermission() {
        //권한 있을 경우
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {//권한 없을 경우
            permissionLauncher(Manifest.permission.CAMERA)
        }
    }

    private fun createImageUri(onUriCreated: (Uri) -> Unit) {
        try {
            val photoFile = File(
                context.getExternalFilesDir("Pictures"),
                "expense_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg"
            )
            photoFile.parentFile?.mkdirs()

            val photoUri = FileProvider.getUriForFile(
                context,
                "com.example.accountbook.fileprovider",
                photoFile
            )
            onUriCreated(photoUri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}



@Composable
fun rememberImageHandler(
    onImageSelected: (Uri?) -> Unit,
    onShowPermissionDialog: () -> Unit,
    onSetTempUri: (Uri?) -> Unit
): ImageHandler {
    val context = LocalContext.current

    // 갤러리 런처
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> onImageSelected(uri) }

    // 카메라 런처
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // tempCameraUri는 ImageHandler에서 관리
        } else {
            onSetTempUri(null)
        }
    }

    // 권한 런처
    val permissionLauncher = rememberLauncherForActivityResult(
contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 권한이 허용되면 카메라 실행은 ImageHandler에서 처리
        } else {
            onShowPermissionDialog()
        }
    }

    return remember {
        ImageHandler(
            context = context,
            galleryLauncher = { mimeType -> galleryLauncher.launch(mimeType) },
            cameraLauncher = { uri -> cameraLauncher.launch(uri) },
            permissionLauncher = { permission -> permissionLauncher.launch(permission) },
            onImageSelected = onImageSelected,
            onShowPermissionDialog = onShowPermissionDialog,
            onSetTempUri = onSetTempUri
        )
    }
}