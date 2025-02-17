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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import ch.qos.logback.classic.Level
import com.gs.erp.sap.demo.R
import com.gs.erp.sap.demo.repository.SharedPreferenceRepository
import com.sap.cloud.mobile.foundation.logging.LoggingService
import com.sap.cloud.mobile.foundation.mobileservices.SDKInitializer
import com.sap.cloud.mobile.foundation.mobileservices.ServiceListener
import com.sap.cloud.mobile.foundation.mobileservices.ServiceResult
import com.sap.cloud.mobile.foundation.settings.policies.LogPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

data class SettingUIState(
    val level: Level = Level.OFF,
    val consentUsageCollection: Boolean = true,
    val consentCrashReportCollection: Boolean = true
)

class SettingsViewModel (application: Application) : BaseOperationViewModel(application) {
    private val sharedPreferenceRepository = SharedPreferenceRepository(getApplication())
    private val preferencesFlow = sharedPreferenceRepository.userPreferencesFlow

    val supportLogging = SDKInitializer.getService(LoggingService::class) != null

    private val _settingUIState = MutableStateFlow(SettingUIState())
    val settingUIState = _settingUIState.asStateFlow()

    init {
        //init from shared preference
        viewModelScope.launch(Dispatchers.Default) {
            preferencesFlow.collect { userPreference ->
                logger.debug("get preference as ${userPreference.logSetting}")
                _settingUIState.update { uiState ->
                    uiState.copy(level = LogPolicy.getLogLevel(userPreference.logSetting))
                }
            }
        }

    }

    fun updateLogLevel(level: Level) {
        viewModelScope.launch(Dispatchers.IO) {
            logger.debug("update preference as $level")
            sharedPreferenceRepository.updateLogLevel(level)
            SDKInitializer.getService(LoggingService::class)?.also { loggingService ->
                val policy = loggingService.policy
                loggingService.policy = policy.copy(logLevel = LogPolicy.getLogLevelString(level))
            }
        }
    }

    fun uploadLog(lifecycleOwner: LifecycleOwner) {
        operationStart()
        SDKInitializer.getService(LoggingService::class)?.also { logging ->
            logging.upload(owner = lifecycleOwner, listener = object : ServiceListener<Boolean> {
                override fun onServiceDone(result: ServiceResult<Boolean>){
                    if(result is ServiceResult.SUCCESS) {
                        logger.debug("Log is uploaded to the server.")
                        operationFinished(
                            result = OperationResult.OperationSuccess(
                                getApplication<Application>().getString(R.string.log_upload_ok),
                                OperationType.UPLOAD_LOG
                            )
                        )
                    } else {
                        val message: String = (result as ServiceResult.FAILURE).message
                        logger.debug("Log upload failed with error message $message")
                        operationFinished(result = OperationResult.OperationFail(message, OperationType.UPLOAD_LOG))
                    }
                }
            })
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SettingsViewModel::class.java)
    }
}
