package com.leoxtech.garageapp.Common

import com.leoxtech.customerapp.Model.UserModel

object Common {

    const val USER_REFERENCE = "Users"
    const val GARAGE_USER_REFERENCE = "Garage Users"
    const val REQUEST_REF = "Requests"
    var currentUser: UserModel? = null

    // Create a array list of vehicle types
    var vehicleTypes = arrayListOf<String>("Bike", "Three Wheel", "Car", "Truck", "Bus", "Van", "SUV")
    var selectedVehicleTypes = arrayListOf<String>()

}