package com.example.doffoodbalancer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.doffoodbalancer.databinding.ActivityAddPetBinding
import java.util.*


private const val VIEW_TYPE_FOOD_ITEM = 0
private const val VIEW_TYPE_ADD_FOOD_ITEM = 1
private lateinit var binding: ActivityAddPetBinding

class SelectedFoodsAdapter(
    val activity: MainActivity,
    val selectedFoods: MutableList<SelectedFoodWithValue>,
    private val viewModel: MainViewModel
) : ArrayAdapter<SelectedFoodWithValue>(activity, R.layout.list_item_food, selectedFoods) {

    override fun getCount(): Int {
        return selectedFoods.size + 1
    }
    override fun getItem(position: Int): SelectedFoodWithValue? {
        return if (position == selectedFoods.size) {
            null
        } else {
            selectedFoods[position]
        }
    }

    fun getFoodsList(): List<SelectedFoodWithValue> {
        return selectedFoods
    }

    fun addPetList(){
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.activity_add_pet, null)
        var addPetInfo: PetInfo
        val addPetName = dialogView.findViewById<EditText>(R.id.pet_name_edit_text)
        val addPetCal = dialogView.findViewById<EditText>(R.id.target_calories_edit_text)
        addPetName.setText("")
        val dialog = AlertDialog.Builder(activity)
            .setView(dialogView)
            .setTitle("ペットの追加")
            .setPositiveButton("保存") { dialogInterface, i ->
                if (addPetName.text.isNullOrEmpty()){
                    //Do Nothing
                }else {
                    val tcal: Int = if(addPetCal.text.isNullOrEmpty()){0}else{addPetCal.text.toString().toInt()}
                    addPetInfo = PetInfo(
                        pet_name = addPetName.text.toString(),
                        target_cal = tcal,
                        mutableListOf()
                    )
                    viewModel.addPetInfo(addPetInfo)
                    notifyDataSetChanged()
                }
            }
            .setNegativeButton("キャンセル", null)
            .create()
        dialog.show()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == selectedFoods.size) {
            VIEW_TYPE_ADD_FOOD_ITEM
        } else {
            VIEW_TYPE_FOOD_ITEM
        }
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewType = getItemViewType(position)
        val view: View
        if (viewType == VIEW_TYPE_ADD_FOOD_ITEM) {
            // フード追加アイテムの場合
            val inflater = LayoutInflater.from(context)
            view = inflater.inflate(R.layout.list_item_add_food, parent, false)
            val textView = view.findViewById<TextView>(R.id.food_add_item)

        } else {
            // 通常のフードアイテムの場合
            val inflater = LayoutInflater.from(context)
            view = inflater.inflate(R.layout.list_item_food, parent, false)
            val selectedFoodWithValue = selectedFoods[position]

            val foodNameTextView =
                view.findViewById<TextView>(R.id.activity_main_list_food_name)
            val foodSeekBarValueTextView =
                view.findViewById<TextView>(R.id.food_seekbar_calories)
            val foodSeekBar = view.findViewById<SeekBar>(R.id.food_seekbar)
            foodSeekBar?.tag = position

            //MainActivityのリストアイテム ロングタップリスナー
            //
            view.setOnLongClickListener { v ->
                val inflater = LayoutInflater.from(context)
                val dialogView = inflater.inflate(R.layout.dialog_max_value, null)
                val seekBar = dialogView.findViewById<SeekBar>(R.id.dialog_seekbar)
                val seekBarValueTextView = dialogView.findViewById<TextView>(R.id.dialog_seekbar_value)

                seekBar.max = 100 // 最大値を200に設定します。必要に応じて他の値に変更できます。
                seekBar.progress = selectedFoodWithValue.seekbar_max // シークバーの現在の値を選択されたアイテムの最大値に設定します。
                seekBarValueTextView.text = selectedFoodWithValue.seekbar_max.toString()
                //シークバーの変更リスナー
                seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        seekBarValueTextView.text = progress.toString()
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        // Do nothing
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        // Do nothing
                    }
                })

                val alertDialog = AlertDialog.Builder(context)
                    .setTitle("調節量設定 ${selectedFoodWithValue.food.food_name}")
                    .setView(dialogView)
                    .setPositiveButton("OK") { _, _ ->
                        val old_max_value = selectedFoodWithValue.seekbar_max
                        selectedFoodWithValue.seekbar_max = seekBar.progress
                        // 必要に応じてアダプターを更新し、データを保存します。
                        notifyDataSetChanged()
                        if (old_max_value != selectedFoodWithValue.seekbar_max){
                            foodSeekBar?.max = selectedFoodWithValue.seekbar_max
                            viewModel.updateSelectedFoods(selectedFoods)
                            viewModel.saveSelectedFoods()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .create()

                alertDialog.show()
                true
            }

            foodNameTextView.text = selectedFoodWithValue.food.food_name

            if (selectedFoodWithValue.seekbar_max == 0){foodSeekBar?.max = 100}else{foodSeekBar?.max = selectedFoodWithValue.seekbar_max}
            foodSeekBar?.progress = selectedFoodWithValue.seekbar_value
            val foodSeekBarGramsTextView = view.findViewById<TextView>(R.id.food_seekbar_grams)
            val foodSeekBarGrams = selectedFoodWithValue.seekbar_value
            foodSeekBarGramsTextView.text =
                context.getString(R.string.food_seekbar_grams, foodSeekBarGrams)

            val foodSeekBarCalories =
                selectedFoodWithValue.food.food_cal * selectedFoodWithValue.seekbar_value / 100
            foodSeekBarValueTextView.text =
                context.getString(R.string.food_seekbar_value, foodSeekBarCalories)

            foodSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val updatedSeekBarCalories =
                        selectedFoodWithValue.food.food_cal * progress / 100
                    selectedFoods[seekBar?.tag as Int].seekbar_value = progress
                    foodSeekBarValueTextView.text =
                        context.getString(R.string.food_seekbar_value, updatedSeekBarCalories)
                    val updatedSeekBarGrams = progress
                    foodSeekBarGramsTextView.text =
                        context.getString(R.string.food_seekbar_grams, updatedSeekBarGrams)
                    // Update the total calories and difference in the viewModel
                    viewModel.updateCurrentCalories(selectedFoods)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    // Do nothing
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    // Do nothing
                }
            })
        }
        return view
    }
    fun updateSelectedFoods(newSelectedFoods: List<SelectedFoodWithValue>) {
        selectedFoods.clear()
        selectedFoods.addAll(newSelectedFoods)
        Log.d("updateSelectedFoods", "selectedFoods: $newSelectedFoods")

        notifyDataSetChanged()
        viewModel.updateCurrentCalories(selectedFoods)
    }

}