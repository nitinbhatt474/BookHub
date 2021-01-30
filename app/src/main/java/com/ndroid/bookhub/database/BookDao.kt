package com.ndroid.bookhub.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BookDao {
    @Insert
    fun insertBook(bookEntity: BookEntity)

    @Delete
    fun deleteBook(bookEntity: BookEntity)

    @Query("SELECT * from books")
    fun getAllBooks(): List<BookEntity>

    //bookId has a : because it tells the compiler that the value will come from function below
    @Query("SELECT * from books WHERE book_id = :bookId")
    fun getBookById(bookId: String): BookEntity
}