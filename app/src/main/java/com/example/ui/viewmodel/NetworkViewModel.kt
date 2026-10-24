package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.GeneratedVoucher
import com.example.data.local.entity.NetworkDevice
import com.example.data.local.entity.RouterConfig
import com.example.data.mikrotik.NetworkRepository
import com.example.data.model.HotspotActiveUser
import com.example.data.model.HotspotUser
import com.example.data.model.SystemResource
import com.example.data.model.TrafficStats
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenDestination {
    DASHBOARD,
    USERS,
    VOUCHERS,
    ACTIVE_USERS,
    PROFILES,
    NETWORK_MAP,
    SETTINGS
}

data class VoucherGeneratorState(
    val quantity: Int = 10,
    val userMode: String = "same", // "same" or "different"
    val prefix: String = "net",
    val length: Int = 4,
    val selectedProfileIndex: Int = 0,
    val timeLimit: String = "1d",
    val dataLimit: String = "",
    val comment: String = "",
    val isGenerating: Boolean = false,
    val lastGeneratedVouchers: List<GeneratedVoucher> = emptyList()
)

class NetworkViewModel(
    private val repository: NetworkRepository
) : ViewModel() {

    private val _currentScreen = MutableStateFlow(ScreenDestination.DASHBOARD)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    private val _userSearchQuery = MutableStateFlow("")
    val userSearchQuery: StateFlow<String> = _userSearchQuery.asStateFlow()

    private val _selectedProfileFilter = MutableStateFlow<String?>(null)
    val selectedProfileFilter: StateFlow<String?> = _selectedProfileFilter.asStateFlow()

    val allRouters: StateFlow<List<RouterConfig>> = repository.allRouters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeRouter: StateFlow<RouterConfig?> = repository.activeRouterConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isLiveConnected: StateFlow<Boolean> = repository.isLiveConnected
    val isConnecting: StateFlow<Boolean> = repository.isConnecting
    val connectionStatusMessage: StateFlow<String> = repository.connectionStatusMessage

    val systemResource: StateFlow<SystemResource> = repository.systemResource
    val trafficStats: StateFlow<TrafficStats> = repository.trafficStats
    val activeUsers: StateFlow<List<HotspotActiveUser>> = repository.activeUsers
    val profiles: StateFlow<List<UserProfile>> = repository.profiles

    val allVouchers: StateFlow<List<GeneratedVoucher>> = repository.allVouchers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNetworkDevices: StateFlow<List<NetworkDevice>> = repository.allNetworkDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _generatorState = MutableStateFlow(VoucherGeneratorState())
    val generatorState: StateFlow<VoucherGeneratorState> = _generatorState.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    // Filtered users
    val filteredUsers: StateFlow<List<HotspotUser>> = combine(
        repository.hotspotUsers,
        _userSearchQuery,
        _selectedProfileFilter
    ) { users, query, profile ->
        users.filter { user ->
            val matchQuery = query.isBlank() ||
                    user.name.contains(query, ignoreCase = true) ||
                    user.comment.contains(query, ignoreCase = true)
            val matchProfile = profile == null || user.profile == profile
            matchQuery && matchProfile
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            activeRouter.collect { config ->
                if (config != null) {
                    repository.connectToRouter(config)
                }
            }
        }
    }

    fun navigateTo(screen: ScreenDestination) {
        _currentScreen.value = screen
    }

    fun setUserSearchQuery(query: String) {
        _userSearchQuery.value = query
    }

    fun setProfileFilter(profile: String?) {
        _selectedProfileFilter.value = profile
    }

    fun reconnect() {
        activeRouter.value?.let { config ->
            viewModelScope.launch {
                repository.connectToRouter(config)
                _toastMessage.emit("جاري إعادة الاتصال بـ ${config.name}")
            }
        }
    }

    fun selectRouter(router: RouterConfig) {
        viewModelScope.launch {
            repository.selectActiveRouter(router)
            _toastMessage.emit("تم تبديل الراوتر النشط إلى: ${router.name}")
        }
    }

    fun saveRouter(router: RouterConfig) {
        viewModelScope.launch {
            repository.saveRouter(router)
            _toastMessage.emit("تم حفظ بيانات الراوتر بنجاح")
        }
    }

    fun deleteRouter(router: RouterConfig) {
        viewModelScope.launch {
            repository.deleteRouter(router)
            _toastMessage.emit("تم حذف الراوتر")
        }
    }

    fun addHotspotUser(
        name: String,
        pass: String,
        profile: String,
        timeLimit: String,
        dataLimitBytes: Long,
        comment: String,
        price: String
    ) {
        viewModelScope.launch {
            repository.addHotspotUser(name, pass, profile, timeLimit, dataLimitBytes, comment, price)
            _toastMessage.emit("تمت إضافة المستخدم $name بنجاح")
        }
    }

    fun removeUser(user: HotspotUser) {
        viewModelScope.launch {
            repository.removeHotspotUser(user)
            _toastMessage.emit("تم حذف المستخدم ${user.name}")
        }
    }

    fun toggleUserDisabled(user: HotspotUser) {
        viewModelScope.launch {
            repository.toggleUserDisabled(user)
            val action = if (user.disabled) "تفعيل" else "تعطيل"
            _toastMessage.emit("تم $action المستخدم ${user.name}")
        }
    }

    fun resetUserCounters(user: HotspotUser) {
        viewModelScope.launch {
            repository.resetUserCounters(user)
            _toastMessage.emit("تم تصفير عدادات المستخدم ${user.name}")
        }
    }

    fun disconnectActiveUser(activeUser: HotspotActiveUser) {
        viewModelScope.launch {
            repository.disconnectActiveUser(activeUser)
            _toastMessage.emit("تم قطع اتصال ${activeUser.user}")
        }
    }

    // Voucher Generator form setters
    fun updateGeneratorQuantity(qty: Int) {
        _generatorState.value = _generatorState.value.copy(quantity = qty)
    }

    fun updateGeneratorUserMode(mode: String) {
        _generatorState.value = _generatorState.value.copy(userMode = mode)
    }

    fun updateGeneratorPrefix(prefix: String) {
        _generatorState.value = _generatorState.value.copy(prefix = prefix)
    }

    fun updateGeneratorLength(length: Int) {
        _generatorState.value = _generatorState.value.copy(length = length)
    }

    fun updateGeneratorProfileIndex(index: Int) {
        _generatorState.value = _generatorState.value.copy(selectedProfileIndex = index)
    }

    fun updateGeneratorTimeLimit(limit: String) {
        _generatorState.value = _generatorState.value.copy(timeLimit = limit)
    }

    fun updateGeneratorDataLimit(limit: String) {
        _generatorState.value = _generatorState.value.copy(dataLimit = limit)
    }

    fun updateGeneratorComment(comment: String) {
        _generatorState.value = _generatorState.value.copy(comment = comment)
    }

    fun generateVouchers() {
        val currentState = _generatorState.value
        val profileList = profiles.value
        if (profileList.isEmpty()) {
            viewModelScope.launch {
                _toastMessage.emit("يرجى إضافة باقة اشتراك أولاً")
            }
            return
        }

        val selectedProfile = profileList.getOrNull(currentState.selectedProfileIndex) ?: profileList.first()

        viewModelScope.launch {
            _generatorState.value = currentState.copy(isGenerating = true)
            val vouchers = repository.generateBatchVouchers(
                quantity = currentState.quantity,
                userMode = currentState.userMode,
                prefix = currentState.prefix,
                length = currentState.length,
                profile = selectedProfile,
                timeLimit = currentState.timeLimit,
                dataLimit = currentState.dataLimit,
                comment = currentState.comment
            )
            _generatorState.value = currentState.copy(
                isGenerating = false,
                lastGeneratedVouchers = vouchers
            )
            _toastMessage.emit("تم بنجاح توليد ${vouchers.size} كارت لـ ${selectedProfile.name}")
        }
    }

    fun addProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.addProfile(profile)
            _toastMessage.emit("تمت إضافة الباقة ${profile.name}")
        }
    }

    fun rebootRouter() {
        viewModelScope.launch {
            val success = repository.rebootRouter()
            if (success) {
                _toastMessage.emit("تم إرسال أمر إعادة تشغيل الراوتر")
            } else {
                _toastMessage.emit("فشل إرسال أمر إعادة التشغيل")
            }
        }
    }

    fun saveNetworkDevice(device: NetworkDevice) {
        viewModelScope.launch {
            repository.saveNetworkDevice(device)
            _toastMessage.emit("تم حفظ الجهاز: ${device.name}")
        }
    }

    fun deleteNetworkDevice(device: NetworkDevice) {
        viewModelScope.launch {
            repository.deleteNetworkDevice(device)
            _toastMessage.emit("تم حذف الجهاز")
        }
    }
}
