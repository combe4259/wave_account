package com.example.accountbook.data.mapper

import com.example.accountbook.domain.model.Category
import com.example.accountbook.domain.model.CategoryType
import com.example.accountbook.domain.model.Transaction
import com.example.accountbook.dto.ExpenseWithCategory
import com.example.accountbook.dto.IncomeWithCategory
import com.example.accountbook.model.Expense as ExpenseEntity
import com.example.accountbook.model.Income as IncomeEntity
import com.example.accountbook.model.ExpenseCategory as ExpenseCategoryEntity
import com.example.accountbook.model.IncomeCategory as IncomeCategoryEntity

// Entity to Domain
fun ExpenseEntity.toDomain(): Transaction.Expense {
    return Transaction.Expense(
        id = id,
        amount = amount,
        categoryId = categoryId,
        date = date,
        description = productName,
        photoUri = photoUri
    )
}

fun IncomeEntity.toDomain(): Transaction.Income {
    return Transaction.Income(
        id = id,
        amount = amount,
        categoryId = categoryId,
        date = date,
        description = description
    )
}

// DTO (WithCategory) to Domain
fun ExpenseWithCategory.toDomain(): Transaction.Expense {
    return Transaction.Expense(
        id = id,
        amount = amount,
        categoryId = categoryId,
        date = date,
        description = productName,
        photoUri = photoUri
    )
}

fun IncomeWithCategory.toDomain(): Transaction.Income {
    return Transaction.Income(
        id = id,
        amount = amount,
        categoryId = categoryId,
        date = date,
        description = description
    )
}

fun ExpenseCategoryEntity.toDomain(): Category {
    return Category(
        id = id,
        name = name,
        iconName = iconName,
        type = CategoryType.EXPENSE
    )
}

fun IncomeCategoryEntity.toDomain(): Category {
    return Category(
        id = id,
        name = name,
        iconName = iconName,
        type = CategoryType.INCOME
    )
}

// Domain to Entity
fun Transaction.Expense.toEntity(): ExpenseEntity {
    return ExpenseEntity(
        id = id,
        productName = description,
        amount = amount,
        categoryId = categoryId,
        date = date,
        photoUri = photoUri
    )
}

fun Transaction.Income.toEntity(): IncomeEntity {
    return IncomeEntity(
        id = id,
        description = description,
        amount = amount,
        categoryId = categoryId,
        date = date
    )
}

fun Category.toExpenseEntity(): ExpenseCategoryEntity {
    require(type == CategoryType.EXPENSE) { "Category type must be EXPENSE" }
    return ExpenseCategoryEntity(
        id = id,
        name = name,
        iconName = iconName
    )
}

fun Category.toIncomeEntity(): IncomeCategoryEntity {
    require(type == CategoryType.INCOME) { "Category type must be INCOME" }
    return IncomeCategoryEntity(
        id = id,
        name = name,
        iconName = iconName
    )
}