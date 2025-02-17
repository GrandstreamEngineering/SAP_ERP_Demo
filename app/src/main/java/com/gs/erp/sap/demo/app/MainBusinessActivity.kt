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

import android.content.Intent
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import com.gs.erp.sap.demo.databinding.ActivityMainBusinessBinding
import com.gs.erp.sap.demo.R


import org.slf4j.LoggerFactory

import com.gs.erp.sap.demo.mdui.EntitySetListActivity

class MainBusinessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBusinessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBusinessBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }


    private fun startEntitySetListActivity() {
        val sapServiceManager = (application as SAPWizardApplication).sapServiceManager
        sapServiceManager?.openODataStore {
            val intent = Intent(this, EntitySetListActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        startEntitySetListActivity()
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(MainBusinessActivity::class.java)
    }
}
