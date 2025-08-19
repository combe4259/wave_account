package com.example.accountbook.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.accountbook.local.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideExpenseDatabase(
        @ApplicationContext context: Context
    ): ExpenseDatabase {
        return Room.databaseBuilder(
            context,
            ExpenseDatabase::class.java,
            "expense_database"
        )
        .addMigrations(MIGRATION_4_5)
        .build()
    }
    
    @Provides
    fun provideExpenseDao(database: ExpenseDatabase): ExpenseDao {
        return database.expenseDao()
    }
    
    @Provides
    fun provideIncomeDao(database: ExpenseDatabase): IncomeDao {
        return database.incomeDao()
    }
    
    @Provides
    fun provideExpenseCategoryDao(database: ExpenseDatabase): ExpenseCategoryDao {
        return database.expenseCategoryDao()
    }
    
    @Provides
    fun provideIncomeCategoryDao(database: ExpenseDatabase): IncomeCategoryDao {
        return database.incomeCategoryDao()
    }
    
    // 마이그레이션 전략 추가
    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 필요시 마이그레이션 로직 추가
            // 현재는 스키마 변경 없음
        }
    }
}