package com.leoxtech.garageapp.Common

import android.text.format.DateFormat
import com.leoxtech.customerapp.Model.UserModel
import java.text.SimpleDateFormat

object Common {
    const val USER_REFERENCE = "Users"
    const val GARAGE_USER_REFERENCE = "Garage Users"
    const val REQUEST_REF = "Requests"
    const val BILL_REF = "Bills"
    const val STORAGE_REF = "billImages/"
    var currentUser: UserModel? = null
    val BOOKING_REF: String = "Bookings"

    // Create a array list of vehicle types
    var vehicleTypes = arrayListOf<String>("Bike", "Three Wheel", "Car", "Truck", "Bus", "Van", "SUV")
    var selectedVehicleTypes = arrayListOf<String>()

    fun convertTimeStampToDate(toLong: Long): CharSequence? {
        return SimpleDateFormat("MMMM dd, yyyy hh:mm aa").format(toLong)
    }

}