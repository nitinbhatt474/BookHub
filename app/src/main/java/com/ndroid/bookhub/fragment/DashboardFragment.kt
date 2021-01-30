package com.ndroid.bookhub.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.ndroid.bookhub.R
import com.ndroid.bookhub.adapter.DashboardRecyclerAdapter
import com.ndroid.bookhub.data.Book
import com.ndroid.bookhub.util.ConnectionManager
import org.json.JSONException
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class DashboardFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    lateinit var recyclerDashboard: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: DashboardRecyclerAdapter

    /*
    Static list of data but remove bookID from Book before using this
        val bookInfoList = arrayListOf<Book>(
        Book("101""P.S. I love You", "Cecelia Ahern", "Rs. 299", "4.5", R.drawable.ps_ily),
        Book("The Great Gatsby", "F. Scott Fitzgerald", "Rs. 399", "4.1", R.drawable.great_gatsby),
        Book("Anna Karenina", "Leo Tolstoy", "Rs. 199", "4.3", R.drawable.anna_kare),
        Book("Madame Bovary", "Gustave Flaubert", "Rs. 500", "4.0", R.drawable.madame),
        Book("War and Peace", "Leo Tolstoy", "Rs. 249", "4.8", R.drawable.war_and_peace),
        Book("Lolita", "Vladimir Nabokov", "Rs. 349", "3.9", R.drawable.lolita),
        Book("Middlemarch", "George Eliot", "Rs. 599", "4.2", R.drawable.middlemarch),
        Book(
            "The Adventures of Huckleberry Finn",
            "Mark Twain",
            "Rs. 699",
            "4.5",
            R.drawable.adventures_finn
        ),
        Book("Moby-Dick", "Herman Melville", "Rs. 499", "4.5", R.drawable.moby_dick),
        Book("The Lord of the Rings", "J.R.R Tolkien", "Rs. 749", "5.0", R.drawable.lord_of_rings)
    )*/
    val bookInfoList = arrayListOf<Book>()
    var ratingComparator = Comparator<Book>{book1, book2 ->
        if(book1.bookRating.compareTo(book2.bookRating, true) == 0){
            book1.bookName.compareTo(book2.bookName, true)
        }
        else{
            book1.bookRating.compareTo(book2.bookRating, true)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //inflates/builds the layout
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val progressLayout: RelativeLayout = view.findViewById(R.id.progressLayout)
        progressLayout.visibility = View.VISIBLE

        //displays the sort menu option and is only used in case of a fragment not an activity
        setHasOptionsMenu(true)

        //initializing recycler view and layout manager
        recyclerDashboard = view.findViewById(R.id.recyclerDashboard)
        layoutManager = LinearLayoutManager(activity)

        val dialog = AlertDialog.Builder(activity as Context)
        if (ConnectionManager().checkConnectivity(activity as Context)) {
            val queue = Volley.newRequestQueue(activity as Context)
            val url = "http://13.235.250.119/v1/book/fetch_books/"

            val jsonObjectRequest =
                object : JsonObjectRequest(Request.Method.GET, url, null, Response.Listener {
                    try {
                        val success = it.getBoolean("success")
                        progressLayout.visibility = View.GONE

                        if (success) {
                            val data = it.getJSONArray("data")
                            for (i in 0 until data.length()) {
                                val bookJsonObject = data.getJSONObject(i)
                                val bookObject = Book(
                                    bookJsonObject.getString("book_id"),
                                    bookJsonObject.getString("name"),
                                    bookJsonObject.getString("author"),
                                    bookJsonObject.getString("rating"),
                                    bookJsonObject.getString("price"),
                                    bookJsonObject.getString("image")
                                )
                                bookInfoList.add(bookObject)
                            }
                            recyclerAdapter =
                                DashboardRecyclerAdapter(activity as Context, bookInfoList)
                            recyclerDashboard.adapter = recyclerAdapter
                            recyclerDashboard.layoutManager = layoutManager

                            /* adds a divider between elements
                            recyclerDashboard.addItemDecoration(
                                DividerItemDecoration(
                                    recyclerDashboard.context,
                                    (layoutManager as LinearLayoutManager).orientation
                                )
                            )*/
                        } else {
                            Toast.makeText(activity, "Some Error Occurred!", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } catch (e: JSONException) {
                        Toast.makeText(activity, "Some exception occurred", Toast.LENGTH_SHORT)
                            .show()
                    }

                }, Response.ErrorListener {
                    //Response Error Listener
                    if(activity != null) {
                        Toast.makeText(activity, "Volley error Occurred!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>() //HashMap is derived form MutableMap
                        headers["Content-type"] = "application/json"
                        headers["token"] = "a97f0b4bdd6fce"   //token for the API given by web-dev
                        return headers
                    }
                }
            queue.add(jsonObjectRequest)

        } else {
            dialog.setTitle("Error")
            dialog.setMessage("No Internet Available")
            dialog.setPositiveButton("Open Settings") { text, listener ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)  //implicit intent
                startActivity(settingsIntent)
                activity?.finish()  //so that activity can restart and fetch the data.
            }
            dialog.setNegativeButton("Cancel") { text, listener ->
                //finishes the activity and closes the app
                ActivityCompat.finishAffinity(activity as Activity)
            }
            dialog.create()
            dialog.show()
        }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_dashboard, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item?.itemId
        if(id == R.id.action_sort){
            Collections.sort(bookInfoList, ratingComparator)
            bookInfoList.reverse()
        }
        recyclerAdapter.notifyDataSetChanged()

        return super.onOptionsItemSelected(item)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DashboardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
