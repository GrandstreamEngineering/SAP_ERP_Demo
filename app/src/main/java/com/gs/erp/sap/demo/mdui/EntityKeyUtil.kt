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

import com.sap.cloud.mobile.odata.EntityValue

object EntityKeyUtil {
    /**
     * Check entity key and if set returns it in the format of key:value,...
     * EntityKey.toString() return in string format: {"key":value,"key2":value2}
     * @param entityValue containing the entity key
     * @return entity key as string in the format key:value, key:value, ... OR empty string
     */
    fun getOptionalEntityKey(entityValue: EntityValue): String {
        val keyString = entityValue.entityKey.toString().replace("\"", "").replace(",", ", ")
        return keyString.substring(1, keyString.length - 1)
    }
}