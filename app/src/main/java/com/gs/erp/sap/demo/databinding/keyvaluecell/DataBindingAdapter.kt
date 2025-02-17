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

package com.gs.erp.sap.demo.databinding.keyvaluecell

import com.sap.cloud.mobile.fiori.misc.KeyValueCell

/*
 * Binding adapter for Fiori KeyValueCell UI component.
 * Android data binding library invokes its methods to set value for the KeyValueCell
 * In one way databinding, layout file has binding expression to convert entity properties to string
 */
object DataBindingAdapter {
    /*
     * For OData types: Edm.String
     * Getter of attribute bound returns String
     */
    @androidx.databinding.BindingAdapter("valueText")
    @JvmStatic fun setValueText(keyValueCell: KeyValueCell, stringValue: String?) {
        if (stringValue == null) {
            keyValueCell.value = ""
        } else {
            keyValueCell.value = stringValue
        }
    }
}