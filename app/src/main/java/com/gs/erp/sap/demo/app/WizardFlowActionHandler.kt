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

package com.gs.erp.sap.demo.app

import com.gs.erp.sap.demo.R
import com.sap.cloud.mobile.flowv2.ext.FlowActionHandler
import com.sap.cloud.mobile.flowv2.ext.FlowCustomStep
import com.sap.cloud.mobile.flowv2.model.FlowType
import com.sap.cloud.mobile.foundation.authentication.CertificateProvider
import com.sap.cloud.mobile.foundation.authentication.SystemCertificateProvider

class WizardFlowActionHandler(val application: SAPWizardApplication): FlowActionHandler() {


    override fun getCertificateProvider(): CertificateProvider {
        return SystemCertificateProvider()
    }

    override fun getFlowCustomizationStep(runningFlowName: String?): List<FlowCustomStep<*>> {
        return if (runningFlowName == FlowType.ONBOARDING.name) {
            listOf(
                    FlowCustomStep.BeforeEula(R.id.stepWelcome, WelcomeStepFragment::class)
            )
        } else super.getFlowCustomizationStep(runningFlowName)
    }
}
