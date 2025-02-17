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

package com.gs.erp.sap.demo.mdui.purchaseorderheaders

import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import com.gs.erp.sap.demo.R
import com.gs.erp.sap.demo.databinding.FragmentPurchaseorderheadersCreateBinding
import com.gs.erp.sap.demo.mdui.BundleKeys
import com.gs.erp.sap.demo.mdui.InterfacedFragment
import com.gs.erp.sap.demo.mdui.UIConstants
import com.gs.erp.sap.demo.repository.OperationResult
import com.gs.erp.sap.demo.viewmodel.purchaseorderheader.PurchaseOrderHeaderViewModel
import com.sap.cloud.android.odata.com_sap_mbtepmdemoservice.PurchaseOrderHeader
import com.sap.cloud.android.odata.com_sap_mbtepmdemoservice.Com_sap_mbtepmdemoServiceMetadata.EntityTypes
import com.sap.cloud.mobile.fiori.formcell.SimplePropertyFormCell
import com.sap.cloud.mobile.fiori.`object`.ObjectHeader
import com.sap.cloud.mobile.odata.Property
import org.slf4j.LoggerFactory

/**
 * A fragment that is used for both update and create for users to enter values for the properties. When used for
 * update, an instance of the entity is required. In the case of create, a new instance of the entity with defaults will
 * be created. The default values may not be acceptable for the OData service.
 * This fragment is either contained in a [PurchaseOrderHeadersListActivity] in two-pane mode (on tablets) or a
 * [PurchaseOrderHeadersDetailActivity] on handsets.
 *
 * Arguments: Operation: [OP_CREATE | OP_UPDATE]
 *            PurchaseOrderHeader if Operation is update
 */
class PurchaseOrderHeadersCreateFragment : InterfacedFragment<PurchaseOrderHeader, FragmentPurchaseorderheadersCreateBinding>() {

    /** PurchaseOrderHeader object and it's copy: the modifications are done on the copied object. */
    private lateinit var purchaseOrderHeaderEntity: PurchaseOrderHeader
    private lateinit var purchaseOrderHeaderEntityCopy: PurchaseOrderHeader

    /** Indicate what operation to be performed */
    private lateinit var operation: String

    /** purchaseOrderHeaderEntity ViewModel */
    private lateinit var viewModel: PurchaseOrderHeaderViewModel

    /** The update menu item */
    private lateinit var updateMenuItem: MenuItem

    private val isPurchaseOrderHeaderValid: Boolean
        get() {
            var isValid = true
            fragmentBinding.createUpdatePurchaseorderheader.let { linearLayout ->
                for (i in 0 until linearLayout.childCount) {
                    val simplePropertyFormCell = linearLayout.getChildAt(i) as SimplePropertyFormCell
                    val propertyName = simplePropertyFormCell.tag as String
                    val property = EntityTypes.purchaseOrderHeader.getProperty(propertyName)
                    val value = simplePropertyFormCell.value.toString()
                    if (!isValidProperty(property, value)) {
                        if (!simplePropertyFormCell.isErrorEnabled) {
                            val errorMessage = resources.getString(R.string.mandatory_warning)
                            simplePropertyFormCell.isErrorEnabled = true
                            simplePropertyFormCell.error = errorMessage
                        }
                        isValid = false
                    } else {
                        if (simplePropertyFormCell.isErrorEnabled) {
                            val hasMandatoryError =
                                if (simplePropertyFormCell.getTag(R.id.TAG_HAS_MANDATORY_ERROR) == null) {
                                    false
                                } else {
                                    simplePropertyFormCell.getTag(R.id.TAG_HAS_MANDATORY_ERROR) as Boolean
                                }
                            if (!hasMandatoryError && resources.getString(R.string.mandatory_warning).equals(simplePropertyFormCell.error)) {
                                simplePropertyFormCell.isErrorEnabled = false
                                simplePropertyFormCell.error = null
                            } else {
                                isValid = false
                            }
                        }
                        simplePropertyFormCell.apply {
                            getTag(R.id.TAG_HAS_MANDATORY_ERROR)?.let {
                                setTag(R.id.TAG_HAS_MANDATORY_ERROR, false)
                            }
                        }
                    }
                }
            }
            return isValid
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        menu = R.menu.itemlist_edit_options

        arguments?.let {
            (it.getString(BundleKeys.OPERATION))?.let { operationType ->
                operation = operationType
                activityTitle = when (operationType) {
                    UIConstants.OP_CREATE -> resources.getString(R.string.title_create_fragment, EntityTypes.purchaseOrderHeader.localName)
                    else -> resources.getString(R.string.title_update_fragment) + " " + EntityTypes.purchaseOrderHeader.localName

                }
            }
        }

        activity?.let {
            (it as PurchaseOrderHeadersActivity).isNavigationDisabled = true
            viewModel = ViewModelProvider(it)[PurchaseOrderHeaderViewModel::class.java]
            viewModel.createResult.observe(this) { result -> onComplete(result) }
            viewModel.updateResult.observe(this) { result -> onComplete(result) }

            purchaseOrderHeaderEntity = if (operation == UIConstants.OP_CREATE) {
                createPurchaseOrderHeader()
            } else {
                viewModel.selectedEntity.value!!
            }

            val workingCopy = when{ (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) -> {
                    savedInstanceState?.getParcelable<PurchaseOrderHeader>(KEY_WORKING_COPY, PurchaseOrderHeader::class.java)
                } else -> @Suppress("DEPRECATION") savedInstanceState?.getParcelable<PurchaseOrderHeader>(KEY_WORKING_COPY)
            }

            if (workingCopy == null) {
                purchaseOrderHeaderEntityCopy = purchaseOrderHeaderEntity.copy()
                purchaseOrderHeaderEntityCopy.entityTag = purchaseOrderHeaderEntity.entityTag
                purchaseOrderHeaderEntityCopy.oldEntity = purchaseOrderHeaderEntity
                purchaseOrderHeaderEntityCopy.editLink = purchaseOrderHeaderEntity.editLink
                purchaseOrderHeaderEntityCopy.dataPath = purchaseOrderHeaderEntity.dataPath
                purchaseOrderHeaderEntityCopy.isContained = purchaseOrderHeaderEntity.isContained
            } else {
                purchaseOrderHeaderEntityCopy = workingCopy
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        currentActivity.findViewById<ObjectHeader>(R.id.objectHeader)?.let {
            it.visibility = View.GONE
        }
        fragmentBinding.purchaseOrderHeader = purchaseOrderHeaderEntityCopy
        return fragmentBinding.root
    }

    override fun initBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentPurchaseorderheadersCreateBinding.inflate(inflater, container, false)

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.save_item -> {
                updateMenuItem = menuItem
                enableUpdateMenuItem(false)
                onSaveItem()
            }
            else -> super.onMenuItemSelected(menuItem)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(secondaryToolbar != null) secondaryToolbar!!.title = activityTitle else activity?.title = activityTitle
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(KEY_WORKING_COPY, purchaseOrderHeaderEntityCopy)
        super.onSaveInstanceState(outState)
    }

    /** Enables the update menu item based on [enable] */
    private fun enableUpdateMenuItem(enable : Boolean = true) {
        updateMenuItem.also {
            it.isEnabled = enable
            it.icon?.alpha = if(enable) 255 else 130
        }
    }

    /** Saves the entity */
    private fun onSaveItem(): Boolean {
        if (!isPurchaseOrderHeaderValid) {
            enableUpdateMenuItem(true)
            return false
        }
        (currentActivity as PurchaseOrderHeadersActivity).isNavigationDisabled = false
        progressBar?.visibility = View.VISIBLE
        when (operation) {
            UIConstants.OP_CREATE -> {
                viewModel.create(purchaseOrderHeaderEntityCopy)
            }
            UIConstants.OP_UPDATE -> viewModel.update(purchaseOrderHeaderEntityCopy)
        }
        return true
    }

    /**
     * Create a new PurchaseOrderHeader instance and initialize properties to its default values
     * Nullable property will remain null
     * @return new PurchaseOrderHeader instance
     */
    private fun createPurchaseOrderHeader(): PurchaseOrderHeader {
        val entity = PurchaseOrderHeader(true)
        return entity
    }

    /** Callback function to complete processing when updateResult or createResult events fired */
    private fun onComplete(result: OperationResult<PurchaseOrderHeader>) {
        progressBar?.visibility = View.INVISIBLE
        enableUpdateMenuItem(true)
        if (result.error != null) {
            (currentActivity as PurchaseOrderHeadersActivity).isNavigationDisabled = true
            handleError(result)
        } else {
            if (operation == UIConstants.OP_UPDATE && !currentActivity.resources.getBoolean(R.bool.two_pane)) {
                viewModel.selectedEntity.value = purchaseOrderHeaderEntityCopy
            }
            (currentActivity as PurchaseOrderHeadersActivity).onBackPressedDispatcher.onBackPressed()
        }
    }

    /** Simple validation: checks the presence of mandatory fields. */
    private fun isValidProperty(property: Property, value: String): Boolean {
        return !(!property.isNullable && value.isEmpty())
    }

    /**
     * Notify user of error encountered while execution the operation
     *
     * @param [result] operation result with error
     */
    private fun handleError(result: OperationResult<PurchaseOrderHeader>) {
        val errorMessage = when (result.operation) {
            OperationResult.Operation.UPDATE -> getString(R.string.update_failed_detail)
            OperationResult.Operation.CREATE -> getString(R.string.create_failed_detail)
            else -> throw AssertionError()
        }
        showError(errorMessage)
    }


    companion object {
        private val KEY_WORKING_COPY = "WORKING_COPY"
        private val LOGGER = LoggerFactory.getLogger(PurchaseOrderHeadersActivity::class.java)
    }
}
