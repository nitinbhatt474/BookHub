package com.ndroid.bookhub.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.ndroid.bookhub.R
import com.ndroid.bookhub.database.BookDatabase
import com.ndroid.bookhub.database.BookEntity
import com.ndroid.bookhub.util.ConnectionManager
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.lang.Exception

class DescriptionActivity : AppCompatActivity() {

    lateinit var txtBookName: TextView
    lateinit var txtAuthorName: TextView
    lateinit var txtBookPrice: TextView
    lateinit var txtBookRating: TextView
    lateinit var imgBookImage: ImageView
    lateinit var txtBookDesc: TextView
    lateinit var btnToFav: Button
    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_description)
        toolbar?.title = "Book Details"

        txtBookName = findViewById(R.id.txtBookName)
        txtAuthorName = findViewById(R.id.txtAuthorName)
        txtBookPrice = findViewById(R.id.txtBookPrice)
        txtBookRating = findViewById(R.id.txtBookRating)
        imgBookImage = findViewById(R.id.imgBookImage)
        txtBookDesc = findViewById(R.id.txtBookDesc)
        btnToFav = findViewById(R.id.btnToFav)
        progressLayout = findViewById(R.id.progressLayout)
        progressBar = findViewById(R.id.progress_bar)
        var bookId: String? = "100"

        progressLayout.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE

        if (intent != null) {
            bookId = intent.getStringExtra("bookId")
        } else {
            Toast.makeText(this@DescriptionActivity, "Some Error Occurred!", Toast.LENGTH_SHORT)
                .show()
        }
        if (bookId == "100") {
            Toast.makeText(
                this@DescriptionActivity,
                "Some Unexpected Error Occurred!",
                Toast.LENGTH_SHORT
            )
                .show()
        }

        if (ConnectionManager().checkConnectivity(this@DescriptionActivity)) {
            val queue = Volley.newRequestQueue(this@DescriptionActivity)
            val url = "http://13.235.250.119/v1/book/get_book/"

            val jsonParams = JSONObject()
            jsonParams.put("book_id", bookId)   //refer to name from JSON response

            val jsonObjectRequest = object : JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonParams,
                Response.Listener {
                    try {
                        val success = it.getBoolean("success")
                        if (success) {
                            val bookJsonObject = it.getJSONObject("book_data")
                            progressLayout.visibility = View.GONE

                            val imgUrl = bookJsonObject.getString("image")
                            Picasso.get().load(imgUrl)
                                .error(R.drawable.default_book_cover).into(imgBookImage)
                            txtBookName.text = bookJsonObject.getString("name")
                            txtAuthorName.text = bookJsonObject.getString("author")
                            txtBookPrice.text = bookJsonObject.getString("price")
                            txtBookRating.text = bookJsonObject.getString("rating")
                            txtBookDesc.text = bookJsonObject.getString("description")

                            //bookEntity to perform database on
                            val bookEntity = BookEntity(
                                bookId?.toInt() as Int,
                                txtBookName.text.toString(),
                                txtAuthorName.text.toString(),
                                txtBookPrice.text.toString(),
                                txtBookRating.text.toString(),
                                txtBookDesc.text.toString(),
                                imgUrl
                            )

                            //will create object for AsyncTask
                            val checkFav = DBAsyncTask(applicationContext, bookEntity, 1).execute()
                            //gets the return value of the the above object
                            val isFav = checkFav.get()

                            if (isFav) {
                                btnToFav.text = "Remove from Favorites"
                                val favColor = ContextCompat.getColor(
                                    applicationContext,
                                    R.color.colorFavorite
                                )
                                btnToFav.setBackgroundColor(favColor)
                            }

                            btnToFav.setOnClickListener {
                                if (!DBAsyncTask(applicationContext, bookEntity, 1).execute()
                                        .get()
                                ) {
                                    val async =
                                        DBAsyncTask(applicationContext, bookEntity, 2).execute()
                                    val result = async.get()

                                    if (result) {
                                        Toast.makeText(
                                            this@DescriptionActivity,
                                            "Book added to Favorites",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        btnToFav.text = "Remove from Favorites"
                                        val favColor = ContextCompat.getColor(
                                            applicationContext,
                                            R.color.colorFavorite
                                        )
                                        btnToFav.setBackgroundColor(favColor)
                                    } else {
                                        Toast.makeText(
                                            this@DescriptionActivity,
                                            "Some Error Occurred!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    val async =
                                        DBAsyncTask(applicationContext, bookEntity, 3).execute()
                                    val result = async.get()

                                    if (result) {
                                        Toast.makeText(
                                            this@DescriptionActivity,
                                            "Book removed from favorites",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        btnToFav.text = "Add to favorites"
                                        val favColor = ContextCompat.getColor(
                                            applicationContext,
                                            R.color.colorButton
                                        )
                                        btnToFav.setBackgroundColor(favColor)
                                    } else {
                                        Toast.makeText(
                                            this@DescriptionActivity,
                                            "Some Error Occurred",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }

                        } else {
                            Toast.makeText(
                                this@DescriptionActivity,
                                "Some Error Occurred",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@DescriptionActivity,
                            "$e Exception Occurred",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                Response.ErrorListener {
                    Toast.makeText(
                        this@DescriptionActivity,
                        "$it Volley Error Occurred",
                        Toast.LENGTH_SHORT
                    ).show()
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "a97f0b4bdd6fce"
                    return headers
                }
            }
            queue.add(jsonObjectRequest)
        } else {
            val dialog = AlertDialog.Builder(this@DescriptionActivity)
            dialog.setTitle("Error")
            dialog.setMessage("No Internet Available")
            dialog.setPositiveButton("Open Settings") { text, listener ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)  //implicit intent
                startActivity(settingsIntent)
                finish()  //so that activity can restart and fetch the data.
            }
            dialog.setNegativeButton("Cancel") { text, listener ->
                //finishes the activity and closes the app
                ActivityCompat.finishAffinity(this@DescriptionActivity)
            }
            dialog.create()
            dialog.show()
        }
    }

    class DBAsyncTask(val context: Context, val bookEntity: BookEntity, val mode: Int) :
        AsyncTask<Void, Void, Boolean>() {
        /*
            This class needs to perform three things:
            1. Check if a book record is present in the database.
            2. Add a book record to the database.
            3. Delete a book record from the database.
        */
        val db = Room.databaseBuilder(context, BookDatabase::class.java, "books-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {
            when (mode) {
                1 -> {
                    //check if book is in the database
                    val book: BookEntity? = db.bookDao().getBookById(bookEntity.book_id.toString())
                    db.close()
                    return book != null
                }
                2 -> {
                    //add book record to the database as favorite
                    db.bookDao().insertBook(bookEntity)
                    db.close()
                    return true
                }
                3 -> {
                    //delete book record from the database as favorite
                    db.bookDao().deleteBook(bookEntity)
                    db.close()
                    return true
                }
            }
            return false
        }
    }
}