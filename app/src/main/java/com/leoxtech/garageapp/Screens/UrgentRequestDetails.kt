package com.leoxtech.garageapp.Screens

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
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
import com.leoxtech.garageapp.databinding.ActivityUrgentRequestDetailsBinding

class UrgentRequestDetails : AppCompatActivity() {

    private lateinit var binding: ActivityUrgentRequestDetailsBinding

    private lateinit var dbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var dialog: AlertDialog
    private lateinit var bottomSheetDialog: BottomSheetDialog

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var key: String? = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUrgentRequestDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialogBox()

        mAuth = FirebaseAuth.getInstance()
        key = intent.getStringExtra("key")

        getUrgentRequestDetails()

        clickListener()

    }

    private fun clickListener() {
        binding.cardShowDetails.setOnClickListener {
            bottomSheetDialog.show()
        }

        binding.cardBack.setOnClickListener {
            finish()
        }
    }

    private fun getUrgentRequestDetails() {
        dialog.show()
        dbRef = FirebaseDatabase.getInstance().getReference(Common.REQUEST_REF)
        dbRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("MissingPermission")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (urgentSnapshot in snapshot.children) {
                        if (urgentSnapshot.key == key && urgentSnapshot.child("status").value.toString() == "Urgent") {
                            val view: View = layoutInflater.inflate(R.layout.bottom_sheet_urgent_request_details, null)
                            bottomSheetDialog = BottomSheetDialog(this@UrgentRequestDetails)
                            bottomSheetDialog.setContentView(view)
                            val txtRequestTitle = view.findViewById<TextView>(R.id.txtRequestTitle)
                            val txtDateTime = view.findViewById<TextView>(R.id.txtDateTime)
                            val txtRequestDescription = view.findViewById<TextView>(R.id.txtRequestDescription)
                            val txtCustomerName = view.findViewById<TextView>(R.id.txtCustomerName)
                            val txtCustomerPhone = view.findViewById<TextView>(R.id.txtCustomerPhone)
                            val txtCustomerVehicle = view.findViewById<TextView>(R.id.txtCustomerVehicle)
                            val btnAccept = view.findViewById<Button>(R.id.btnAccept)
                            val btnReject = view.findViewById<Button>(R.id.btnReject)

                            txtRequestTitle.text = urgentSnapshot.child("customerIssueTitle").value.toString()
                            txtDateTime.text = Common.convertTimeStampToDate(urgentSnapshot.child("timeStamp").value.toString().toLong())
                            txtRequestDescription.text = urgentSnapshot.child("customerIssueDescription").value.toString()
                            txtCustomerName.text = urgentSnapshot.child("customerName").value.toString()
                            txtCustomerPhone.text = urgentSnapshot.child("customerPhone").value.toString()
                            txtCustomerVehicle.text = urgentSnapshot.child("customerVehicle").value.toString()

                            val latLngCustomer = LatLng(urgentSnapshot.child("latitude").value.toString().toDouble(), urgentSnapshot.child("longitude").value.toString().toDouble())

                            val locationResult = LocationServices.getFusedLocationProviderClient(this@UrgentRequestDetails).lastLocation
                            val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
                            mapFragment.getMapAsync { googleMap ->
                                locationResult.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val lastKnownLocation = task.result
                                        if (lastKnownLocation != null) {
                                            val latLng = LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
                                            googleMap.isMyLocationEnabled = true
                                            googleMap.uiSettings.isMyLocationButtonEnabled = true
                                            googleMap.uiSettings.isZoomControlsEnabled = true
                                            googleMap.uiSettings.isZoomGesturesEnabled = true
                                            googleMap.uiSettings.isScrollGesturesEnabled = true
                                            googleMap.uiSettings.isTiltGesturesEnabled = true
                                            googleMap.uiSettings.isRotateGesturesEnabled = true
                                            googleMap.uiSettings.isCompassEnabled = true
                                            googleMap.uiSettings.isMapToolbarEnabled = true
                                            googleMap.uiSettings.isRotateGesturesEnabled = true
                                            googleMap.uiSettings.isTiltGesturesEnabled = true
                                            googleMap.isIndoorEnabled = true
                                            googleMap.projection.visibleRegion.latLngBounds
                                            googleMap.isBuildingsEnabled = true
                                            googleMap.isTrafficEnabled = true
                                            googleMap.isMyLocationEnabled = true

                                            googleMap.addMarker(MarkerOptions().position(latLngCustomer).title(urgentSnapshot.child("customerVehicle").value.toString()).icon(BitmapDescriptorFactory.fromResource(R.drawable.location_3d)))
                                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngCustomer, 13f))

                                            dialog.dismiss()
                                        }
                                    }
                                }
                                dialog.dismiss()
                            }

                            btnAccept.setOnClickListener {
                                dialog.show()
                                FirebaseDatabase.getInstance().getReference(Common.REQUEST_REF).child(key!!).child("status").setValue("Accepted")
                                    .addOnCompleteListener(this@UrgentRequestDetails) { task ->
                                        if (task.isSuccessful) {
                                            Snackbar.make(binding.root, "Request Accepted", Snackbar.LENGTH_LONG).show()
                                            bottomSheetDialog.dismiss()
                                            finish()
                                            dialog.dismiss()
                                        } else {
                                            Snackbar.make(binding.root, task.exception!!.message.toString(), Snackbar.LENGTH_LONG).show()
                                            dialog.dismiss()
                                        }
                                    }

                            }

                            btnReject.setOnClickListener {
                                dialog.show()
                                FirebaseDatabase.getInstance().getReference(Common.REQUEST_REF).child(key!!).child("status").setValue("Rejected")
                                    .addOnCompleteListener(this@UrgentRequestDetails) { task ->
                                        if (task.isSuccessful) {
                                            Snackbar.make(binding.root, "Request Rejected", Snackbar.LENGTH_LONG).show()
                                            bottomSheetDialog.dismiss()
                                            finish()
                                            dialog.dismiss()
                                        } else {
                                            Snackbar.make(binding.root, task.exception!!.message.toString(), Snackbar.LENGTH_LONG).show()
                                            dialog.dismiss()
                                        }
                                    }
                            }

                            dialog.dismiss()
                        } else {
                            dialog.dismiss()
                        }
                    }


                } else {
                    dialog.dismiss()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Snackbar.make(binding.root, error.message, Snackbar.LENGTH_LONG).show()
                dialog.dismiss()
            }
        })
    }

    private fun bitmapDescriptor(urgentRequestDetails: UrgentRequestDetails, location3d: Int): BitmapDescriptor? {
        val vectorDrawable = ContextCompat.getDrawable(urgentRequestDetails, location3d)
        vectorDrawable!!.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
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