// Copyright (c) 2025 Grandstream
// 
// Licensed under the Apache License, Version 2.0 (the \"License\");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//   http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an \"AS IS\" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.gs.erp.sap.demo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class OperationType {
    UPLOAD_LOG, UPLOAD_USAGE_DATA
}

sealed class OperationResult() {
    data class OperationSuccess(val message: String, val operationType: OperationType) : OperationResult()
    data class OperationFail(val message: String, val operationType: OperationType) : OperationResult()
}

data class OperationUIState(
    val inProgress: Boolean = false,
    val progress: Float? = null,
    val result: OperationResult? = null,
)

open class BaseOperationViewModel(application: Application) : AndroidViewModel(application){
    protected val _operationUIState = MutableStateFlow(OperationUIState())
    val operationUIState = _operationUIState.asStateFlow()

    fun resetOperationState() {
        _operationUIState.update { OperationUIState() }
    }

    fun operationFinished(result: OperationResult) {
        _operationUIState.update { OperationUIState(result = result) }
    }

    fun operationStart() {
        _operationUIState.update { OperationUIState(inProgress = true) }
    }
}
