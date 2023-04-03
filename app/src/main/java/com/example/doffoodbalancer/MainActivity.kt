package com.example.doffoodbalancer

import androidx.activity.viewModels
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
//import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.View
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.doffoodbalancer.databinding.ActivityMainBinding
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    //前回終了時点のデータを取得
    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences("doffoodbalancer_preferences", Context.MODE_PRIVATE)
    }
    //MainViewModelFactoryにsharedPreferencesを渡してMainViewModelに設定、MainActivityの復元
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(sharedPreferences)
    }

    private lateinit var petInfoAdapter: ArrayAdapter<PetInfo>
    private lateinit var petListAdapter: ArrayAdapter<PetsList>
    private lateinit var selectedFoodsAdapter: SelectedFoodsAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var activity: MainActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.saveButton.setOnClickListener {
            viewModel.saveSelectedFoods()
        }
        setupPetInfoSpinner()
        setupSelectedFoodsList()
        observeViewModel()
        activity = this

        binding.activityMainFoodList.setOnItemClickListener { parent, view, position, id ->
            if (position == selectedFoodsAdapter.count - 1) {
                // フード追加アイテムがクリックされた場合、FoodSelectionActivity に遷移します
                val intent = Intent(this@MainActivity, FoodSelectionActivity::class.java)
                intent.putParcelableArrayListExtra("selected_foods", ArrayList(selectedFoodsAdapter.getFoodsList()))
                startActivityForResult(intent, FoodSelectionActivity.FOOD_SELECTION_REQUEST_CODE)
            }
        }

        binding.targetCaloriesEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Do nothing
            }

            override fun afterTextChanged(s: Editable?) {
                val targetCalories = s.toString().toIntOrNull() ?: 0
                val currentCalories = viewModel.currentCalories.value ?: 0
                val difference = targetCalories - currentCalories
                binding.totalCaloriesTextView.text = getString(R.string.total_calories, currentCalories)
                binding.leftCaloriesTextView.text = getString(R.string.left_calories, difference)

                // Update the target calories of the selected PetInfo
                viewModel.selectedPetInfo.value?.let { petInfo ->
                    petInfo.target_cal = targetCalories
                    viewModel.saveSelectedFoods()
                }
            }
        })
    }

    private fun setupPetInfoSpinner() {

        // 「ペット追加」を追加
//        val addPetInfo = PetInfo("ペット追加", 0, mutableListOf())
        // ペット情報リストに「ペット追加」を追加
        val updatedPetInfoList = viewModel.petInfoList.value?.toMutableList()?.apply {
            add(PetInfo("ペット追加", 0, mutableListOf()))
        } ?: mutableListOf()

        petInfoAdapter = ArrayAdapter(this, R.layout.spinner_item, updatedPetInfoList)
        petInfoAdapter.setDropDownViewResource(R.layout.spinner_list_text)
        binding.petNameSpinner.adapter = petInfoAdapter
        binding.petNameSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val petInfo = parent.getItemAtPosition(position) as PetInfo
                // 「ペット追加」が選択された場合
                if (petInfo.pet_name == "ペット追加") {
                    selectedFoodsAdapter.addPetList()
                    binding.petNameSpinner.setSelection(0) // 追加
                } else {
                    viewModel.selectPetInfo(petInfo)
                    Log.d("petNameSpinner.onItemSelectedListener", "petInfo: ${petInfo.selectedFoodsOfEachPet}")
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

    private fun setupSelectedFoodsList() {
        selectedFoodsAdapter = SelectedFoodsAdapter(
            this,
            mutableListOf(),
            viewModel)
        binding.activityMainFoodList.adapter = selectedFoodsAdapter
    }

    private fun observeViewModel() {
        viewModel.petInfoList.observe(this) { petInfoList ->
            petInfoAdapter.clear()
            petInfoAdapter.addAll(petInfoList)
            petInfoAdapter.add(PetInfo("ペット追加", 0, mutableListOf()))
            petInfoAdapter.notifyDataSetChanged()
        }

        viewModel.selectedPetInfo.observe(this, Observer { petInfo ->
            binding.targetCaloriesEditText.setText(petInfo.target_cal.toString())
            selectedFoodsAdapter.updateSelectedFoods(petInfo.selectedFoodsOfEachPet)
            if (binding.activityMainFoodList.adapter == null) {
                binding.activityMainFoodList.adapter = selectedFoodsAdapter
            }
        })

        viewModel.currentCalories.observe(this, Observer { currentCalories ->
            val targetCaloriesStr = binding.targetCaloriesEditText.text.toString()
            if (targetCaloriesStr.isNotEmpty()) {
                val targetCalories = targetCaloriesStr.toIntOrNull() ?: 0
                val currentCalories = viewModel.currentCalories.value ?: 0
                val difference = targetCalories - currentCalories
                binding.totalCaloriesTextView.text = getString(R.string.total_calories, currentCalories)
                binding.leftCaloriesTextView.text = getString(R.string.left_calories, difference)

                // Update the target calories of the selected PetInfo
                viewModel.selectedPetInfo.value?.let { petInfo ->
                    petInfo.target_cal = targetCalories
                    viewModel.saveSelectedFoods()
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FoodSelectionActivity.FOOD_SELECTION_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.getParcelableArrayListExtra<SelectedFoodWithValue>(FoodSelectionActivity.SELECTED_FOODS_EXTRA)
                ?.let { selectedFoods ->
                    viewModel.updateSelectedFoods(selectedFoods)
                    // Save selected foods
                    viewModel.saveSelectedFoods()
                    // Update the adapter explicitly
                    selectedFoodsAdapter.updateSelectedFoods(selectedFoods)
                }
        }
        /*
        else if (requestCode == ADD_PET_REQUEST_CODE && resultCode == RESULT_OK) {
            // Add the added PetInfo
            data?.getParcelableExtra<PetInfo>(EXTRA_ADDED_PET_INFO)?.let { addedPetInfo: PetInfo ->
                viewModel.addPetInfo(addedPetInfo)
            }
        }
        */
    }


    companion object {
        const val ADD_PET_REQUEST_CODE = 2
        const val FOOD_SELECTION_REQUEST_CODE = 1
        const val SELECTED_FOODS_EXTRA = "com.example.doffoodbalancer.SELECTED_FOODS_EXTRA"
        const val EXTRA_ADDED_PET_INFO = "com.example.doffoodbalancer.EXTRA_ADDED_PET_INFO"
    }
}