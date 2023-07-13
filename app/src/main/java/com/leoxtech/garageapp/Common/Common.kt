package com.leoxtech.garageapp.Common

import android.text.format.DateFormat
import com.leoxtech.customerapp.Model.UserModel

object Common {
    const val USER_REFERENCE = "Users"
    const val GARAGE_USER_REFERENCE = "Garage Users"
    const val REQUEST_REF = "Requests"
    var currentUser: UserModel? = null

    // Create a array list of vehicle types
    var vehicleTypes = arrayListOf<String>("Bike", "Three Wheel", "Car", "Truck", "Bus", "Van", "SUV")
    var selectedVehicleTypes = arrayListOf<String>()

    fun convertTimeStampToDate(toLong: Long): CharSequence? {
        return DateFormat.format("dd-MM-yyyy HH:mm", toLong)
        return DateFormat.format("dddd, MMMM dd, yyyy hh:mm tt", toLong)
    }

}