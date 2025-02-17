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

package com.gs.erp.sap.demo.databinding.simplepropertyformcell

import androidx.databinding.BindingAdapter
import android.text.InputFilter
import com.sap.cloud.mobile.fiori.formcell.SimplePropertyFormCell

/*
 * Binding adapter for Fiori SimplePropertyFormCell UI component.
 * Android data binding library invokes its methods to set value for the SimplePropertyFormCell
 * In one way databinding, layout file has binding expression to convert entity properties to string
 */
object DataBindingAdapter {
    /*
     * For OData types: Edm.String
     * Getter of attribute bound returns String
     */
    @BindingAdapter("value")
    @JvmStatic
    fun setValue(simplePropertyFormCell: SimplePropertyFormCell, stringValue: String?) {
        if (stringValue == null) {
            simplePropertyFormCell.value = ""
        } else {
            simplePropertyFormCell.value = stringValue
        }
    }

    @BindingAdapter("android:maxLength")
    @JvmStatic
    fun setMaxLength(simplePropertyFormCell: SimplePropertyFormCell, value: Int) {
        val filters = simplePropertyFormCell.valueView.filters
        var newFilters = filters
        if (filters == null) {
            newFilters = arrayOf<InputFilter>(InputFilter.LengthFilter(value))
        } else {
            var hasMaxLengthFilter = false
            for (index in filters.indices) {
                val filter = filters[index]
                if (filter is InputFilter.LengthFilter) {
                    hasMaxLengthFilter = true
                    filters[index] = InputFilter.LengthFilter(value)
                    break
                }
            }
            if (!hasMaxLengthFilter) {
                var index = 0
                newFilters = arrayOfNulls(filters.size + 1)
                for (filter in filters) {
                    newFilters[index++] = filter
                }
                newFilters[index] = InputFilter.LengthFilter(value)
            }
        }
        simplePropertyFormCell.valueView.filters = newFilters
    }
}