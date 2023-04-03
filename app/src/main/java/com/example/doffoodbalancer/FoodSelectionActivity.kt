
package com.example.doffoodbalancer
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.util.Log
import android.util.SparseBooleanArray
import android.view.ContextMenu
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.doffoodbalancer.databinding.ActivityFoodSelectionBinding

class FoodSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFoodSelectionBinding
    var foodList = mutableListOf<Food>()
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var foodListAdapter: FoodListAdapter
    //private lateinit var ViewModel: MainViewModel
    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences("doffoodbalancer_preferences", Context.MODE_PRIVATE)
    }

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(sharedPreferences)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_food_selection)
        val selectedFoods: ArrayList<SelectedFoodWithValue> by lazy {
            intent.getParcelableArrayListExtra<SelectedFoodWithValue>("selected_foods")
                ?: arrayListOf()
        }

        foodListAdapter = FoodListAdapter(this, foodList, selectedFoods)
        binding.foodListView.adapter = foodListAdapter

        val listView = binding.foodListView
        val finishSelectionButton = binding.finishSelectionButton
        val foodListView = binding.foodListView
        //selectedFoods: 既に選択されたフードのリストをインテントから取得します。

        Log.d("FoodSelectionActivity", "selectedFoods: $selectedFoods")

        val savedFoodList = getFoodList(this)
        if (savedFoodList.isNotEmpty()) {
            foodList.addAll(savedFoodList)
        }
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        binding.foodListView.setOnItemClickListener { parent, view, position, id ->
            if (position == foodListAdapter.count - 1) {
                // フード追加アイテムがクリックされた場合、FoodSelectionActivity に遷移します
                foodListAdapter.addFoodListItem()
/*
                val intent = Intent(this@FoodSelectionActivity, NewFoodActivity::class.java)
                intent.putParcelableArrayListExtra(
                    "foods_list",
                    ArrayList(foodListAdapter.getFoodsList())
                )
                startActivityForResult(intent, ADD_FOOD_REQUEST_CODE)
*/
            } else {
                // フードリストアイテムがクリックされた場合
                val checkedTextView = view.findViewById<CheckedTextView>(R.id.food_name)
                checkedTextView.isChecked = !checkedTextView.isChecked
                listView.setItemChecked(position, checkedTextView.isChecked)
            }
        }
        foodListAdapter.notifyDataSetChanged()


        for (i in 0 until foodList.size) {
            val currentFood = foodList[i]
            val isSelected = selectedFoods.any { it.food.food_id == currentFood.food_id }
            if (isSelected) {
                listView.setItemChecked(i, true)
            }
        }

        finishSelectionButton.setOnClickListener {
            val intent = Intent()
            var checkedItems = foodListAdapter.getCheckedItems()
            val selectedFoodsWithValue = mutableListOf<SelectedFoodWithValue>()
            val oldSelectedFoodWithValue = selectedFoodsWithValue.toMutableList()
            for (i in 0 until checkedItems.size) {
                val checkedItem = checkedItems[i]
                var fl = -1
                for (oldSelectedFoodPosition in 0 until oldSelectedFoodWithValue.size) {
                    if (checkedItem.food_id == oldSelectedFoodWithValue[oldSelectedFoodPosition].food.food_id) {
                        fl = oldSelectedFoodPosition
                    }
                }
                if(fl >= 0) {
                    selectedFoodsWithValue.add(oldSelectedFoodWithValue[fl])
                }else{
                    selectedFoodsWithValue.add(
                        SelectedFoodWithValue(checkedItem,0,1,100)
                    )
                }
            }
            intent.putParcelableArrayListExtra(SELECTED_FOODS_EXTRA, ArrayList(selectedFoodsWithValue))
            setResult(RESULT_OK, intent)
            finish()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_FOOD_REQUEST_CODE && resultCode == RESULT_OK) {
            val newFood = data?.getParcelableExtra<Food>(NEW_FOOD_EXTRA)
            newFood?.let {
                foodList.add(newFood)
                val newFoodList: List<Food> = foodList.toList()
                Log.d("FoodSelectionActivity", "Food list: $foodList")
                foodListAdapter.clear()
                foodListAdapter.addAll(newFoodList)
                foodListAdapter.notifyDataSetChanged()
                saveFoodList(this, foodList)
            }
        }
    }

    fun saveFoodList(context: Context, foodList: List<Food>) {
        val sharedPreferences = context.getSharedPreferences("FoodList", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(foodList)
        editor.putString(FOOD_LIST_KEY, json)
        editor.apply()
    }

    fun getFoodList(context: Context): MutableList<Food> {
        val sharedPreferences = context.getSharedPreferences("FoodList", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString(FOOD_LIST_KEY, "")
        return if (json.isNullOrEmpty()) {
            mutableListOf()
        } else {
            val type = object : TypeToken<List<Food>>() {}.type
            gson.fromJson(json, type)
        }
    }

    companion object {
        const val ADD_FOOD_REQUEST_CODE = 2
        const val NEW_FOOD_EXTRA = "NEW_FOOD_EXTRA"
        const val SELECTED_FOODS_EXTRA = "com.example.doffoodbalancer.SELECTED_FOODS_EXTRA"
        const val FOOD_SELECTION_REQUEST_CODE = 1
        const val FOOD_LIST_KEY = "food_list_key"
    }
}