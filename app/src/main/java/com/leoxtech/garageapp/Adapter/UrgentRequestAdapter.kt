package com.leoxtech.garageapp.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase
import com.leoxtech.garageapp.Common.Common
import com.leoxtech.garageapp.Model.RequestHelpModel
import com.leoxtech.garageapp.R
import com.leoxtech.garageapp.Screens.RequestComplete
import com.leoxtech.garageapp.Screens.UrgentRequestDetails

class UrgentRequestAdapter (internal var context: Context, private var urgentRequestList: List<RequestHelpModel>) : RecyclerView.Adapter<UrgentRequestAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UrgentRequestAdapter.MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.urgent_request_item, parent, false))
    }

    override fun onBindViewHolder(holder: UrgentRequestAdapter.MyViewHolder, position: Int) {
        holder.txtUrgentRequestTitle!!.text = urgentRequestList.get(position).customerIssueTitle
        holder.txtUrgentRequestDescription!!.text = urgentRequestList.get(position).customerIssueDescription
        holder.txtUrgentRequest!!.text = urgentRequestList.get(position).status
        Glide.with(context).load(urgentRequestList.get(position).imageList!!.get(0)).into(holder.imgUrgentRequest!!)

        if (urgentRequestList.get(position).status.equals("Accepted")) {
            holder.btnAccept!!.visibility = View.GONE
            holder.btnReject!!.visibility = View.GONE
            holder.btnLocation!!.visibility = View.GONE
            holder.btnComplete!!.visibility = View.VISIBLE
        } else if (urgentRequestList.get(position).status.equals("Rejected")) {
            holder.btnAccept!!.visibility = View.GONE
            holder.btnReject!!.visibility = View.GONE
            holder.btnLocation!!.visibility = View.GONE
            holder.animationView!!.visibility = View.GONE
        } else if (urgentRequestList.get(position).status.equals("Completed")) {
            holder.btnAccept!!.visibility = View.GONE
            holder.btnReject!!.visibility = View.GONE
            holder.btnLocation!!.visibility = View.GONE
            holder.btnComplete!!.visibility = View.GONE
            holder.animationView!!.visibility = View.GONE
        } else {
            holder.btnAccept!!.visibility = View.VISIBLE
            holder.btnReject!!.visibility = View.VISIBLE
            holder.btnLocation!!.visibility = View.VISIBLE
        }

        holder.btnLocation!!.setOnClickListener {
            startActivity(context, Intent(context, UrgentRequestDetails::class.java).putExtra("key", urgentRequestList.get(position).key), null)
        }

        holder.cardUrgentRequest!!.setOnClickListener {
            startActivity(context, Intent(context, UrgentRequestDetails::class.java).putExtra("key", urgentRequestList.get(position).key), null)
        }

        holder.btnAccept!!.setOnClickListener {
            FirebaseDatabase.getInstance().getReference(Common.REQUEST_REF).child(urgentRequestList.get(position).key!!).child("status").setValue("Accepted")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Request Accepted", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Request Accepted Failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        holder.btnReject!!.setOnClickListener {
            FirebaseDatabase.getInstance().getReference(Common.REQUEST_REF).child(urgentRequestList.get(position).key!!).child("status").setValue("Rejected")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Request Rejected", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Request Rejected Failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        holder.btnComplete!!.setOnClickListener {
            startActivity(context, Intent(context, RequestComplete::class.java).putExtra("key", urgentRequestList.get(position).key), null)
        }
    }

    override fun getItemCount(): Int {
        return urgentRequestList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var imgUrgentRequest: ImageView? = null
        var txtUrgentRequestTitle: TextView? = null
        var txtUrgentRequestDescription: TextView? = null
        var txtUrgentRequest: TextView? = null
        var btnLocation: Button? = null
        var btnAccept: Button? = null
        var btnReject: Button? = null
        var btnComplete: Button? = null
        var cardUrgentRequest: MaterialCardView? = null
        var animationView: LottieAnimationView? = null

        init {
            imgUrgentRequest = itemView.findViewById(R.id.imgUrgentRequest) as ImageView
            txtUrgentRequestTitle = itemView.findViewById(R.id.txtUrgentRequestTitle) as TextView
            txtUrgentRequestDescription = itemView.findViewById(R.id.txtUrgentRequestDescription) as TextView
            txtUrgentRequest = itemView.findViewById(R.id.txtUrgentRequest) as TextView
            btnLocation = itemView.findViewById(R.id.btnLocation) as Button
            btnAccept = itemView.findViewById(R.id.btnAccept) as Button
            btnReject = itemView.findViewById(R.id.btnReject) as Button
            btnComplete = itemView.findViewById(R.id.btnComplete) as Button
            cardUrgentRequest = itemView.findViewById(R.id.cardUrgentRequest) as MaterialCardView
            animationView = itemView.findViewById(R.id.animationView) as LottieAnimationView

        }
    }
}