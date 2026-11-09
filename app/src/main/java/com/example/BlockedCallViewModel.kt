package com.example

import android.app.Application
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BlockedCall
import com.example.data.BlockedCallRepository
import com.example.data.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AppTab {
    HOME,
    CONTACTS,
    BLOCKED,
    SETTINGS
}

data class ContactItem(
    val name: String,
    val number: String
)

class BlockedCallViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: BlockedCallRepository
    private val prefs = PreferencesManager.getInstance(application)

    val blockedCalls: StateFlow<List<BlockedCall>>

    private val _isProtectionActive = MutableStateFlow(prefs.isProtectionActive)
    val isProtectionActive: StateFlow<Boolean> = _isProtectionActive.asStateFlow()

    private val _selectedTab = MutableStateFlow(AppTab.HOME)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    private val _contactsCount = MutableStateFlow(24) // Default fallback matches mockup
    val contactsCount: StateFlow<Int> = _contactsCount.asStateFlow()

    private val _contactsList = MutableStateFlow<List<ContactItem>>(emptyList())
    val contactsList: StateFlow<List<ContactItem>> = _contactsList.asStateFlow()

    private val _totalFilteredCalls = MutableStateFlow(prefs.totalCallsScreened)
    val totalFilteredCalls: StateFlow<Int> = _totalFilteredCalls.asStateFlow()

    private val _isCallSimulationVisible = MutableStateFlow(false)
    val isCallSimulationVisible: StateFlow<Boolean> = _isCallSimulationVisible.asStateFlow()

    private val _simulatedCallerNumber = MutableStateFlow("(11) 98765-4321")
    val simulatedCallerNumber: StateFlow<String> = _simulatedCallerNumber.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = BlockedCallRepository(database.blockedCallDao())
        blockedCalls = repository.allBlockedCalls.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        refreshContacts()
    }

    fun selectTab(tab: AppTab) {
        _selectedTab.value = tab
    }

    fun setProtectionActive(active: Boolean) {
        prefs.isProtectionActive = active
        _isProtectionActive.value = active
    }

    fun refreshContacts() {
        viewModelScope.launch {
            if (ContextCompat.checkSelfPermission(
                    getApplication(),
                    android.Manifest.permission.READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val list = withContext(Dispatchers.IO) {
                    val contacts = mutableListOf<ContactItem>()
                    val contentResolver = getApplication<Application>().contentResolver
                    val cursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                        ),
                        null,
                        null,
                        "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
                    )
                    cursor?.use {
                        val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                        val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        while (it.moveToNext()) {
                            val name = if (nameIndex >= 0) it.getString(nameIndex) ?: "" else ""
                            val number = if (numberIndex >= 0) it.getString(numberIndex) ?: "" else ""
                            if (name.isNotBlank()) {
                                contacts.add(ContactItem(name, number))
                            }
                        }
                    }
                    contacts
                }
                if (list.isNotEmpty()) {
                    _contactsList.value = list
                    _contactsCount.value = list.size
                } else {
                    _contactsCount.value = 24
                }
            } else {
                _contactsCount.value = 24
            }
        }
    }

    fun showCallSimulation(number: String = "(11) 98765-4321") {
        _simulatedCallerNumber.value = number
        _isCallSimulationVisible.value = true
    }

    fun dismissCallSimulation() {
        _isCallSimulationVisible.value = false
    }

    fun blockAndRejectSimulatedCall() {
        val number = _simulatedCallerNumber.value
        viewModelScope.launch {
            repository.insert(
                BlockedCall(
                    phoneNumber = number,
                    timestamp = System.currentTimeMillis(),
                    reason = "Chamada indesejada (bloqueada pelo SpamBlocker)"
                )
            )
            val updatedTotal = prefs.incrementCallsScreened()
            _totalFilteredCalls.value = updatedTotal
            _isCallSimulationVisible.value = false
        }
    }

    fun deleteBlockedCall(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun clearAllBlockedCalls() {
        viewModelScope.launch {
            repository.clearAll()
            prefs.resetCallsScreened()
            _totalFilteredCalls.value = 0
        }
    }

    fun addSimulatedCall(number: String = "+55 (11) 9${(1000..9999).random()}-${(1000..9999).random()}") {
        viewModelScope.launch {
            repository.insert(
                BlockedCall(
                    phoneNumber = number,
                    timestamp = System.currentTimeMillis(),
                    reason = "Fora da lista de contatos"
                )
            )
            val updatedTotal = prefs.incrementCallsScreened()
            _totalFilteredCalls.value = updatedTotal
        }
    }
}
