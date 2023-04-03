package com.example.doffoodbalancer

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.ViewModelProvider
import java.util.*
import kotlin.collections.ArrayList


class FoodListAdapter(
    private val activity: FoodSelectionActivity,
    private var foodList: MutableList<Food>,
    private val selectedFoods: ArrayList<SelectedFoodWithValue>,
) : BaseAdapter() {
    //) : ArrayAdapter<Food>(context, android.R.layout.simple_list_item_multiple_choice, foodList) {
    private val checkedItems = mutableSetOf<Int>()
    override fun getCount(): Int {
        return foodList.size + 1
    }

    override fun getItemId(position: Int): Long{
        return position.toLong()
    }

    override fun getItem(position: Int): Food? {
        return if (position == foodList.size) {
            null
        } else {
            foodList[position]
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == foodList.size) {
            VIEW_TYPE_ADD_FOOD
        } else {
            VIEW_TYPE_FOOD_LIST_ITEM
        }
    }

    fun getCheckedItems(): List<Food> {
        return checkedItems.map { activity.foodList[it] }
    }

    fun getFoodsList(): List<Food> {
        return foodList
    }

    fun clear() {
        foodList.clear()
    }

    fun addAll(newFoods: List<Food>) {
        foodList.addAll(newFoods)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewType = getItemViewType(position)
        val view: View

        if (viewType == VIEW_TYPE_ADD_FOOD) {
            // フード追加アイテムの場合
            val inflater = LayoutInflater.from(activity)
            view = inflater.inflate(R.layout.list_item_add_food, parent, false)
            view.findViewById<TextView>(R.id.food_add_item).text = "新規フード追加"
        } else {
            val food = foodList[position]
            val inflater = LayoutInflater.from(activity)
            view = inflater.inflate(R.layout.food_selection_list_item, parent, false)

            val foodName = view.findViewById<CheckedTextView>(R.id.food_name)
            val foodMaker = view.findViewById<TextView>(R.id.food_maker)
            val foodCalories = view.findViewById<TextView>(R.id.food_calories)
            foodName.text = food.food_name
            foodMaker.text = food.maker_name
            foodCalories.text = "${food.food_cal}"
            foodName.isChecked = checkedItems.contains(position)

            for (i in 0 until selectedFoods.size) {
                val checkedTextView = view.findViewById<CheckedTextView>(R.id.food_name)
                val currentFood = selectedFoods[i]
                val isSelected = food.food_id == currentFood.food.food_id
                if (isSelected) {
                    checkedTextView.isChecked = true
                    checkedItems.add(position)
                }
            }

            view.setOnClickListener {
                if (position != count - 1) {
                    // フードリストアイテムがクリックされた場合
                    val checkedTextView = it.findViewById<CheckedTextView>(R.id.food_name)
                    val listView = activity.foodList
                    checkedTextView.isChecked = !checkedTextView.isChecked
                    if (checkedTextView.isChecked) {
                        checkedItems.add(position)
                    } else {
                        checkedItems.remove(position)
                    }
                }
            }

            view.setOnLongClickListener {
                // コンテキストメニューの生成
                val popupMenu = PopupMenu(activity, view)
                popupMenu.gravity = Gravity.CENTER_HORIZONTAL
                popupMenu.menuInflater.inflate(R.menu.context_menu, popupMenu.menu)
                // コンテキストメニューのアイテムクリック時の処理
                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.edit_menu_item -> {
                            // 編集処理
                            editFoodListItem(activity.foodList,position)
                            true
                        }
                        R.id.delete_menu_item -> {
                            deleteFoodListItem(foodList,position)
                            true
                        }else ->
                        false
                    }
                }
                // コンテキストメニューを表示
                popupMenu.show()
                true
            }
        }
        return view
    }
    @SuppressLint("MissingInflatedId")
    fun addFoodListItem(){
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.add_newfood, null)
        var addFoodItem: Food
        val addFoodId = UUID.randomUUID().toString()
        val addFoodName = dialogView.findViewById<EditText>(R.id.edit_food_name)
        val addMakerName = dialogView.findViewById<EditText>(R.id.edit_maker_name)
        val addCalories = dialogView.findViewById<EditText>(R.id.edit_calories)
        addFoodName.setText("")
        addMakerName.setText("")
        addCalories.setText("")
        val dialog = AlertDialog.Builder(activity)
            .setView(dialogView)
            .setTitle("新しいフードの追加")
            .setPositiveButton("保存") { dialogInterface, i ->
                addFoodItem = Food(
                    food_id = addFoodId,
                    food_name = addFoodName.text.toString(),
                    maker_name = addMakerName.text.toString(),
                    food_cal = addCalories.text.toString().toInt()
                )
                activity.foodList.add(addFoodItem)
                activity.saveFoodList(activity, activity.foodList)
                notifyDataSetChanged()
            }
            .setNegativeButton("キャンセル", null)
            .create()
        dialog.show()
    }

    @SuppressLint("MissingInflatedId")
    fun editFoodListItem(foodList: MutableList<Food>, position: Int) {
        //val viewModel = ViewModelProvider(activity).get(AddNewFoodViewModel::class.java)
        val editFoodItem: Food = foodList[position].copyFood()
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.add_newfood, null)
        val editFoodName = dialogView.findViewById<EditText>(R.id.edit_food_name)
        val editMakerName = dialogView.findViewById<EditText>(R.id.edit_maker_name)
        val editCalories = dialogView.findViewById<EditText>(R.id.edit_calories)
        editFoodName.setText(editFoodItem.food_name)
        editMakerName.setText(editFoodItem.maker_name)
        editCalories.setText(editFoodItem.food_cal.toString())
        val dialog = AlertDialog.Builder(activity)
            .setView(dialogView)
            .setTitle("編集する")
            .setPositiveButton("保存") { dialogInterface, i ->
                val editedFoodItem = Food(
                    food_id = editFoodItem.food_id,
                    food_name = editFoodName.text.toString(),
                    maker_name = editMakerName.text.toString(),
                    food_cal = editCalories.text.toString().toInt()
                )
                activity.foodList[position] = editedFoodItem
                activity.saveFoodList(activity, activity.foodList)
                notifyDataSetChanged()
            }
            .setNegativeButton("キャンセル", null)
            .create()
        dialog.show()
    }

    fun deleteFoodListItem(foodList: MutableList<Food>, position: Int){
        val removedFoodList: MutableList<Food> = foodList.toMutableList()
        checkedItems.remove(position)
        val oldCheckedItems = checkedItems.toMutableList<Int>()
        removedFoodList.removeAt(position)
        activity.foodList = removedFoodList
        this.clear()
        this.addAll(removedFoodList)
        activity.saveFoodList(activity,removedFoodList)
        val checkedTextView = activity.findViewById<CheckedTextView>(R.id.food_name)
        checkedItems.clear()
        for(i in 0 until oldCheckedItems.size) {
            var checkedItem: Food = foodList[oldCheckedItems[i]]
            for (n in 0 until removedFoodList.size) {
                if (checkedTextView.isChecked == true && removedFoodList[n].food_id == checkedItem.food_id) {
                    checkedItems.add(n)
                }
            }
        }
        notifyDataSetChanged()
    }
    companion object {
        const val VIEW_TYPE_ADD_FOOD = 0
        const val VIEW_TYPE_FOOD_LIST_ITEM = 1
    }
}
