package com.ifpr.androidapptemplate.ui.notifications

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        notificationsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notificationList = mutableListOf<Notification>()
                for (notificationSnapshot in snapshot.children) {
                    val notification = notificationSnapshot.getValue(Notification::class.java)
                    if (notification != null) {
                        notificationList.add(notification)
                    }
                }
                // Inverte a lista para mostrar as mais recentes primeiro
                _notifications.value = notificationList.reversed()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("NotificationsViewModel", "Falha ao carregar notificações", error.toException())
            }
        }
        // Ordena por timestamp para pegar as mais recentes
        notificationsRef.orderByChild("timestamp").addValueEventListener(notificationsListener!!)
    }

    override fun onCleared() {
        super.onCleared()
        notificationsListener?.let {
            notificationsRef.removeEventListener(it)
        }
    }
}
