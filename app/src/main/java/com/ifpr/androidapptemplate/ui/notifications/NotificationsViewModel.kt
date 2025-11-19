package com.ifpr.androidapptemplate.ui.notifications

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ifpr.androidapptemplate.model.Notification

class NotificationsViewModel : ViewModel() {

    private val _notifications = MutableLiveData<List<Notification>>()
    val notifications: LiveData<List<Notification>> = _notifications

    private val notificationsRef = FirebaseDatabase.getInstance().getReference("notifications")
    private var notificationsListener: ValueEventListener? = null
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        notificationsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notificationList = mutableListOf<Notification>()
                for (notificationSnapshot in snapshot.children) {
                    val notification = notificationSnapshot.getValue(Notification::class.java)
                    if (notification != null && userId != null && !notification.deletedBy.containsKey(userId)) {
                        notificationList.add(notification)
                    }
                }
                _notifications.value = notificationList.reversed()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("NotificationsViewModel", "Falha ao carregar notificações", error.toException())
            }
        }
        notificationsRef.orderByChild("timestamp").addValueEventListener(notificationsListener!!)
    }

    override fun onCleared() {
        super.onCleared()
        notificationsListener?.let {
            notificationsRef.removeEventListener(it)
        }
    }
}
