package com.example.doffoodbalancer
import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import java.util.UUID

@Parcelize
data class PetInfo(
    var pet_name: String,
    var target_cal: Int,
    val selectedFoodsOfEachPet: MutableList<SelectedFoodWithValue>
) : Parcelable {
    override fun toString(): String {
        return pet_name
    }
}

@Parcelize
data class Food(
    val food_id: String = UUID.randomUUID().toString(),
    val food_name: String,
    val maker_name: String,
    val food_cal: Int
): Parcelable {
    fun copyFood(
        food_id: String = this.food_id,
        food_name: String = this.food_name,
        maker_name: String = this.maker_name,
        food_cal: Int = this.food_cal
    ) = Food(food_id,food_name,maker_name, food_cal)
}

@Parcelize
data class SelectedFoodWithValue(
    val food: Food,
    var seekbar_max :Int,
    var seekbar_step :Int,
    var seekbar_value: Int
): Parcelable
