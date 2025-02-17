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

package com.gs.erp.sap.demo.mdui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.Preference
import com.gs.erp.sap.demo.databinding.ActivitySettingsBinding
import com.gs.erp.sap.demo.R
import com.gs.erp.sap.demo.viewmodel.OperationResult
import com.gs.erp.sap.demo.viewmodel.OperationType
import com.gs.erp.sap.demo.viewmodel.OperationType.UPLOAD_LOG
import com.gs.erp.sap.demo.viewmodel.OperationType.UPLOAD_USAGE_DATA
import com.gs.erp.sap.demo.viewmodel.SettingsViewModel
import com.sap.cloud.mobile.flowv2.core.DialogHelper
import kotlinx.coroutines.launch

class SettingsActivity: AppCompatActivity() {
    lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingsFragment = SettingsFragment()
        val binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
        lifecycleScope?.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.operationUIState.collect{
                    val result = it.result
                    result?.let {
                        when (it) {
                            is OperationResult.OperationFail -> {
                                DialogHelper(this@SettingsActivity).showOKOnlyDialog(
                                    fragmentManager = supportFragmentManager,
                                    message = it.message
                                )
                                enablePreferenceStatus(it.operationType, settingsFragment)
                            }
                            is OperationResult.OperationSuccess -> {
                                Toast.makeText(this@SettingsActivity, it.message, Toast.LENGTH_LONG).show()
                                enablePreferenceStatus(it.operationType, settingsFragment)
                            }
                        }
                        viewModel.resetOperationState()
                    }
                    val inProgress = it.inProgress
                    inProgress?.let {
                        when {
                            it -> {
                                binding.indeterminateBar.visibility = View.VISIBLE
                            }
                            else -> {
                                binding.indeterminateBar.visibility = View.INVISIBLE
                            }
                        }
                    }
                }
            }
        }
        supportFragmentManager.beginTransaction().replace(binding.settingsContainer.id, settingsFragment).commit()
    }

    private fun enablePreferenceStatus(operationType: OperationType, settingsFragment: SettingsFragment) {
        when (operationType) {
            UPLOAD_LOG -> {
                var logUploadPreference: Preference? =
                    settingsFragment.findPreference(getString(R.string.upload_log))
                logUploadPreference?.apply {
                    isEnabled = true
                }
            }

            UPLOAD_USAGE_DATA -> {
                var usageUploadPreference: Preference? =
                    settingsFragment.findPreference(getString(R.string.upload_usage))
                usageUploadPreference?.apply {
                    isEnabled = true
                }
            }
        }
    }
}
