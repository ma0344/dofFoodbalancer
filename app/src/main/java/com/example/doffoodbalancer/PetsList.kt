package com.example.doffoodbalancer

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object PetsList {
    private val pets = mutableListOf<PetInfo>()
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences("pets_preferences", Context.MODE_PRIVATE)
        loadPets()
    }

    fun addPet(pet: PetInfo) {
        pets.add(pet)
        savePets()
    }

    fun updatePet(index: Int, pet: PetInfo) {
        pets[index] = pet
        savePets()
    }

    fun removePet(index: Int) {
        pets.removeAt(index)
        savePets()
    }

    fun getPet(index: Int): PetInfo = pets[index]

    fun getAllPets(): List<PetInfo> = pets.toList()

    fun getSize(): Int = pets.size

    private fun savePets() {
        val jsonString = gson.toJson(pets)
        sharedPreferences.edit().putString("pets_list", jsonString).apply()
    }

    private fun loadPets() {
        val jsonString = sharedPreferences.getString("pets_list", null)
        if (jsonString != null) {
            val type = object : TypeToken<MutableList<PetInfo>>() {}.type
            pets.clear()
            pets.addAll(gson.fromJson(jsonString, type))
        }
    }
}
