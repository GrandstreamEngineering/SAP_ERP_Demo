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

import com.gs.erp.sap.demo.R
import android.content.Context
import android.util.AttributeSet
import androidx.preference.ListPreference
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MaterialListPreference : ListPreference {
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context,attrs,defStyleAttr)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context) : super(context)

    override fun onClick() {
        MaterialAlertDialogBuilder(context, R.style.AlertDialogStyle)
            .setSingleChoiceItems(entries, findIndexOfValue(value)) { dialog, which ->
                if (callChangeListener(entryValues[which].toString())) {
                    setValueIndex(which)
                }
                dialog.dismiss()
            }
            .setNegativeButton(
                R.string.cancel
            ) { dialog, _ ->
                dialog.cancel()
            }
            .setTitle(title)
            .create()
            .show()
    }
}
