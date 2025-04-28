package com.redstar.redefinencm.viewmodel

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redstar.redefinencm.api.NCMApi
import com.redstar.redefinencm.api.RetrofitInstance
import com.redstar.redefinencm.util.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    var server by mutableStateOf("")
    var cookie by mutableStateOf("")
    val retrofit: NCMApi = RetrofitInstance.retrofit.create(NCMApi::class.java)

    var cookieLoginLoading by mutableStateOf(false)
    var cookieLoginErrorMessage by mutableStateOf("")

    var qrLoginBitmap by mutableStateOf<Bitmap?>(null)
    var qrLoginUnikey by mutableStateOf("")
    var qrLoginScanStatus by mutableStateOf("Generating Code")

    init {
        loadServer()
        loadCookie()
    }

    private fun loadServer() {
        viewModelScope.launch {
            val value = DataStoreManager.getAppDataStore().data.first()[stringPreferencesKey("server")] ?: ""
            server = value
        }
    }

    private fun loadCookie() {
        viewModelScope.launch {
            val value = DataStoreManager.getAppDataStore().data.first()[stringPreferencesKey("cookie")] ?: ""
            cookie = value
        }
    }

    fun updateDatastore(key: String, value: String) {
        viewModelScope.launch {
            DataStoreManager.getAppDataStore().edit { preferences ->
                preferences[stringPreferencesKey(key)] = value
            }
        }
    }
}
