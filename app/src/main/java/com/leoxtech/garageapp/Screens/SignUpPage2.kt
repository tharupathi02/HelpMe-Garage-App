package com.leoxtech.garageapp.Screens

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.leoxtech.customerapp.Model.UserModel
import com.leoxtech.garageapp.Common.Common
import com.leoxtech.garageapp.R
import com.leoxtech.garageapp.databinding.ActivitySignUpPage2Binding
import java.io.IOException
import java.util.Locale

class SignUpPage2 : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpPage2Binding

    private lateinit var mAuth: FirebaseAuth
    private lateinit var userRef: DatabaseReference
    private lateinit var dialog: AlertDialog

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private var uID = "";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpPage2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        initialUserGoogleData()

        clickListeners()

        initLocation()

        dialogBox()
    }

    @SuppressLint("MissingPermission")
    private fun initLocation() {
        buildLocationRequest()
        buildLocationCallback()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient!!.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }

    private fun buildLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                currentLocation = p0!!.lastLocation!!
            }
        }
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest.setInterval(5000)
        locationRequest.setFastestInterval(3000)
        locationRequest.setSmallestDisplacement(10f)
    }

    @SuppressLint("MissingPermission")
    private fun clickListeners() {
        binding.txtSkipForm.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        binding.btnSignUp.setOnClickListener {
            signUpForm()
        }

        binding.btnSelectGarageLocation.setOnClickListener {
            dialog.show()
            fusedLocationProviderClient!!.lastLocation
                .addOnFailureListener { e ->
                    binding.txtGarageLocation.visibility = View.GONE
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener { task ->
                    latitude = task.result!!.latitude
                    longitude = task.result!!.longitude

                    val singleAddress = getAddressFromLatLng(task.result!!.latitude, task.result!!.longitude)

                    binding.txtGarageLocation.text = singleAddress.toString()
                    binding.txtGarageLocation.visibility = View.VISIBLE
                    dialog.dismiss()
                }
        }

        // Add vehicle types to chip group programmatically from array list in Common.kt
        for (i in Common.vehicleTypes.indices) {
            val chip = layoutInflater.inflate(R.layout.layout_chip, null, false) as Chip
            chip.text = Common.vehicleTypes[i]
            var selectedType = ""
            chip.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    Common.selectedVehicleTypes.add(buttonView.text.toString())
                    for (i in Common.selectedVehicleTypes.indices) {
                        selectedType = Common.selectedVehicleTypes.toString().replace("[", "").replace("]", "")
                        binding.txtSelectedVehicleTypes.text = selectedType
                    }

                } else {
                    Common.selectedVehicleTypes.remove(buttonView.text.toString())
                    for (i in Common.selectedVehicleTypes.indices) {
                        selectedType = Common.selectedVehicleTypes.toString().replace("[", "").replace("]", "")
                        binding.txtSelectedVehicleTypes.text = selectedType
                    }
                    if (Common.selectedVehicleTypes.isEmpty()) {
                        binding.txtSelectedVehicleTypes.text = "Select Vehicle Types you can repair or service"
                    }
                }
            }

            binding.chipGroupVehicleType.addView(chip)
        }

    }

    private fun getAddressFromLatLng(latitude: Double, longitude: Double): Any {
        val geoCoder = Geocoder(this, Locale.getDefault())
        val  result : String?=null
        try {
            val addressList = geoCoder.getFromLocation(latitude, longitude, 1)
            if (addressList != null && addressList.size > 0) {
                val address = addressList[0]
                val sb = StringBuilder(address.getAddressLine(0))
                return sb.toString()
            } else {
                return "Address not found"
            }
        } catch (e: IOException) {
            return e.message!!
        }
    }

    private fun signUpForm() {
        val firstName = binding.txtFirstName.editText?.text.toString()
        val lastName = binding.txtLastName.editText?.text.toString()
        val companyName = binding.txtCompanyName.editText?.text.toString()
        val registrationNumber = binding.txtRegistrationNumber.editText?.text.toString()
        val address = binding.txtAddress.editText?.text.toString()
        val contactNumber = binding.txtContactNumber.editText?.text.toString()
        val idNumber = binding.txtIDNumber.editText?.text.toString()
        val workingHours = binding.txtWorkingHours.editText?.text.toString()

        if (firstName.isEmpty()) {
            binding.txtFirstName.error = "First name is required"
            return
        } else if (lastName.isEmpty()) {
            binding.txtLastName.error = "Last name is required"
            return
        } else if (companyName.isEmpty()) {
            binding.txtCompanyName.error = "Company name is required"
            return
        } else if (address.isEmpty()) {
            binding.txtAddress.error = "Address is required"
            return
        } else if (contactNumber.isEmpty()) {
            binding.txtContactNumber.error = "Contact number is required"
            return
        } else if (idNumber.isEmpty()) {
            binding.txtIDNumber.error = "ID number is required"
            return
        } else if (workingHours.isEmpty()) {
            binding.txtWorkingHours.error = "Working hours is required"
            return
        } else if (Common.selectedVehicleTypes.isEmpty()) {
            Snackbar.make(binding.root, "Please select vehicle types you can repair or service", Snackbar.LENGTH_LONG).show()
            return
        }
        else {
            dialog.show()
            val userModel = UserModel()
            userModel.uid = uID
            userModel.name = binding.txtFirstName.editText?.text.toString() + " " + binding.txtLastName.editText?.text.toString()
            userModel.email = binding.txtEmail.editText?.text.toString()
            userModel.companyName = binding.txtCompanyName.editText?.text.toString()
            userModel.registrationNumber = binding.txtRegistrationNumber.editText?.text.toString()
            userModel.address = binding.txtAddress.editText?.text.toString()
            userModel.phone = binding.txtContactNumber.editText?.text.toString()
            userModel.idNumber = binding.txtIDNumber.editText?.text.toString()
            userModel.photoURL = mAuth.currentUser?.photoUrl.toString()
            userModel.latitude = latitude
            userModel.longitude = longitude
            userModel.workingHours = binding.txtWorkingHours.editText?.text.toString()
            userModel.workingVehicleTypes = Common.selectedVehicleTypes.toString().replace("[", "").replace("]", "")

            userRef.child(uID).setValue(userModel).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Snackbar.make(binding.root, "Congratulation! Registration Completed...", Snackbar.LENGTH_SHORT).show()
                    Common.currentUser = userModel
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    dialog.dismiss()
                } else {
                    Snackbar.make(binding.root, "Registration Failed... ${task.exception?.message}", Snackbar.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }

        }

    }

    private fun initialUserGoogleData() {
        userRef = FirebaseDatabase.getInstance().getReference(Common.GARAGE_USER_REFERENCE)
        uID = mAuth.currentUser?.uid.toString()
        binding.txtEmail.editText?.setText(mAuth.currentUser?.email)
    }

    private fun dialogBox() {
        AlertDialog.Builder(this).apply {
            setCancelable(false)
            setView(R.layout.progress_dialog)
        }.create().also {
            dialog = it
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

}
