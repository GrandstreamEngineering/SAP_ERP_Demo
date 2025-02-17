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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

import com.sap.cloud.mobile.flowv2.core.DialogHelper
import com.sap.cloud.mobile.flowv2.core.Flow
import com.sap.cloud.mobile.flowv2.core.FlowContextBuilder
import com.sap.cloud.mobile.flowv2.ext.FlowOptions
import com.sap.cloud.mobile.foundation.configurationprovider.*
import com.sap.cloud.mobile.foundation.model.AppConfig
import com.sap.cloud.mobile.foundation.mobileservices.ApplicationStates
import com.sap.cloud.mobile.foundation.networking.BlockedUserInterceptor
import com.gs.erp.sap.demo.databinding.ActivitySplashBinding
import com.gs.erp.sap.demo.R

class WelcomeActivity : AppCompatActivity() {
    private val fManager = this.supportFragmentManager
    private lateinit var binding: ActivitySplashBinding
    private var sharedPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        startConfigurationLoader()
    }

    private fun startConfigurationLoader() {
        val callback: ConfigurationLoaderCallback = object : ConfigurationLoaderCallback() {
            override fun onCompletion(providerIdentifier: ProviderIdentifier?, success: Boolean) {
                if (success) {
                    startOnboarding(this@WelcomeActivity)
                } else {
                    DialogHelper(application, R.style.OnboardingDefaultTheme_Dialog_Alert)
                            .showOKOnlyDialog(
                                    fManager,
                                    resources.getString(R.string.config_loader_complete_error_description),
                                    null, null, null
                            )
                }
            }

            override fun onError(configurationLoader: ConfigurationLoader, providerIdentifier: ProviderIdentifier, userInputs: UserInputs, configurationProviderError: ConfigurationProviderError) {
                DialogHelper(application, R.style.OnboardingDefaultTheme_Dialog_Alert)
                        .showOKOnlyDialog(
                                fManager, String.format(resources.getString(
                                R.string.config_loader_on_error_description),
                                providerIdentifier.toString(), configurationProviderError.errorMessage
                        ),
                                null, null, null
                        )
                configurationLoader.processRequestedInputs(UserInputs())
            }

            override fun onInputRequired(configurationLoader: ConfigurationLoader, userInputs: UserInputs) {
                configurationLoader.processRequestedInputs(UserInputs())
            }
        }
        val providers = arrayOf<ConfigurationProvider>(FileConfigurationProvider(this, "sap_mobile_services"))
        this.runOnUiThread {
            val loader = ConfigurationLoader(this, callback, providers)
            loader.loadConfiguration()
        }
    }

    private fun prepareAppConfig(): AppConfig? {
        return try {
            val configData = DefaultPersistenceMethod.getPersistedConfiguration(this)
            AppConfig.createAppConfigFromJsonString(configData.toString())
        } catch (ex: ConfigurationPersistenceException) {
            DialogHelper(this, R.style.OnboardingDefaultTheme_Dialog_Alert)
                    .showOKOnlyDialog(
                            fManager,
                            resources.getString(R.string.config_data_build_json_description),
                            null, null, null
                    )
            null
        } catch (ex: Exception) {
            DialogHelper(this, R.style.OnboardingDefaultTheme_Dialog_Alert)
                    .showOKOnlyDialog(
                            fManager,
                            ex.localizedMessage ?: resources.getString(R.string.error_unknown_app_config),
                            null, null, null
                    )
            null
        }
    }

    internal fun startOnboarding(activity: Activity) {
        val appConfig = prepareAppConfig() ?: return
        val flowContext =
                FlowContextBuilder()
                    .setApplication(appConfig)
                    .setMultipleUserMode(false)
                    .setFlowActionHandler(WizardFlowActionHandler(activity.application as SAPWizardApplication))
                    .setFlowStateListener(WizardFlowStateListener(activity.application as SAPWizardApplication))
                    .setFlowOptions(FlowOptions(
                            needConfirmWhenDeleteRegistration = false,
                            excludeEula = true,
                            excludeEulaWhenCreateAccount = true,
                            fullScreen = false
                    ))
                    .build()
        Flow.start(activity, flowContext) { _, resultCode, _ ->
            if (resultCode == Activity.RESULT_OK) {
                (application as SAPWizardApplication).isApplicationUnlocked = true
                Intent(application, MainBusinessActivity::class.java).also {
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    application.startActivity(it)
                }

                if (ApplicationStates.isNetworkAvailable) {
                    sharedPreferences = application.getSharedPreferences(
                        SAPWizardApplication.KEY_LOCK_WIPE_POLICY_PARAMETERS_PREFERENCE,
                        Context.MODE_PRIVATE
                    )
                    sharedPreferences?.getString(
                        SAPWizardApplication.LOCK_WIPE_INVOKING_TYPE,
                        BlockedUserInterceptor.BlockType.UNDEFINED_BLOCK.name
                    )?.let { blockType ->
                        if (!blockType.equals(BlockedUserInterceptor.BlockType.UNDEFINED_BLOCK.name)) {
                            var invokingType = when (blockType) {
                                BlockedUserInterceptor.BlockType.REGISTRATION_LOCKED.name -> {
                                    BlockedUserInterceptor.BlockType.REGISTRATION_LOCKED
                                }
                                BlockedUserInterceptor.BlockType.REGISTRATION_WIPED.name -> {
                                    BlockedUserInterceptor.BlockType.REGISTRATION_WIPED
                                }
                                else -> {
                                    BlockedUserInterceptor.BlockType.UNDEFINED_BLOCK
                                }
                            }
                            //Clear the flag of blockType which should be handled to invoke lock or wipe
                            sharedPreferences?.edit()?.apply {
                                remove(SAPWizardApplication.LOCK_WIPE_INVOKING_TYPE)
                            }?.apply()
                            (application as SAPWizardApplication).applyLockOrWipeByServer(
                                invokingType
                            )
                        }
                    }
                }
            } else {
                startOnboarding(activity)
            }
        }
    }
}
