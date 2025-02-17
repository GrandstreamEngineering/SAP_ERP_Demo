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

package com.gs.erp.sap.demo.service

import com.sap.cloud.mobile.foundation.model.AppConfig
import com.sap.cloud.android.odata.com_sap_mbtepmdemoservice.Com_sap_mbtepmdemoService
import com.sap.cloud.mobile.foundation.common.ClientProvider
import com.sap.cloud.mobile.odata.OnlineODataProvider
import com.sap.cloud.mobile.odata.http.OKHttpHandler

class SAPServiceManager(private val appConfig: AppConfig) {

    var serviceRoot: String = ""
        private set
        get() {
            return (com_sap_mbtepmdemoService?.provider as OnlineODataProvider).serviceRoot
        }

    var com_sap_mbtepmdemoService: Com_sap_mbtepmdemoService? = null
        private set
        get() {
            return field ?: throw IllegalStateException("SAPServiceManager was not initialized")
        }

    fun openODataStore(callback: () -> Unit) {
        if( appConfig != null ) {
            appConfig.serviceUrl?.let { _serviceURL ->
                com_sap_mbtepmdemoService = Com_sap_mbtepmdemoService (
                    OnlineODataProvider("SAPService", _serviceURL + CONNECTION_ID_COM_SAP_MBTEPMDEMOSERVICE).apply {
                        networkOptions.httpHandler = OKHttpHandler(ClientProvider.get())
                        serviceOptions.checkVersion = false
                        serviceOptions.requiresType = true
                        serviceOptions.cacheMetadata = false
                    }
                )
            } ?: run {
                throw IllegalStateException("ServiceURL of Configuration Data is not initialized")
            }
        }
        callback.invoke()
    }

    companion object {
        const val CONNECTION_ID_COM_SAP_MBTEPMDEMOSERVICE: String = "com.gs.erp.sap.demo"
    }
}
