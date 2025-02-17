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

import android.graphics.Color

/** Class containing all constants required by various UI activities/fragments */
object UIConstants {

    /** Fragment tag to identify create fragment */
    const val CREATE_FRAGMENT_TAG = "CreateFragment"

    /** Fragment tag to identify update fragment */
    const val MODIFY_FRAGMENT_TAG = "UpdateFragment"

    /** Fragment tag to identify detail fragment */
    const val DETAIL_FRAGMENT_TAG = "DetailFragment"

    /** Fragment tag to identify entity list fragment  */
    const val LIST_FRAGMENT_TAG = "ListFragment"

    /** Perform read/display operation */
    const val OP_READ = "OpRead"

    /** Perform create operation */
    const val OP_CREATE = "OpCreate"

    /** Perform update operation */
    const val OP_UPDATE = "OpUpdate"

    /** Fragment tag to identify the confirmation dialog fragment */
    const val CONFIRMATION_FRAGMENT_TAG = "ConfirmationFragment"

    /** Event types */
    const val EVENT_ITEM_CLICKED = 0
    const val EVENT_CREATE_NEW_ITEM = 1
    const val EVENT_DELETION_COMPLETED = 2
    const val EVENT_EDIT_ITEM = 3
    const val EVENT_BACK_NAVIGATION_CONFIRMED = 4
    const val EVENT_ASK_DELETE_CONFIRMATION = 5

    /** SAP Fiori Standard Theme Primary Color: 'Global Dark Base'  */
    @JvmField val FIORI_STANDARD_THEME_GLOBAL_LIGHT_BASE = Color.rgb(239, 244, 249)

    /** SAP Fiori Standard Theme Primary Color: 'Global Dark Base'  */
    @JvmField val FIORI_STANDARD_THEME_GLOBAL_DARK_BASE = Color.rgb(63, 81, 96)

    /** SAP Fiori Standard Theme Primary Color: 'Global Dark Base'  */
    @JvmField val FIORI_STANDARD_THEME_BRAND_HIGHLIGHT_LIGHT = Color.rgb(66, 124, 172)

    /** SAP Fiori Standard Theme Primary Color: 'Global Dark Base'  */
    @JvmField val FIORI_STANDARD_THEME_BRAND_HIGHLIGHT_DARK = Color.rgb(145, 200, 246)

    /** SAP Fiori Standard Theme Primary Color: 'Background'  */
    @JvmField val FIORI_STANDARD_THEME_BACKGROUND = Color.rgb(250, 250, 250)
}
