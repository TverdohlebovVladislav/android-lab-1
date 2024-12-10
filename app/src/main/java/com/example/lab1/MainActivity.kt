package com.example.lab1

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var editTextItem: EditText
    private lateinit var btnAddItem: Button
    private lateinit var btnDeleteChecked: Button
    private lateinit var listViewItems: ListView

    private val shoppingList = mutableListOf<Pair<String, Boolean>>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация View элементов
        editTextItem = findViewById(R.id.editTextItem)
        btnAddItem = findViewById(R.id.btnAddItem)
        btnDeleteChecked = findViewById(R.id.btnDeleteChecked)
        listViewItems = findViewById(R.id.listViewItems)

        // Инициализация адаптера
        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_multiple_choice,
            shoppingList.map { it.first }
        )

        listViewItems.adapter = adapter
        listViewItems.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        // Загрузка данных
        loadShoppingList()
        updateAdapter()

        // Обработчики событий
        btnAddItem.setOnClickListener { addItem() }
        btnDeleteChecked.setOnClickListener { deleteCheckedItems() }

        // Сохранение состояний при изменении чекбоксов
        listViewItems.setOnItemClickListener { _, _, position, _ ->
            val currentItem = shoppingList[position]
            shoppingList[position] = currentItem.copy(second = !currentItem.second) // Меняем состояние
        }
    }

    private fun addItem() {
        val item = editTextItem.text.toString()
        if (item.isNotBlank()) {
            shoppingList.add(Pair(item, false))
            updateAdapter()
            editTextItem.text.clear()
        } else {
            Toast.makeText(this, "Введите название предмета", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteCheckedItems() {
        val checkedPositions = listViewItems.checkedItemPositions
        val itemsToRemove = mutableListOf<Pair<String, Boolean>>()

        for (i in 0 until checkedPositions.size()) {
            if (checkedPositions.valueAt(i)) {
                val position = checkedPositions.keyAt(i)
                itemsToRemove.add(shoppingList[position])
            }
        }

        shoppingList.removeAll(itemsToRemove)
        updateAdapter()
    }

    private fun updateAdapter() {
        adapter.clear()
        adapter.addAll(shoppingList.map { it.first })
        adapter.notifyDataSetChanged()

        // Устанавливаем состояния чекбоксов
        for (i in shoppingList.indices) {
            listViewItems.setItemChecked(i, shoppingList[i].second)
        }
    }

    private fun saveShoppingList() {
        val sharedPreferences = getSharedPreferences("ShoppingChecklist", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Сохраняем элементы как строки "название:true/false"
        val items = shoppingList.joinToString(";") { "${it.first}:${it.second}" }
        editor.putString("shopping_list", items)
        editor.apply()
    }

    private fun loadShoppingList() {
        val sharedPreferences = getSharedPreferences("ShoppingChecklist", MODE_PRIVATE)
        val items = sharedPreferences.getString("shopping_list", null)

        if (!items.isNullOrEmpty()) {
            shoppingList.clear()
            shoppingList.addAll(
                items.split(";").map {
                    val parts = it.split(":")
                    Pair(parts[0], parts[1].toBoolean()) // Разделяем на название и состояние
                }
            )
        }
    }

    override fun onPause() {
        super.onPause()
        saveShoppingList() // Сохранение списка
    }
}