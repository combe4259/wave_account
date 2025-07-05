//package com.example.accountbook.view
//
//import android.app.Application
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.viewModelScope
//import com.example.accountbook.local.ExpenseDatabase
//import com.example.accountbook.model.Category
//import com.example.accountbook.repository.CategoryRepository
//import kotlinx.coroutines.launch
//
//class CategoryViewModel {
//    class CategoryViewModel(application: Application) : AndroidViewModel(application) {
//        private val repository: CategoryRepository
//        val allCategories: LiveData<List<Category>>
//
//        init {
//            val categoryDao = ExpenseDatabase.getDatabase(application).categoryDao()
//            repository = CategoryRepository(categoryDao)
//            allCategories = repository.allCategories
//
//            // 앱 시작 시 기본 카테고리 초기화
//            viewModelScope.launch {
//                repository.initializeDefaultCategories()
//            }
//        }
//
//        fun insertCategory(category: Category) = viewModelScope.launch {
//            repository.insertCategory(category)
//        }
//
//        fun deleteCategory(category: Category) = viewModelScope.launch {
//            repository.deleteCategory(category)
//        }
//    }
//}
//
//object IconMapper {
//    private val iconMap = mapOf(
//        "Restaurant" to Icons.Default.Restaurant,
//        "DirectionsCar" to Icons.Default.DirectionsCar,
//        "ShoppingCart" to Icons.Default.ShoppingCart,
//        "Movie" to Icons.Default.Movie,
//        "LocalHospital" to Icons.Default.LocalHospital,
//        "MoreHoriz" to Icons.Default.MoreHoriz,
//        "Home" to Icons.Default.Home,
//        "Work" to Icons.Default.Work,
//        "School" to Icons.Default.School,
//        "FitnessCenter" to Icons.Default.FitnessCenter,
//        "Pets" to Icons.Default.Pets,
//        "Coffee" to Icons.Default.LocalCafe,
//        "Gas" to Icons.Default.LocalGasStation,
//        "Phone" to Icons.Default.Phone,
//        "Book" to Icons.Default.Book,
//        "Music" to Icons.Default.MusicNote,
//        "Custom" to Icons.Default.Category // 사용자 정의용 기본 아이콘
//    )
//
//    fun <ImageVector> getIcon(iconName: String): androidx.compose.ui.graphics.vector.ImageVector {
//        return iconMap[iconName] ?: Icons.Default.Category
//    }
//
//    fun getAllIcons(): List<Pair<String, ImageVector>> {
//        return iconMap.toList()
//    }
//}