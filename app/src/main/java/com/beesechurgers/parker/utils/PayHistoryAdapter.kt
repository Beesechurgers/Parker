/*
 * Copyright (c) 2021, Beesechurgers <https://github.com/Beesechurgers>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 */
package com.beesechurgers.parker.utils

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.beesechurgers.parker.R
import com.beesechurgers.parker.utils.PayHistoryAdapter.PaymentHistoryViewHolder
import com.beesechurgers.parker.utils.Utils.formatAmount
import java.text.DateFormat
import java.util.*

class PayHistoryAdapter(private val list: List<PaymentHistoryItem>) : RecyclerView.Adapter<PaymentHistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentHistoryViewHolder =
        PaymentHistoryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.payment_history_item_layout, parent, false))

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PaymentHistoryViewHolder, position: Int) {
        val item = list[position]
        val calendar = Calendar.getInstance().apply { this.timeInMillis = item.seconds * 1000 }
        holder.dateTime.text = DateFormat.getTimeInstance(DateFormat.SHORT).format(calendar.time) +
            " " + DateFormat.getDateInstance(DateFormat.FULL).format(calendar.time)

        holder.amount.text = "Amount: \u20B9 ${item.amount.formatAmount()}"

        holder.timeElapsed.text = "Time Elapsed: ${(item.endTime - item.enterTime) / 60} min(s)"
    }

    override fun getItemCount(): Int = list.size


    inner class PaymentHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTime: AppCompatTextView = itemView.findViewById(R.id.history_date_time)
        val timeElapsed: AppCompatTextView = itemView.findViewById(R.id.history_time_elapsed)
        val amount: AppCompatTextView = itemView.findViewById(R.id.history_amount)
    }

    data class PaymentHistoryItem(var seconds: Long, var endTime: Long, var enterTime: Long, var amount: String)
}