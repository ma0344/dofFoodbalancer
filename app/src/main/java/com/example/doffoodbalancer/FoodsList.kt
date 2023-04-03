package com.example.doffoodbalancer

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object FoodsList {
    private val foods = mutableListOf<Food>()
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences("foods_preferences", Context.MODE_PRIVATE)
        loadFoods()
    }

    fun addFood(food: Food) {
        foods.add(food)
        saveFoods()
    }

    fun updateFood(index: Int, food: Food) {
        foods[index] = food
        saveFoods()
    }

    fun removeFood(index: Int) {
        foods.removeAt(index)
        saveFoods()
    }

    fun getFood(index: Int): Food = foods[index]

    fun getAllFoods(): List<Food> = foods.toList()

    fun getSize(): Int = foods.size

    private fun saveFoods() {
        val jsonString = gson.toJson(foods)
        sharedPreferences.edit().putString("foods_list", jsonString).apply()
    }

    private fun loadFoods() {
        val jsonString = sharedPreferences.getString("foods_list", null)
        if (jsonString != null) {
            val type = object : TypeToken<MutableList<Food>>() {}.type
            foods.clear()
            foods.addAll(gson.fromJson(jsonString, type))
        }
    }
}
