package com.chirag.todolist

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chirag.todolist.DTO.ToDo
import kotlinx.android.synthetic.main.activity_dash_board.*
import kotlinx.android.synthetic.main.list_view.view.*
import java.text.SimpleDateFormat
import java.util.*

class DashBoardActivity : AppCompatActivity() {

    lateinit var dbHandler: DBHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board)
        clickOperations()
        dashBoardShimmerLayout.startShimmerAnimation()
        Handler().postDelayed({
           dashBoardShimmerLayout.stopShimmerAnimation()
            dashBoardShimmerLayout.visibility = View.GONE
        }, 2000)

    }

    fun clickOperations() {
        inviteFriendTextview.setOnClickListener {
            try {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My application name")
                var shareMessage = "\nLet me recommend you this application\n\n"
                shareMessage = """
                ${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}               
                """.trimIndent()
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                startActivity(Intent.createChooser(shareIntent, "choose one"))
            } catch (e: Exception) {
                //e.toString();
            }
        }
        //Setting the action bar
        setSupportActionBar(dashboardToolbar)
        setTitle("Tasks List")
        dashboardToolbar.setTitleTextColor(Color.BLACK)

        //Assigning the dbhandler
        dbHandler = DBHandler(this)
        // RecyclerView Operations
        mainRecyclerView.layoutManager = LinearLayoutManager(this)
        // Dialog operations
        addButton.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.dialog_dashboard, null)
            val toDoName = view.findViewById<EditText>(R.id.dialogEditText)
            dialog.setView(view)
            dialog.setTitle("Task Title")
            dialog.setPositiveButton("Add")
            { dialogInterface: DialogInterface, _: Int ->
                //Adding items in List
                if (toDoName.text.isNotEmpty()) {
                    val todo = ToDo()
                    todo.name = toDoName.text.toString()
                    // Date and Time
                    // Also sotring Date and time for each task
//                    val calender = Calendar.getInstance()
//                    val currentDate = DateFormat.getInstance().format(calender.time)
                    val sdf = SimpleDateFormat("dd/MMM/yyyy")
                    val date: String = sdf.format(Date())
                    todo.createdAt = date
                    dbHandler.addToDo(todo)
                    // Search Opeartion
                    // To add list in nameList
                    //refresh recyclerView
                    refreshList()
                }
            }
            dialog.setNegativeButton("Cancle") { _: DialogInterface, _: Int ->

            }
            dialog.show()
        }

    }

    fun updateToDo(toDo: ToDo) {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Update ToDo")
        val view = layoutInflater.inflate(R.layout.dialog_dashboard, null)
        val toDoName = view.findViewById<EditText>(R.id.dialogEditText)
        toDoName.setText(toDo.name)
        dialog.setView(view)
        dialog.setPositiveButton("Update") { _: DialogInterface, _: Int ->
            if (toDoName.text.isNotEmpty()) {
                toDo.name = toDoName.text.toString()
//                val calender = Calendar.getInstance()
//                val currentDate = DateFormat.getInstance().format(calender.time)
//                toDo.createdAt = currentDate
                dbHandler.updateToDo(toDo)
                refreshList()
            }
        }
        dialog.setNegativeButton("Cancel") { _: DialogInterface, _: Int ->
        }
        dialog.show()
    }

    // For search operations....

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        val searchItem = menu.findItem(R.menu.search_menu)
        if (searchItem != null) {
            val searchView = searchItem.actionView as SearchView
            searchView.setQueryHint("Search Hint")
            searchView.setBackgroundColor(Color.BLACK)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(p0: String?): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    return true
                }
            })
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onResume() {
        refreshList()
        super.onResume()
    }

    private fun refreshList() {
        mainRecyclerView.adapter = DashboardAdapter(this, dbHandler.getToDos())
    }

    class DashboardAdapter(val activity: DashBoardActivity, val list: MutableList<ToDo>) :
        RecyclerView.Adapter<DashboardAdapter.ViewHolder>() {
        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(activity).inflate(R.layout.list_view, parent, false)
            return ViewHolder(v)
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.itemView.dateTextView.text = list[position].createdAt
            holder.itemView.todoListTextView.text = list[position].name
            // ClickListener on menu icon
            holder.itemView.listMenu.setOnClickListener {
                val popup = PopupMenu(activity, holder.itemView.listMenu)
                popup.inflate(R.menu.dashboard_child)
                // ClickListener on menu items
                popup.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.menu_edit -> {
                            activity.updateToDo(list[position])
                        }
                        R.id.menu_delete -> {
                            val dialog = AlertDialog.Builder(activity)
                            dialog.setTitle("Are you sure")
                            dialog.setMessage("Do you want to delete this Task ?")
                            // to delete on click on continue
                            dialog.setPositiveButton("continue") { _: DialogInterface, _: Int ->
                                activity.dbHandler.deleteToDo(list[position].id)
                                //To refresh the list so our list can show us remaining elements.
                                activity.refreshList()
                            }
                            dialog.setNegativeButton("cancle") { _: DialogInterface, _: Int ->
                            }
                            dialog.show()
                        }
                        R.id.menu_mark_as_completed -> {
                            activity.dbHandler.updateToDoItemCompletedStatus(
                                list[position].id,
                                true
                            )
                            Toast.makeText(
                                activity,
                                "All Subtasks are completed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        R.id.menu_reset -> {
                            activity.dbHandler.updateToDoItemCompletedStatus(
                                list[position].id,
                                false
                            )
                            Toast.makeText(activity, "All Subtasks are reset", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                    true
                }
                popup.show()
            }

            holder.itemView.todoListCardView.setLongClickable(true)
            holder.itemView.todoListCardView.setOnClickListener {
                val intent = Intent(activity, ItemListActivity::class.java)
                //ClickListener on RecyclerView According to the Position
                // sends id and name to next activity or fragment where the extras will be called .
                intent.putExtra(INTENT_TODO_ID, list[position].id)
                intent.putExtra(INTENT_TODO_NAME, list[position].name)
                activity.startActivity(intent)
            }
        }
    }
}