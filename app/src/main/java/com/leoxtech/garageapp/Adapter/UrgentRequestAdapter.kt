package com.leoxtech.garageapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.leoxtech.garageapp.Model.RequestHelpModel
import com.leoxtech.garageapp.R

class UrgentRequestAdapter (internal var context: Context, private var urgentRequestList: List<RequestHelpModel>) : RecyclerView.Adapter<UrgentRequestAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UrgentRequestAdapter.MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.urgent_request_item, parent, false))
    }

    override fun onBindViewHolder(holder: UrgentRequestAdapter.MyViewHolder, position: Int) {
        holder.txtUrgentRequestTitle!!.text = urgentRequestList.get(position).customerIssueTitle
        holder.txtUrgentRequestDescription!!.text = urgentRequestList.get(position).customerIssueDescription
        Glide.with(context).load(urgentRequestList.get(position).imageList!!.get(0)).into(holder.imgUrgentRequest!!)
    }

    override fun getItemCount(): Int {
        return urgentRequestList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var imgUrgentRequest: ImageView? = null
        var txtUrgentRequestTitle: TextView? = null
        var txtUrgentRequestDescription: TextView? = null
        private var btnLocation: Button? = null
        private var btnAccept: Button? = null
        private var btnReject: Button? = null

        init {
            imgUrgentRequest = itemView.findViewById(R.id.imgUrgentRequest) as ImageView
            txtUrgentRequestTitle = itemView.findViewById(R.id.txtUrgentRequestTitle) as TextView
            txtUrgentRequestDescription = itemView.findViewById(R.id.txtUrgentRequestDescription) as TextView
            btnLocation = itemView.findViewById(R.id.btnLocation) as Button
            btnAccept = itemView.findViewById(R.id.btnAccept) as Button
            btnReject = itemView.findViewById(R.id.btnReject) as Button

        }
    }
}