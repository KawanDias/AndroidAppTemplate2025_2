package com.ifpr.androidapptemplate.ui.notifications

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ifpr.androidapptemplate.databinding.FragmentNotificationsBinding
import com.ifpr.androidapptemplate.model.Imovel
import com.ifpr.androidapptemplate.model.Notification
import com.ifpr.androidapptemplate.ui.imovel.ImovelDetailActivity

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

        binding.buttonRemoveAll.setOnClickListener {
            deleteAllNotifications()
        }

        return root
    }

    private fun setupRecyclerView() {
        val recyclerView = binding.notificationsRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        notificationsViewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            if (notifications != null) {
                recyclerView.adapter = NotificationAdapter(notifications, {
                    notification -> handleNotificationClick(notification)
                }, {
                    notification -> deleteNotification(notification)
                })
            }
        }
    }

    private fun handleNotificationClick(notification: Notification) {
        notification.imovelId?.let { imovelId ->
            val imovelRef = FirebaseDatabase.getInstance().getReference("destaques").child(imovelId)
            imovelRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val imovel = snapshot.getValue(Imovel::class.java)
                    if (imovel != null) {
                        val intent = Intent(requireContext(), ImovelDetailActivity::class.java)
                        intent.putExtra("IMOVEL_EXTRA", imovel)
                        startActivity(intent)
                    } else {
                        Toast.makeText(context, "Imóvel Indisponível", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Erro ao buscar detalhes do imóvel.", Toast.LENGTH_SHORT).show()
                }
            })
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

    private fun deleteAllNotifications() {
        if (userId != null) {
            val notificationsRef = FirebaseDatabase.getInstance().getReference("notifications")
            notificationsViewModel.notifications.value?.forEach { notification ->
                if (notification.id != null) {
                    notificationsRef.child(notification.id).child("deletedBy").child(userId).setValue(true)
                }
            }
            if (isAdded) {
                Toast.makeText(context, "Todas as notificações foram removidas.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
