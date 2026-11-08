package com.hieu.ui.admin.batch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hieu.core.util.NetworkResult
import com.hieu.core.util.UiState
import com.hieu.data.remote.dto.WasteReportDto
import com.hieu.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AdminWasteReportViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val today = LocalDate.now()
    private val startOfMonth = today.withDayOfMonth(1)

    private val _fromDate = MutableStateFlow(startOfMonth.toString())
    val fromDate: StateFlow<String> = _fromDate.asStateFlow()

    private val _toDate = MutableStateFlow(today.toString())
    val toDate: StateFlow<String> = _toDate.asStateFlow()

    private val _reportState = MutableStateFlow<UiState<WasteReportDto>>(UiState.Loading)
    val reportState: StateFlow<UiState<WasteReportDto>> = _reportState.asStateFlow()

    init {
        loadReport()
    }

    fun onFromDateChanged(from: String) {
        _fromDate.value = from
    }

    fun onToDateChanged(to: String) {
        _toDate.value = to
    }

    fun loadReport() {
        viewModelScope.launch {
            _reportState.value = UiState.Loading
            when (val result = adminRepository.getWasteReport(from = _fromDate.value, to = _toDate.value)) {
                is NetworkResult.Success -> {
                    _reportState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _reportState.value = UiState.Error(result.message)
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }
}

