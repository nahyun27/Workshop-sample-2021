package page.chungjungsoo.to_dosample

import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.WindowInsetsController
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import page.chungjungsoo.to_dosample.todo.Todo
import page.chungjungsoo.to_dosample.todo.TodoDatabaseHelper
import page.chungjungsoo.to_dosample.todo.TodoListViewAdapter
import java.util.*

class MainActivity : AppCompatActivity() {
    var dbHandler : TodoDatabaseHelper? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set content view - loads activity_main.xml
        setContentView(R.layout.activity_main)

        // Set app status bar color : white, force light status bar mode
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        // Set light status bar mode depending on the android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController!!.setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
        }
        else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // Add database helper and load data from database
        dbHandler = TodoDatabaseHelper(this)
        var todolist: MutableList<Todo> = dbHandler!!.getAll()

        // Put data with custom listview adapter
        todoList.adapter = TodoListViewAdapter(this, R.layout.todo_item, todolist)
        todoList.emptyView = helpText

        // Onclick listener for add button
        addBtn.setOnClickListener {
            // By pressing the add button, we will inflate an AlertDialog.
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.add_todo_dialog, null)

            // Get elements from custom dialog layout (add_todo_dialog.xml)
            val titleToAdd = dialogView.findViewById<EditText>(R.id.todoTitle)
            val descriptionToAdd = dialogView.findViewById<EditText>(R.id.todoDescription)

            val dueDate = dialogView.findViewById<EditText>(R.id.todoDate)
            val checkBox = dialogView.findViewById<CheckBox>(R.id.checkBox)

            val mCalendar : Calendar = Calendar.getInstance()
            dueDate.setText(mCalendar.get(Calendar.YEAR).toString() + "년 " + (mCalendar.get(Calendar.MONTH)+1).toString()+"월 " + mCalendar.get(Calendar.DAY_OF_MONTH).toString()+"일")

            dueDate.setOnClickListener{
                val calendar : Calendar = Calendar.getInstance()

                DatePickerDialog(
                    dialogView.context,
                    R.style.ShapeAppearanceOverlay_MaterialComponents_MaterialCalendar_Day,
                    DatePickerDialog.OnDateSetListener { datePicker, year, monthOfYear, dayOfMonth ->
                        //val currentDate = Calendar.getInstance().apply{set(year, monthOfYear, dayOfMonth)}
                        dueDate.setText(year.toString()+"년 "+(monthOfYear+1).toString()+"월 " + dayOfMonth +"일")
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).apply {
                    datePicker.minDate = System.currentTimeMillis()
                }.show()
            }


            // Add InputMethodManager for auto keyboard popup
            val ime = applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            // Cursor auto focus on title when AlertDialog is inflated
            titleToAdd.requestFocus()

            // Show keyboard when AlertDialog is inflated
            ime.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)


            // Add positive button and negative button for AlertDialog.
            // Pressing the positive button: Add data to the database and also add them in listview and update.
            // Pressing the negative button: Do nothing. Close the AlertDialog
            val add = builder.setView(dialogView)
                .setPositiveButton("추가") { _, _ ->
                    if (!TextUtils.isEmpty(titleToAdd.text.trim())) {
                        // Add item to the database
                        val todo = Todo(
                            titleToAdd.text.toString(),
                            descriptionToAdd.text.toString(),
                            dueDate.text.toString(),
                            checkBox.isChecked
                        )
                        dbHandler!!.addTodo(todo)

                        // Add them to listview and update.
                        (todoList.adapter as TodoListViewAdapter).add(todo)
                        (todoList.adapter as TodoListViewAdapter).notifyDataSetChanged()

                        // Close keyboard
                        ime.hideSoftInputFromWindow(titleToAdd.windowToken, 0)
                    }
                    else {
                        Toast.makeText(this,
                            "제목을 입력하세요!", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("취소") {_, _ ->
                    // Cancel Btn. Do nothing. Close keyboard.
                    ime.hideSoftInputFromWindow(titleToAdd.windowToken, 0)
                }
                .show()
                .getButton(DialogInterface.BUTTON_POSITIVE)

            // Default status of add button should be disabled. Because when AlertDialog inflates,
            // the title is empty by default and we do not want empty titles to be added to listview
            // and in databases.
            add.isEnabled = false

            
            // Listener for title text. If something is inputted in title, we should re-enable the add button.
            titleToAdd.addTextChangedListener(object: TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    if (!TextUtils.isEmpty(p0.toString().trim())) {
                        add.isEnabled = true
                    }
                    else {
                        titleToAdd.error = "TODO 제목을 입력하세요!"
                        add.isEnabled = false
                    }
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            })
        }
    }
}