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
package com.beesechurgers.parker

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beesechurgers.parker.utils.DatabaseConstants
import com.beesechurgers.parker.utils.PayHistoryAdapter
import com.beesechurgers.parker.utils.Utils.valueEvenListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_history.*

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "FATAL: User NULL", Toast.LENGTH_SHORT).show()
            super.onBackPressed()
            return
        }

        payment_history_view.setHasFixedSize(true)
        payment_history_view.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        initList(user.uid)
    }

    private fun initList(uid: String) {
        FirebaseDatabase.getInstance().getReference(DatabaseConstants.HISTORY).child(uid).valueEvenListener(onDataChange = {
            val list = ArrayList<PayHistoryAdapter.PaymentHistoryItem>()
            for (child in it.children) {
                list.add(PayHistoryAdapter.PaymentHistoryItem(child.key.toString().toLong(),
                    child.child(DatabaseConstants.EXITED_TIME).value.toString().toLong(),
                    child.child(DatabaseConstants.ENTERED_TIME).value.toString().toLong(),
                    child.child(DatabaseConstants.PAYMENT).child(DatabaseConstants.PAYMENT_AMOUNT).value.toString()))
            }
            if (list.isEmpty()) {
                empty_history_text.visibility = View.VISIBLE
            }

            payment_history_view.adapter = null
            payment_history_view.adapter = PayHistoryAdapter(list)
        })
    }
}