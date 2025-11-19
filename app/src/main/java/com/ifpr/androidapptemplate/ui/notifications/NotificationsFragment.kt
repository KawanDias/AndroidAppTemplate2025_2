package com.ifpr.androidapptemplate.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ifpr.androidapptemplate.databinding.FragmentNotificationsBinding
import com.ifpr.androidapptemplate.model.Notification

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var notificationsViewModel: NotificationsViewModel
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        notificationsViewModel = ViewModelProvider(this).get(NotificationsViewModel::class.java)
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()

        return root
    }

    private fun setupRecyclerView() {
        val recyclerView = binding.notificationsRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        notificationsViewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            if (notifications != null) {
                recyclerView.adapter = NotificationAdapter(notifications) { notification ->
                    deleteNotification(notification)
                }
            }
        }
    }

    private fun deleteNotification(notification: Notification) {
        if (userId != null && notification.id != null) {
            val notificationRef = FirebaseDatabase.getInstance().getReference("notifications").child(notification.id)
            notificationRef.child("deletedBy").child(userId).setValue(true)
                .addOnSuccessListener {
                    // A notificação foi marcada como excluída para o usuário atual
                    // O LiveData no ViewModel irá atualizar a UI automaticamente
                    if (isAdded) {
                        Toast.makeText(context, "Notificação removida.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    if (isAdded) {
                        Toast.makeText(context, "Falha ao remover notificação.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
