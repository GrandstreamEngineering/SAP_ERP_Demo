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

package com.gs.erp.sap.demo.mdui.purchaseorderitems

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.gs.erp.sap.demo.databinding.FragmentPurchaseorderitemsDetailBinding
import com.gs.erp.sap.demo.mdui.EntityKeyUtil
import com.gs.erp.sap.demo.mdui.InterfacedFragment
import com.gs.erp.sap.demo.mdui.UIConstants
import com.gs.erp.sap.demo.repository.OperationResult
import com.gs.erp.sap.demo.R
import com.gs.erp.sap.demo.viewmodel.purchaseorderitem.PurchaseOrderItemViewModel
import com.sap.cloud.android.odata.com_sap_mbtepmdemoservice.Com_sap_mbtepmdemoServiceMetadata.*
import com.sap.cloud.android.odata.com_sap_mbtepmdemoservice.PurchaseOrderItem
import com.sap.cloud.mobile.fiori.`object`.ObjectHeader

import com.gs.erp.sap.demo.mdui.products.ProductsActivity
import com.gs.erp.sap.demo.mdui.purchaseorderheaders.PurchaseOrderHeadersActivity

/**
 * A fragment representing a single PurchaseOrderItem detail screen.
 * This fragment is contained in an PurchaseOrderItemsActivity.
 */
class PurchaseOrderItemsDetailFragment : InterfacedFragment<PurchaseOrderItem, FragmentPurchaseorderitemsDetailBinding>() {

    /** PurchaseOrderItem entity to be displayed */
    private lateinit var purchaseOrderItemEntity: PurchaseOrderItem

    /** Fiori ObjectHeader component used when entity is to be displayed on phone */
    private var objectHeader: ObjectHeader? = null

    /** View model of the entity type that the displayed entity belongs to */
    private lateinit var viewModel: PurchaseOrderItemViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        menu = R.menu.itemlist_view_options
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        fragmentBinding.handler = this
        return fragmentBinding.root
    }

    override fun initBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentPurchaseorderitemsDetailBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            currentActivity = it
            viewModel = ViewModelProvider(it)[PurchaseOrderItemViewModel::class.java]
            viewModel.deleteResult.observe(viewLifecycleOwner) { result ->
                onDeleteComplete(result)
            }

            viewModel.selectedEntity.observe(viewLifecycleOwner) { entity ->
                purchaseOrderItemEntity = entity
                fragmentBinding.purchaseOrderItem = entity
                setupObjectHeader()
            }
        }
    }


    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.update_item -> {
                listener?.onFragmentStateChange(UIConstants.EVENT_EDIT_ITEM, purchaseOrderItemEntity)
                true
            }
            R.id.delete_item -> {
                listener?.onFragmentStateChange(UIConstants.EVENT_ASK_DELETE_CONFIRMATION,null)
                true
            }
            else -> super.onMenuItemSelected(menuItem)
        }
    }

    /**
     * Completion callback for delete operation
     *
     * @param [result] of the operation
     */
    private fun onDeleteComplete(result: OperationResult<PurchaseOrderItem>) {
        progressBar?.let {
            it.visibility = View.INVISIBLE
        }
        viewModel.removeAllSelected()
        result.error?.let {
            showError(getString(R.string.delete_failed_detail))
            return
        }
        listener?.onFragmentStateChange(UIConstants.EVENT_DELETION_COMPLETED, purchaseOrderItemEntity)
    }


    @Suppress("UNUSED", "UNUSED_PARAMETER") // parameter is needed because of the xml binding
    fun onNavigationClickedToProducts_Product(view: View) {
        val intent = Intent(currentActivity, ProductsActivity::class.java)
        intent.putExtra("parent", purchaseOrderItemEntity)
        intent.putExtra("navigation", "Product")
        startActivity(intent)
    }

    @Suppress("UNUSED", "UNUSED_PARAMETER") // parameter is needed because of the xml binding
    fun onNavigationClickedToPurchaseOrderHeaders_Header(view: View) {
        val intent = Intent(currentActivity, PurchaseOrderHeadersActivity::class.java)
        intent.putExtra("parent", purchaseOrderItemEntity)
        intent.putExtra("navigation", "Header")
        startActivity(intent)
    }

    /**
     * Set detail image of ObjectHeader.
     * When the entity does not provides picture, set the first character of the masterProperty.
     */
    private fun setDetailImage(objectHeader: ObjectHeader, purchaseOrderItemEntity: PurchaseOrderItem) {
        if (purchaseOrderItemEntity.getOptionalValue(PurchaseOrderItem.currencyCode) != null && !purchaseOrderItemEntity.getOptionalValue(PurchaseOrderItem.currencyCode).toString().isEmpty()) {
            objectHeader.detailImageCharacter = purchaseOrderItemEntity.getOptionalValue(PurchaseOrderItem.currencyCode).toString().substring(0, 1)
        } else {
            objectHeader.detailImageCharacter = "?"
        }
    }

    /**
     * Setup ObjectHeader with an instance of purchaseOrderItemEntity
     */
    private fun setupObjectHeader() {
        val secondToolbar = currentActivity.findViewById<Toolbar>(R.id.secondaryToolbar)
        if (secondToolbar != null) {
            secondToolbar.title = purchaseOrderItemEntity.entityType.localName
        } else {
            currentActivity.title = purchaseOrderItemEntity.entityType.localName
        }

        // Object Header is not available in tablet mode
        objectHeader = currentActivity.findViewById(R.id.objectHeader)
        val dataValue = purchaseOrderItemEntity.getOptionalValue(PurchaseOrderItem.currencyCode)

        objectHeader?.let {
            it.apply {
                headline = dataValue?.toString()
                subheadline = EntityKeyUtil.getOptionalEntityKey(purchaseOrderItemEntity)
                body = "You can set the header body text here."
                footnote = "You can set the header footnote here."
                description = "You can add a detailed item description here."
            }
            it.setTag("#tag1", 0)
            it.setTag("#tag3", 2)
            it.setTag("#tag2", 1)

            setDetailImage(it, purchaseOrderItemEntity)
            it.visibility = View.VISIBLE
        }
    }
}
