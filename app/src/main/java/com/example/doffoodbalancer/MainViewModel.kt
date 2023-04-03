package com.example.doffoodbalancer

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainViewModel(private val sharedPreferences: SharedPreferences) : ViewModel() {

    val petInfoList = MutableLiveData<List<PetInfo>>().apply { value = mutableListOf() }

    private val _currentCalories = MutableLiveData<Int>()
    val currentCalories: LiveData<Int> get() = _currentCalories

    private val _selectedPetInfo = MutableLiveData<PetInfo>()
    val selectedPetInfo: LiveData<PetInfo> get() = _selectedPetInfo

    init {
        _currentCalories.value = 0
        loadPetInfoList()
    }

    fun selectPetInfo(petInfo: PetInfo) {
        _selectedPetInfo.value = petInfo
        updateCurrentCalories(petInfo.selectedFoodsOfEachPet)
    }

    fun updateSelectedFoods(selectedFoods: List<SelectedFoodWithValue>) {
        _selectedPetInfo.value?.let { petInfo ->
            petInfo.selectedFoodsOfEachPet.forEach { oldValue ->
                val oldListFood = oldValue.food.food_name
                selectedFoods.forEach { newValue ->
                    val newListFood = newValue.food.food_name
                    if (oldListFood == newListFood){
                        newValue.seekbar_value= oldValue.seekbar_value
                        newValue.seekbar_max= oldValue.seekbar_max
                    }
                }
            }
            petInfo.selectedFoodsOfEachPet.clear()

            petInfo.selectedFoodsOfEachPet.addAll(selectedFoods)
            updateCurrentCalories(petInfo.selectedFoodsOfEachPet)
        }
        loadPetInfoList()
    }

    fun updateCurrentCalories(selectedFoods: List<SelectedFoodWithValue>) {
        val totalCalories = selectedFoods.sumBy { selectedFoodWithValue ->
            selectedFoodWithValue.food.food_cal * selectedFoodWithValue.seekbar_value / 100
        }
        _currentCalories.value = totalCalories

    }


    fun addPetInfo(petInfo: PetInfo) {
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = sharedPreferences.getString("petInfoList", "")
        val type = object : TypeToken<List<PetInfo>>() {}.type
        val petInfoList = gson.fromJson<MutableList<PetInfo>>(json, type) ?: mutableListOf()

        petInfoList.add(petInfo)
        val petInfoJson = gson.toJson(petInfoList)
        editor.putString("petInfoList", petInfoJson)
        editor.apply()

        loadPetInfoList()
    }
    fun deleteFoodListItem(foodList: MutableList<Food>, position: Int){
        val removedFoodList: MutableList<Food> = foodList.toMutableList()
        removedFoodList.removeAt(position)
        foodList.clear()
        foodList.addAll(removedFoodList)
    }
    private fun loadPetInfoList() {
        val gson = Gson()
        val json = sharedPreferences.getString("petInfoList", "")
        val type = object : TypeToken<List<PetInfo>>() {}.type
        val petInfoList = gson.fromJson<List<PetInfo>>(json, type) ?: mutableListOf()
        this.petInfoList.value = petInfoList
    }
    //選択されたフードを保存するメソッド
    fun saveSelectedFoods() {
        selectedPetInfo.value?.let { petInfo ->
            val editor = sharedPreferences.edit()
            val gson = Gson()
            val json = sharedPreferences.getString("petInfoList", "")
            val type = object : TypeToken<List<PetInfo>>() {}.type
            val petInfoList = gson.fromJson<MutableList<PetInfo>>(json, type) ?: mutableListOf()

            val index = petInfoList.indexOfFirst { it.pet_name == petInfo.pet_name }
            if (index != -1) {
                petInfoList[index] = petInfo
                val petInfoJson = gson.toJson(petInfoList)
                editor.putString("petInfoList", petInfoJson)
                editor.apply()
            }
        }
    }//選択されたフードのリストを保存するメソッド
    private fun savePetInfoList(petInfoList: List<PetInfo>) {
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val petInfoJson = gson.toJson(petInfoList)
        editor.putString("petInfoList", petInfoJson)
        editor.apply()
    }

}
