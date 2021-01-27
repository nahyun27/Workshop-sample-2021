package page.chungjungsoo.to_dosample.todo

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import page.chungjungsoo.to_dosample.R
import java.util.*


class TodoListViewAdapter (context: Context, var resource: Int, var items: MutableList<Todo> ) : ArrayAdapter<Todo>(context, resource, items){
    private lateinit var db: TodoDatabaseHelper

    override fun getView(position: Int, convertView: View?, p2: ViewGroup): View {
        val layoutInflater : LayoutInflater = LayoutInflater.from(context)
        val view : View = layoutInflater.inflate(resource , null )
        val title : TextView = view.findViewById(R.id.listTitle)
        val description : TextView = view.findViewById(R.id.listDesciption)
        val duedate : TextView = view.findViewById(R.id.listDuedate)
        val edit : Button = view.findViewById(R.id.editBtn)
        val delete : Button = view.findViewById(R.id.delBtn)

        db = TodoDatabaseHelper(this.context)

        // Get to-do item
        var todo = items[position]

        // Load title and description to single ListView item
        title.text = todo.title
        description.text = todo.description
        duedate.text = todo.duedate

        // OnClick Listener for edit button on every ListView items
        edit.setOnClickListener {
            // Very similar to the code in MainActivity.kt
            val builder = AlertDialog.Builder(this.context)
            val dialogView = layoutInflater.inflate(R.layout.add_todo_dialog, null)
            val titleToAdd = dialogView.findViewById<EditText>(R.id.todoTitle)
            val descriptionToAdd = dialogView.findViewById<EditText>(R.id.todoDescription)
            val dueDate = dialogView.findViewById<EditText>(R.id.todoDate)
            val checkBox = dialogView.findViewById<CheckBox>(R.id.checkBox)
            val mCalendar : Calendar = Calendar.getInstance()

            Log.d("dfdsf", todo.description.toString())
            dueDate.setText(dueDate.text.toString())

            dueDate.setOnClickListener{
                val calendar : Calendar = Calendar.getInstance()

                DatePickerDialog(
                    dialogView.context,
                    R.style.ShapeAppearanceOverlay_MaterialComponents_MaterialCalendar_Day,
                    DatePickerDialog.OnDateSetListener { datePicker, year, monthOfYear, dayOfMonth ->
                        val currentDate = Calendar.getInstance().apply{set(year, monthOfYear, dayOfMonth)}
                        dueDate.setText(year.toString()+"년 "+(monthOfYear+1).toString()+"월 " + dayOfMonth +"일")
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).apply {
                    datePicker.minDate = System.currentTimeMillis()
                }.show()
            }
            val ime = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            titleToAdd.setText(todo.title)
            descriptionToAdd.setText(todo.description.toString())
            duedate.setText(todo.duedate.toString())

            titleToAdd.requestFocus()
            ime.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)



            builder.setView(dialogView)
                .setPositiveButton("수정") { _, _ ->
                    val tmp = Todo(
                        titleToAdd.text.toString(),
                        descriptionToAdd.text.toString(),
                        dueDate.text.toString(),
                        checkBox.isChecked
                    )

                    val result = db.updateTodo(tmp, position)
                    if (result) {
                        todo.title = titleToAdd.text.toString()
                        todo.description = descriptionToAdd.text.toString()
                        todo.duedate = dueDate.text.toString()
                        todo.finished = checkBox.isChecked
                        notifyDataSetChanged()
                        ime.hideSoftInputFromWindow(titleToAdd.windowToken, 0)
                    }
                    else {
                        Toast.makeText(this.context, "수정 실패! :(", Toast.LENGTH_SHORT).show()
                        notifyDataSetChanged()
                    }
                }
                .setNegativeButton("취소") {_, _ ->
                    // Cancel Btn. Do nothing. Close keyboard.
                    ime.hideSoftInputFromWindow(titleToAdd.windowToken, 0)
                }
                .show()
        }

        // OnClick Listener for X(delete) button on every ListView items
        delete.setOnClickListener {
            val result = db.delTodo(position)
            if (result) {
                items.removeAt(position)
                notifyDataSetChanged()
            }
            else {
                Toast.makeText(this.context, "삭제 실패! :(", Toast.LENGTH_SHORT).show()
                notifyDataSetChanged()
            }
        }

        return view
    }
}