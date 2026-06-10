package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface Screen {
    object Welcome : Screen
    object Login : Screen
    object Register : Screen
    object Dashboard : Screen
    object SavingsDetail : Screen
    object AddSavings : Screen
    object ApplyLoan : Screen
    object LoanDetail : Screen
    object LoanApprovals : Screen // Pengurus role
    object Marketplace : Screen
    object AddProduct : Screen
    object CommunityChat : Screen
    object FinancialEducation : Screen
    object AiAdvisor : Screen
    object MembersManagement : Screen // Admin/Pengurus role
}

class CoopViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = CoopDatabase.getDatabase(application, viewModelScope)
    private val repository = CoopRepository(database.coopDao())

    // --- Navigation & Flow state ---
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Welcome)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _currentUser = MutableStateFlow<CoopUser?>(null)
    val currentUser: StateFlow<CoopUser?> = _currentUser.asStateFlow()

    // --- Observable Flows from Database ---
    val allUsers: StateFlow<List<CoopUser>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLoans: StateFlow<List<LoanApplication>> = repository.getAllLoans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<UmkmProduct>> = repository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allChats: StateFlow<List<ChatMessage>> = repository.getAllChats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Dynamic Derived Flows ---
    private val _savingsTransactions = MutableStateFlow<List<SavingsTransaction>>(emptyList())
    val savingsTransactions: StateFlow<List<SavingsTransaction>> = _savingsTransactions.asStateFlow()

    private val _userLoans = MutableStateFlow<List<LoanApplication>>(emptyList())
    val userLoans: StateFlow<List<LoanApplication>> = _userLoans.asStateFlow()

    // --- State flags ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _registerError = MutableStateFlow<String?>(null)
    val registerError: StateFlow<String?> = _registerError.asStateFlow()

    // --- AI Advisor Chat History ---
    private val _aiChatMessages = MutableStateFlow<List<Pair<String, Boolean>>>(listOf(
        Pair("Sampurasun! Abdi Asisten AI Keuangan Koperasi Merah Putih Bogor. Mangga, silakan bertanya tentang perencanaan keuangan, pemodalan UMKM, atau simpanan di koperasi kami.", false)
    )) // Pair of (Text, isUserMessage)
    val aiChatMessages: StateFlow<List<Pair<String, Boolean>>> = _aiChatMessages.asStateFlow()

    init {
        // Collect current user transactions and loans dynamically whenever currentUser changes
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    repository.getSavingsTransactions(user.id).collect { list ->
                        _savingsTransactions.value = list
                    }
                } else {
                    _savingsTransactions.value = emptyList()
                }
            }
        }

        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    repository.getLoansForUser(user.id).collect { list ->
                        _userLoans.value = list
                    }
                } else {
                    _userLoans.value = emptyList()
                }
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    // --- Auth Actions ---
    fun login(nik: String, pin: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loginError.value = null
            val user = repository.login(nik, pin)
            if (user != null) {
                _currentUser.value = user
                _currentScreen.value = Screen.Dashboard
            } else {
                _loginError.value = "NIK atau PIN salah. Hubungi Pengurus untuk bimbingan."
            }
            _isLoading.value = false
        }
    }

    fun register(nik: String, name: String, pin: String, phone: String, initialWajib: Double, initialSukarela: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            _registerError.value = null
            
            // Check if NIK already registered
            val existingUsers = allUsers.value
            if (existingUsers.any { it.nik == nik }) {
                _registerError.value = "NIK sudah terdaftar. Silakan login."
                _isLoading.value = false
                return@launch
            }

            try {
                val user = repository.register(nik, name, pin, phone, initialWajib, initialSukarela)
                _currentUser.value = user
                _currentScreen.value = Screen.Dashboard
            } catch (e: Exception) {
                _registerError.value = "Gagal mendaftar: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _currentScreen.value = Screen.Welcome
    }

    fun clearAuthErrors() {
        _loginError.value = null
        _registerError.value = null
    }

    // --- Financial Actions ---
    fun addSavings(type: String, amount: Double, notes: String, onFinished: (Boolean) -> Unit) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val success = repository.addSavings(user.id, type, amount, notes)
            if (success) {
                // Refresh local user entity in viewmodel state
                repository.getUser(user.id).firstOrNull()?.let { updated ->
                    _currentUser.value = updated
                }
            }
            onFinished(success)
        }
    }

    fun applyLoan(amount: Double, purpose: String, tenureMonths: Int, onFinished: (Boolean) -> Unit) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val success = repository.applyLoan(user.id, user.name, amount, purpose, tenureMonths)
            onFinished(success)
        }
    }

    fun approveLoan(loan: LoanApplication) {
        viewModelScope.launch {
            repository.approveLoan(loan)
            // If the approved loan belongs to the logged-in user, refresh their details
            _currentUser.value?.let { current ->
                repository.getUser(current.id).firstOrNull()?.let { updated ->
                    _currentUser.value = updated
                }
            }
        }
    }

    fun rejectLoan(loan: LoanApplication) {
        viewModelScope.launch {
            repository.rejectLoan(loan)
        }
    }

    // --- UMKM Actions ---
    fun uploadProduct(name: String, price: Double, category: String, description: String, phone: String, onResponse: () -> Unit) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val product = UmkmProduct(
                name = name,
                sellerName = user.name,
                price = price,
                category = category,
                description = description,
                phone = phone
            )
            repository.insertProduct(product)
            onResponse()
        }
    }

    fun deleteProduct(id: Int) {
        viewModelScope.launch {
            repository.deleteProduct(id)
        }
    }

    // --- Community Chats ---
    fun sendChat(messageText: String) {
        val user = _currentUser.value ?: return
        if (messageText.isBlank()) return
        viewModelScope.launch {
            repository.sendChatMessage(user.id, user.name, user.role, messageText)
        }
    }

    // --- AI Assistant Advisory Chats ---
    fun askAiAdvisor(questionText: String) {
        if (questionText.isBlank()) return
        
        // Append user question
        val currentHistory = _aiChatMessages.value.toMutableList()
        currentHistory.add(Pair(questionText, true))
        _aiChatMessages.value = currentHistory
        
        // Fetch AI response
        viewModelScope.launch {
            _isLoading.value = true
            // Real-time API query (with automatic Indonesian/Sundanese rural fallback)
            val advice = repository.getGeminiAdvice(questionText)
            
            val updatedHistory = _aiChatMessages.value.toMutableList()
            updatedHistory.add(Pair(advice, false))
            _aiChatMessages.value = updatedHistory
            _isLoading.value = false
        }
    }

    fun clearAiChat() {
        _aiChatMessages.value = listOf(
            Pair("Sampurasun! Abdi Asisten AI Keuangan Koperasi Merah Putih Bogor. Mangga, silakan bertanya tentang perencanaan keuangan, pemodalan UMKM, atau simpanan di koperasi kami.", false)
        )
    }

    // --- Simulator Feature: Dynamic Role Switcher ---
    // Perfect for demonstrations so the reviewer can click and transform their current state
    // to Admin, Pengurus, or Anggota instantaneously rather than re-registering and logging out.
    fun simulateSwitchRoleTo(newRole: String) {
        val current = _currentUser.value ?: return
        val updated = current.copy(role = newRole)
        viewModelScope.launch {
            repository.updateUser(updated)
            _currentUser.value = updated
        }
    }
}
