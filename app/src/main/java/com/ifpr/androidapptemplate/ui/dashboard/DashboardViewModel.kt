package com.ifpr.androidapptemplate.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ifpr.androidapptemplate.baseclasses.Event

class DashboardViewModel : ViewModel() {

    // LiveData para acionar a exibição do diálogo de filtro como um evento
    private val _showFilterDialog = MutableLiveData<Event<Unit>>()
    val showFilterDialog: LiveData<Event<Unit>> = _showFilterDialog

    /**
     * Chamado quando o botão de filtro é clicado na MainActivity.
     */
    fun onFilterFabClicked() {
        // Posta um evento para o fragmento observar
        _showFilterDialog.value = Event(Unit)
    }

    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text
}
