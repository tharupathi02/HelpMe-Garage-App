package com.leoxtech.garageapp.Screens

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.leoxtech.garageapp.Adapter.MyBookingsAdapter
import com.leoxtech.garageapp.Common.Common
import com.leoxtech.garageapp.Model.Booking
import com.leoxtech.garageapp.R
import com.leoxtech.garageapp.databinding.ActivityBookingsRequestsBinding

class BookingsRequests : AppCompatActivity() {

    private lateinit var binding: ActivityBookingsRequestsBinding

    private lateinit var dbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var dialog: AlertDialog

    private lateinit var bookingArrayList: ArrayList<Booking>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingsRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        dialogBox()

        bookingArrayList = arrayListOf<Booking>()
        getBookings()

        binding.cardBack.setOnClickListener {
            finish()
        }

    }

    private fun getBookings() {
        dialog.show()
        dbRef = FirebaseDatabase.getInstance().getReference(Common.BOOKING_REF)
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    bookingArrayList.clear()
                    for (bookingSnapshot in snapshot.children) {
                        if (bookingSnapshot.child("garageId").value.toString() == Common.currentUser!!.uid) {
                            val booking = bookingSnapshot.getValue(Booking::class.java)
                            bookingArrayList.add(booking!!)
                        }
                    }
                    bookingArrayList.reverse()
                    if (bookingArrayList.isNotEmpty()) {
                        binding.recyclerBookingRequests.adapter = MyBookingsAdapter(this@BookingsRequests, bookingArrayList!!)
                        binding.recyclerBookingRequests.layoutManager = LinearLayoutManager(this@BookingsRequests, LinearLayoutManager.VERTICAL, false)
                        binding.txtNotFound.visibility = View.GONE
                        dialog.dismiss()
                    } else {
                        dialog.dismiss()
                        binding.txtNotFound.visibility = View.VISIBLE
                    }
                } else {
                    dialog.dismiss()
                    Toast.makeText(this@BookingsRequests, "No Bookings", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                dialog.dismiss()
                Toast.makeText(this@BookingsRequests, error.message, Toast.LENGTH_SHORT).show()
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