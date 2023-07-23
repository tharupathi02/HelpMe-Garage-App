package com.leoxtech.garageapp.Screens

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.leoxtech.garageapp.Common.Common
import com.leoxtech.garageapp.Model.Bill
import com.leoxtech.garageapp.R
import com.leoxtech.garageapp.databinding.ActivityBookingCompleteBinding

class BookingComplete : AppCompatActivity() {

    private lateinit var binding: ActivityBookingCompleteBinding

    private lateinit var dbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var dialog: AlertDialog

    private var key: String? = ""
    private var garageName: String? = ""
    private var customerName: String? = ""
    private var vehicleModel: String? = ""
    private var vehicleNumber: String? = ""
    private var customerId: String? = ""

    var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingCompleteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialogBox()

        mAuth = FirebaseAuth.getInstance()

        getBookingDetails()

        clickListener()

    }

    private fun getBookingDetails() {
        key = intent.getStringExtra("key")
        garageName = intent.getStringExtra("garageName")
        customerName = intent.getStringExtra("customerName")
        vehicleModel = intent.getStringExtra("vehicleModel")
        vehicleNumber = intent.getStringExtra("vehicleNumber")
        customerId = intent.getStringExtra("customerId")

        binding.txtGarageName.editText?.setText(garageName)
        binding.txtCustomerName.editText?.setText(customerName)
        binding.txtVehicleModel.editText?.setText(vehicleModel)
        binding.txtVehicleNumber.editText?.setText(vehicleNumber)
        binding.txtBillDate.editText?.setText(Common.convertTimeStampToDate(System.currentTimeMillis()))
        dialog.dismiss()
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
                    dbRef = FirebaseDatabase.getInstance().getReference(Common.BILL_REF)
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
                    dbRef.child(key!!).setValue(bill).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            FirebaseDatabase.getInstance().getReference(Common.BOOKING_REF).child(key!!).child("bookingStatus").setValue("Completed")
                                .addOnCompleteListener(this@BookingComplete) { task ->
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