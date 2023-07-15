package com.leoxtech.garageapp.Fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.leoxtech.garageapp.databinding.FragmentEmergencyBinding
import java.util.Collections

class EmergencyFragment : Fragment() {

    private lateinit var binding: FragmentEmergencyBinding

    private lateinit var dbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var dialog: AlertDialog

    private lateinit var requestHelpArrayList: ArrayList<RequestHelpModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEmergencyBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()

        dialogBox()

        requestHelpArrayList = arrayListOf<RequestHelpModel>()
        getUrgentRequests()

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

                    binding.recyclerEmergencyRequest.adapter = UrgentRequestAdapter(context!!, requestHelpArrayList)
                    binding.recyclerEmergencyRequest.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

                    if (requestHelpArrayList.size > 0) {
                        binding.txtNoRequestFound.visibility = View.GONE
                        dialog.dismiss()
                    } else {
                        binding.txtNoRequestFound.visibility = View.VISIBLE
                        dialog.dismiss()
                    }

                } else {
                    dialog.dismiss()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Snackbar.make(requireView(), error.message, Snackbar.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        })
    }

    private fun dialogBox() {
        AlertDialog.Builder(context).apply {
            setCancelable(false)
            setView(R.layout.progress_dialog)
        }.create().also {
            dialog = it
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

}