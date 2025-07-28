package com.redstar.redefinencm.viewmodel

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redstar.redefinencm.util.DataStoreManager
import com.redstar.redefinencm.util.SettingProvider
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    var server by mutableStateOf("")
        private set
    var cookie by mutableStateOf(SettingProvider.cookie)
        private set

    var cookieLoginLoading by mutableStateOf(false)
    var cookieLoginErrorMessage by mutableStateOf("")

    var qrLoginBitmap by mutableStateOf<Bitmap?>(null)
    var qrLoginUnikey by mutableStateOf("")
    var qrLoginScanStatus by mutableStateOf("Generating Code")

    init {
        loadServer()
    }

    private fun loadServer() {
        viewModelScope.launch {
            val value =
                DataStoreManager.getStringItem("server", "")
            server = value
        }
    }

    fun updateCookie(newCookie: String) {
        cookie = newCookie
        SettingProvider.updateCookie(newCookie)
    }
}
