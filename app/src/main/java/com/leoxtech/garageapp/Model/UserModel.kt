package com.leoxtech.customerapp.Model

import com.leoxtech.garageapp.Model.Review

class UserModel {
    var uid: String? = null
    var name: String? = null
    var companyName: String? = null
    var description: String? = null
    var registrationNumber: String? = null
    var email: String? = null
    var address: String? = null
    var phone: String? = null
    var idNumber: String? = null
    var photoURL: String? = null
    var latitude: Double? = null
    var longitude: Double? = null
    var workingVehicleTypes: String? = null
    var workingHours: String? = null

    var garageReview: List<Review>? = ArrayList<Review>()

    constructor(){}
}