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
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.leoxtech.garageapp.Adapter.UrgentRequestAdapter
import com.leoxtech.garageapp.Common.Common
import com.leoxtech.garageapp.Model.RequestHelpModel
import com.leoxtech.garageapp.R
import com.leoxtech.garageapp.databinding.ActivityHomeBinding
import java.io.IOException
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    private lateinit var dbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var dialog: AlertDialog

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location

    private lateinit var requestHelpArrayList: ArrayList<RequestHelpModel>

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        dialogBox()

        initLocation()

        profileInfo()

        clickListeners()

        requestHelpArrayList = arrayListOf<RequestHelpModel>()
        getUrgentRequests()

    }

    private fun getUrgentRequests() {
        dialog.show()
        dbRef = FirebaseDatabase.getInstance().getReference(Common.REQUEST_REF)
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                requestHelpArrayList.clear()
                if (snapshot.exists()) {
                    for (urgentSnapshot in snapshot.children) {
                        if (urgentSnapshot.child("status").value.toString() == "Urgent" && urgentSnapshot.child("garageUid").value.toString() == mAuth.currentUser!!.uid) {
                            val urgent = urgentSnapshot.getValue(RequestHelpModel::class.java)
                            requestHelpArrayList.add(urgent!!)
                        }
                    }

                    requestHelpArrayList.reverse()

                    if (requestHelpArrayList.size >= 0) {

                        binding.recyclerviewUrgentRequests.adapter = UrgentRequestAdapter(this@HomeActivity, requestHelpArrayList!!)
                        binding.recyclerviewUrgentRequests.layoutManager = LinearLayoutManager(this@HomeActivity, LinearLayoutManager.HORIZONTAL, false)

                        binding.txtNoUrgentRequests.visibility = View.GONE
                        binding.txtUrgentRequestCount.visibility = View.VISIBLE
                        binding.txtUrgentRequestCount.text = "You have (${requestHelpArrayList.size}) Urgent Requests Available in your area Now."

                        if (requestHelpArrayList.size > 1) {
                            binding.txtUrgentRequestSwipeText.visibility = View.VISIBLE
                        } else {
                            binding.txtUrgentRequestSwipeText.visibility = View.GONE
                        }
                        dialog.dismiss()
                    } else {
                        binding.txtNoUrgentRequests.visibility = View.VISIBLE
                        binding.txtUrgentRequestCount.visibility = View.GONE
                        binding.recyclerviewUrgentRequests.visibility = View.GONE
                        dialog.dismiss()
                    }

                } else {
                    binding.txtNoUrgentRequests.visibility = View.VISIBLE
                    binding.txtUrgentRequestCount.visibility = View.GONE
                    dialog.dismiss()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Snackbar.make(binding.root, error.message, Snackbar.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        })
    }

    private fun clickListeners() {
        binding.cardLocation.setOnClickListener {
            startActivity(Intent(this, CurrentLocationView::class.java))
        }

        binding.cardRequestHelp.setOnClickListener {
            startActivity(Intent(this, EmergencyActivity::class.java))
        }

        binding.cardBookingsRequests.setOnClickListener {
            startActivity(Intent(this, BookingsRequests::class.java))
        }

    }

    @SuppressLint("MissingPermission")
    private fun profileInfo() {
        dialog.show()
        if (Common.currentUser != null) {
            binding.txtCustomerName.text = Common.currentUser!!.companyName
            if (Common.currentUser!!.photoURL != null) {
                Glide.with(this).load(Common.currentUser!!.photoURL).into(binding.imgAvatar)
                dialog.dismiss()
            } else {
                binding.imgAvatar.setImageResource(R.drawable.avatar)
                dialog.dismiss()
            }
        }

        dialog.show()
        fusedLocationProviderClient!!.lastLocation
            .addOnFailureListener { e ->
                binding.txtLocation.text = "Location not found"
                Snackbar.make(binding.root, e.message!!, Snackbar.LENGTH_SHORT).show()
            }
            .addOnCompleteListener { task ->
                latitude = task.result!!.latitude
                longitude = task.result!!.longitude

                val geoCoder = Geocoder(this, Locale.getDefault())
                val  result : String?=null
                try {
                    val addressList = geoCoder.getFromLocation(latitude, longitude, 1)
                    if (addressList != null && addressList.size > 0) {
                        val address = addressList[0]
                        val sb = StringBuilder(address.getAddressLine(0))
                        binding.txtLocation.text = sb.toString()
                    } else {
                        binding.txtLocation.text = "Address not found"
                    }
                } catch (e: IOException) {
                    binding.txtLocation.text = e.message!!
                }
            }
        dialog.dismiss()
    }

    @SuppressLint("MissingPermission")
    private fun initLocation() {
        dialog.show()
        buildLocationRequest()
        buildLocationCallback()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient!!.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper()!!)
        dialog.dismiss()
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