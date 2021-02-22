package com.chirag.todolist

import android.content.DialogInterface
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chirag.todolist.DTO.ToDoItem

import kotlinx.android.synthetic.main.activity_item_list.*
import kotlinx.android.synthetic.main.child_list_view.view.*

class ItemListActivity : AppCompatActivity() {

    lateinit var dbHandler: DBHandler
    var todoId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_list)
        setUp()
    }
    fun setUp(){
        setSupportActionBar(itemListToolbar)
        // To make tool bar button as back arrow
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.title = intent.getStringExtra(INTENT_TODO_NAME)
        itemListToolbar.setTitleTextColor(Color.BLACK)
        todoId = intent.getLongExtra(INTENT_TODO_ID, -1)
        dbHandler = DBHandler(this)
        listRecyclerView.layoutManager = LinearLayoutManager(this)

        listAddButton.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("Add ToDo Item")
            val view = layoutInflater.inflate(R.layout.dialog_dashboard, null)
            val toDoName = view.findViewById<EditText>(R.id.dialogEditText)
            dialog.setView(view)
            dialog.setTitle("Subtask Title")
            dialog.setPositiveButton("Add") { dialogInterface: DialogInterface, _: Int ->
                //Adding items in List
                if (toDoName.text.isNotEmpty()) {
                    val item = ToDoItem()
                    item.itemName = toDoName.text.toString()
                    item.toDoId = todoId
                    item.isCompleted = false
                    dbHandler.addToDoItem(item)
                    //refresh recyclerView
                    refreshList()
                }
            }
            dialog.setNegativeButton("Cancle") { _: DialogInterface, _: Int ->
            }
            dialog.show()
        }
    }

    fun updateItem(item: ToDoItem) {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Update ToDo Item")
        val view = layoutInflater.inflate(R.layout.dialog_dashboard, null)
        val toDoName = view.findViewById<EditText>(R.id.dialogEditText)
        toDoName.setText(item.itemName)
        dialog.setView(view)
        dialog.setPositiveButton("Update") { _: DialogInterface, _: Int ->
            if (toDoName.text.isNotEmpty()) {
                item.itemName = toDoName.text.toString()
                item.toDoId = todoId
                item.isCompleted = false
                dbHandler.updateToDoItem(item)
                refreshList()
            }
        }
        dialog.setNegativeButton("Cancel") { _: DialogInterface, _: Int ->

        }
        dialog.show()
    }

    override fun onResume() {
        refreshList()
        super.onResume()
    }

    fun refreshList() {
        listRecyclerView.adapter = ItemAdapter(this, dbHandler.getToDoItems(todoId))
    }

    class ItemAdapter(val activity: ItemListActivity, val list: MutableList<ToDoItem>) :
        RecyclerView.Adapter<ItemAdapter.ViewHolder>() {
        class ViewHolder(v: View) : RecyclerView.ViewHolder(v)

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ViewHolder {
            val v = LayoutInflater.from(activity).inflate(R.layout.child_list_view, parent, false)
            return ViewHolder(v)
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.itemView.itemCheckBox.text = list[position].itemName

            holder.itemView.editImageView.setOnClickListener {
                activity.updateItem(list[position])
            }

            holder.itemView.deleteImageView.setOnClickListener {
                // create a dialog for ask user about his/her surity about the task to be done.
                val dialog = AlertDialog.Builder(activity)
                dialog.setTitle("Are you sure")
                dialog.setMessage("Do you want to delete this item ?")
                // to delete on click on continue
                dialog.setPositiveButton("continue") { _: DialogInterface, _: Int ->
                    activity.dbHandler.deleteToDoItem(list[position].id)
                    activity.refreshList()
                }
                dialog.setNegativeButton("cancle") { _: DialogInterface, _: Int ->
                }
                dialog.show()
            }
            // To check the status of the checkBox
            holder.itemView.itemCheckBox.isChecked = list[position].isCompleted
            holder.itemView.itemCheckBox.setOnClickListener {
                list[position].isCompleted = !list[position].isCompleted
                activity.dbHandler.updateToDoItem(list[position])
            }
        }
    }

    // On clicking back button we are killing our activity.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else
            super.onOptionsItemSelected(item)
    }
}