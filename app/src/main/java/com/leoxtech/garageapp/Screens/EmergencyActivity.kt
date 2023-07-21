package com.leoxtech.garageapp.Screens

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.leoxtech.garageapp.databinding.ActivityEmergencyBinding

class EmergencyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmergencyBinding

    private lateinit var dbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var dialog: AlertDialog

    private lateinit var requestHelpArrayList: ArrayList<RequestHelpModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmergencyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        dialogBox()

        requestHelpArrayList = arrayListOf<RequestHelpModel>()
        getUrgentRequests()

        binding.cardBack.setOnClickListener {
            finish()
        }

    }

    private fun getUrgentRequests() {
        dialog.show()
        dbRef = FirebaseDatabase.getInstance().getReference(Common.REQUEST_REF)
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    requestHelpArrayList.clear()
                    for (emergencySnapshot in snapshot.children) {
                        if (emergencySnapshot.child("garageUid").value.toString() == mAuth.currentUser!!.uid) {
                            val urgent = emergencySnapshot.getValue(RequestHelpModel::class.java)
                            requestHelpArrayList.add(urgent!!)
                        }
                    }

                    requestHelpArrayList.reverse()

                    if (requestHelpArrayList.isNotEmpty()) {

                        binding.recyclerEmergencyRequest.adapter = UrgentRequestAdapter(this@EmergencyActivity, requestHelpArrayList)
                        binding.recyclerEmergencyRequest.layoutManager = LinearLayoutManager(this@EmergencyActivity, LinearLayoutManager.VERTICAL, false)

                        binding.txtNoRequestFound.visibility = View.GONE
                        dialog.dismiss()
                    } else {
                        binding.txtNoRequestFound.visibility = View.VISIBLE
                        dialog.dismiss()
                    }

                } else {
                    binding.txtNoRequestFound.visibility = View.VISIBLE
                    dialog.dismiss()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Snackbar.make(binding.root, error.message, Snackbar.LENGTH_SHORT).show()
                dialog.dismiss()
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