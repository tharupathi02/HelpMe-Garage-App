package com.leoxtech.garageapp.Screens

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
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
import com.google.firebase.storage.FirebaseStorage
import com.leoxtech.garageapp.Common.Common
import com.leoxtech.garageapp.Model.Bill
import com.leoxtech.garageapp.R
import com.leoxtech.garageapp.databinding.ActivityRequestCompleteBinding
import com.leoxtech.garageapp.databinding.ActivityUrgentRequestDetailsBinding

class RequestComplete : AppCompatActivity() {

    private lateinit var binding: ActivityRequestCompleteBinding

    private lateinit var dbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var dialog: AlertDialog

    private var key: String? = "0"
    private var customerId: String? = "0"
    var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestCompleteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialogBox()

        mAuth = FirebaseAuth.getInstance()
        key = intent.getStringExtra("key")

        getUrgentRequestDetails()

        clickListener()

    }

    private fun clickListener() {

        binding.cardBack.setOnClickListener {
            finish()
        }

        binding.btnAttachBill.setOnClickListener {
            ImagePicker.with(this).crop().compress(1024).maxResultSize(1080, 1080).start()
        }

        binding.btnCompleteRequest.setOnClickListener {
            if (binding.txtBillAmount.editText!!.text.toString().isEmpty()) {
                binding.txtBillAmount.error = "Enter Bill Amount"
                binding.txtBillAmount.requestFocus()
            } else if (selectedImageUri == null) {
                Snackbar.make(binding.root, "Please attach bill to proceed next", Snackbar.LENGTH_LONG).show()
            } else {
                saveCustomerBill()
            }
        }
    }

    private fun saveCustomerBill() {
        dialog.show()
        val storageRef = FirebaseStorage.getInstance().reference.child(Common.STORAGE_REF + customerId + "-" + System.currentTimeMillis() + ".jpg")
        storageRef.putFile(selectedImageUri!!).addOnCompleteListener{ task ->
            if (task.isSuccessful) {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    var bill = Bill()
                    bill.key = dbRef.push().key
                    bill.garageId = mAuth.currentUser!!.uid
                    bill.customerId = customerId
                    bill.vehicleModel = binding.txtVehicleModel.editText!!.text.toString()
                    bill.billDate = System.currentTimeMillis().toString()
                    bill.vehicleNumber = binding.txtVehicleNumber.editText!!.text.toString()
                    bill.inspectionDetails = binding.txtInspectionDetails.editText!!.text.toString()
                    bill.billAmount = binding.txtBillAmount.editText!!.text.toString()
                    bill.billImageUrl = uri.toString()

                    dbRef = FirebaseDatabase.getInstance().getReference(Common.BILL_REF)
                    dbRef.child(key!!).setValue(bill).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            FirebaseDatabase.getInstance().getReference(Common.REQUEST_REF).child(key!!).child("status").setValue("Completed")
                                .addOnCompleteListener(this@RequestComplete) { task ->
                                    if (task.isSuccessful) {
                                        Snackbar.make(binding.root, "Bill Added Successfully", Snackbar.LENGTH_LONG).show()
                                        startActivity(Intent(this, HomeActivity::class.java))
                                        finish()
                                        dialog.dismiss()
                                    } else {
                                        Snackbar.make(binding.root, task.exception!!.message.toString(), Snackbar.LENGTH_LONG).show()
                                        dialog.dismiss()
                                    }
                                }
                        } else {
                            dialog.dismiss()
                            Snackbar.make(binding.root, task.exception!!.message.toString(), Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            } else {
                dialog.dismiss()
                Snackbar.make(binding.root, task.exception!!.message.toString(), Snackbar.LENGTH_LONG).show()
            }
        }.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
            val progressText = dialog.findViewById<TextView>(R.id.txtProgress)
            progressText.visibility = View.VISIBLE
            progressText.text = "Updating... " + progress.toInt() + "%"
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
                        if (urgentSnapshot.key == key) {
                            binding.txtGarageName.editText!!.setText(Common.currentUser?.companyName.toString())
                            binding.txtCustomerName.editText!!.setText(urgentSnapshot.child("customerName").value.toString())
                            binding.txtVehicleModel.editText!!.setText(urgentSnapshot.child("customerVehicle").value.toString())
                            binding.txtBillDate.editText!!.setText(Common.convertTimeStampToDate(System.currentTimeMillis()))
                            customerId = urgentSnapshot.child("customerUid").value.toString()
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

    private fun dialogBox() {
        AlertDialog.Builder(this).apply {
            setCancelable(false)
            setView(R.layout.progress_dialog)
        }.create().also {
            dialog = it
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (data!!.data != null) {
                selectedImageUri = data.data
                binding.imgBill.setImageURI(selectedImageUri)

            } else if (resultCode == ImagePicker.RESULT_ERROR) {
                Snackbar.make(binding.root, "Error: Image draw too large. Please select another image. and try again.", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

}