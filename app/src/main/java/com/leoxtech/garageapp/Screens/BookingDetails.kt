package com.leoxtech.garageapp.Screens

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.RatingBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.leoxtech.garageapp.Adapter.ImageAdapter
import com.leoxtech.garageapp.Adapter.ImageLargeAdapter
import com.leoxtech.garageapp.Adapter.MyBookingsAdapter
import com.leoxtech.garageapp.Common.Common
import com.leoxtech.garageapp.Model.Booking
import com.leoxtech.garageapp.R
import com.leoxtech.garageapp.databinding.ActivityBookingDetailsBinding

class BookingDetails : AppCompatActivity() {

    private lateinit var binding: ActivityBookingDetailsBinding

    private lateinit var dbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var dialog: AlertDialog

    val bookingRequestImages = ArrayList<String>()
    private var bookingId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        dialogBox()

        bookingId = intent.getStringExtra("key")
        getBookingDetails(bookingId!!)

        clickListeners()

    }

    private fun clickListeners() {
        binding.cardBack.setOnClickListener {
            finish()
        }

        binding.btnAccept.setOnClickListener {
            acceptBooking()
        }

        binding.btnReject.setOnClickListener {
            rejectBooking()

        }
    }

    private fun acceptBooking() {
        dialog.show()
        val builder = MaterialAlertDialogBuilder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.custom_dialog_booking, null)
        val txtNote = view.findViewById<TextInputLayout>(R.id.txtNote)

        builder.setPositiveButton("Submit") { dialogBox, which ->
            if (txtNote.editText!!.text.toString().isEmpty()) {
                txtNote.error = "Please Enter a Note"
                txtNote.requestFocus()
            } else {
                txtNote.error = null
                dbRef = FirebaseDatabase.getInstance().getReference(Common.BOOKING_REF)
                dbRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (bookingSnapshot in snapshot.children) {
                                if (bookingSnapshot.child("key").value.toString() == bookingId) {
                                    bookingSnapshot.child("bookingStatus").ref.setValue("Accepted")
                                    bookingSnapshot.child("bookingNote").ref.setValue(txtNote.editText!!.text.toString())
                                    dialog.dismiss()
                                    finish()
                                }
                            }

                        } else {
                            dialog.dismiss()
                            Toast.makeText(this@BookingDetails, "Accepting Failed", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        dialog.dismiss()
                        Toast.makeText(this@BookingDetails, error.message, Toast.LENGTH_SHORT).show()
                    }
                })
                dialogBox.dismiss()
            }
        }

        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }

        builder.setView(view)
        val confirmationDialog = builder.create()
        confirmationDialog.show()
    }

    private fun rejectBooking() {
        dialog.show()
        val builder = MaterialAlertDialogBuilder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.custom_dialog_booking, null)
        val txtNote = view.findViewById<TextInputLayout>(R.id.txtNote)

        builder.setPositiveButton("Submit") { dialogBox, which ->
            if (txtNote.editText!!.text.toString().isEmpty()) {
                txtNote.error = "Please Enter a Note"
                txtNote.requestFocus()
            } else {
                txtNote.error = null
                dbRef = FirebaseDatabase.getInstance().getReference(Common.BOOKING_REF)
                dbRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (bookingSnapshot in snapshot.children) {
                                if (bookingSnapshot.child("key").value.toString() == bookingId) {
                                    bookingSnapshot.child("bookingStatus").ref.setValue("Rejected")
                                    bookingSnapshot.child("bookingNote").ref.setValue(txtNote.editText!!.text.toString())
                                    dialog.dismiss()
                                    finish()
                                }
                            }

                        } else {
                            dialog.dismiss()
                            Toast.makeText(this@BookingDetails, "Accepting Failed", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        dialog.dismiss()
                        Toast.makeText(this@BookingDetails, error.message, Toast.LENGTH_SHORT).show()
                    }
                })
                dialogBox.dismiss()
            }
        }

        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }

        builder.setView(view)
        val confirmationDialog = builder.create()
        confirmationDialog.show()
    }

    private fun getBookingDetails(bookingKey: String) {
        dialog.show()
        dbRef = FirebaseDatabase.getInstance().getReference(Common.BOOKING_REF)
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (bookingSnapshot in snapshot.children) {
                        if (bookingSnapshot.child("key").value.toString() == bookingKey) {
                            binding.txtBookingTitle.text = bookingSnapshot.child("issueTitle").value.toString()
                            binding.txtBookingDescription.text = bookingSnapshot.child("issueDescription").value.toString()
                            binding.txtBookingDate.text = bookingSnapshot.child("bookingDate").value.toString()
                            binding.txtBookingTime.text = bookingSnapshot.child("bookingTime").value.toString()
                            binding.txtVehicleModel.text = bookingSnapshot.child("vehicleModel").value.toString()
                            binding.txtVehicleNumber.text = bookingSnapshot.child("vehicleNumber").value.toString()

                            if (bookingSnapshot.child("bookingType").value.toString() == "Home Service") {
                                binding.layoutBookingType.visibility = View.VISIBLE
                                binding.layoutCustomerAddress.visibility = View.VISIBLE
                                binding.txtBookingType.text = bookingSnapshot.child("bookingType").value.toString()
                                binding.txtCustomerAddress.text = bookingSnapshot.child("customerAddress").value.toString()
                            } else if (bookingSnapshot.child("bookingType").value.toString() == "Garage Visit") {
                                binding.layoutBookingType.visibility = View.GONE
                                binding.layoutCustomerAddress.visibility = View.GONE
                            }

                            for (imageSnapshot in bookingSnapshot.child("imageList").children) {
                                bookingRequestImages.add(imageSnapshot.value.toString())
                            }
                            binding.recyclerBookingImages.adapter = ImageLargeAdapter(this@BookingDetails, bookingRequestImages)
                            binding.recyclerBookingImages.layoutManager = LinearLayoutManager(this@BookingDetails, LinearLayoutManager.HORIZONTAL, false)
                            binding.recyclerBookingImages.setHasFixedSize(true)

                            if (bookingSnapshot.child("bookingStatus").value.toString() == "Rejected" || bookingSnapshot.child("bookingStatus").value.toString() == "Accepted") {
                                binding.btnAccept.visibility = View.GONE
                                binding.btnReject.visibility = View.GONE
                            }

                            dialog.dismiss()
                            getCustomerDetails(bookingSnapshot.child("customerId").value.toString())
                        }
                    }

                } else {
                    dialog.dismiss()
                    Toast.makeText(this@BookingDetails, "No Bookings Details Found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                dialog.dismiss()
                Toast.makeText(this@BookingDetails, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getCustomerDetails(customerId: String) {
        dbRef = FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCE)
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (customerSnapshot in snapshot.children) {
                        if (customerSnapshot.child("uid").value.toString() == customerId) {
                            binding.txtCustomerName.text = customerSnapshot.child("name").value.toString()
                            binding.txtMobileNumber.text = customerSnapshot.child("phone").value.toString()
                        }
                    }

                } else {
                    Toast.makeText(this@BookingDetails, "No Customer Details Found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@BookingDetails, error.message, Toast.LENGTH_SHORT).show()
            }
        })
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