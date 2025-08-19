package com.example.accountbook.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.accountbook.model.Expense

import com.example.accountbook.model.ExpenseCategory
import com.example.accountbook.model.Income
import com.example.accountbook.model.IncomeCategory

@Database(
    entities = [Expense::class, ExpenseCategory::class, Income::class, IncomeCategory::class], //DB에 포함될 테이블
    version = 5,  // 버전 업데이트
    exportSchema = false
)
//데이터베이스의 진입점
//SQLite 기반 로컬 데이터베이스의 진입점
abstract class ExpenseDatabase : RoomDatabase() {

    //DB 접근은 Dao를 연결하여 접근
    abstract fun expenseDao(): ExpenseDao
    abstract fun incomeDao(): IncomeDao
    abstract fun expenseCategoryDao(): ExpenseCategoryDao
    abstract fun incomeCategoryDao(): IncomeCategoryDao

    //singleton패턴
    companion object {
        //thread간 최신값을 항상 보도록
        @Volatile
        private var INSTANCE: ExpenseDatabase? = null

        fun getDatabase(context: Context): ExpenseDatabase {
            return INSTANCE ?: synchronized(this) { //DB lock으로 동시에 DB 생성 방지
                val instance = Room.databaseBuilder(
                                context.applicationContext,
                                ExpenseDatabase::class.java,
                                "expense_database",
                            ).fallbackToDestructiveMigration(true).//DB 버전 변경할때만 초기화되는
                build()
                INSTANCE = instance
                instance
            }
        }
    }
}