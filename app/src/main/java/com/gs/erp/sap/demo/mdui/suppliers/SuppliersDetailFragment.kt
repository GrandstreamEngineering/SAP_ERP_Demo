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

package com.gs.erp.sap.demo.mdui.suppliers

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
import com.gs.erp.sap.demo.databinding.FragmentSuppliersDetailBinding
import com.gs.erp.sap.demo.mdui.EntityKeyUtil
import com.gs.erp.sap.demo.mdui.InterfacedFragment
import com.gs.erp.sap.demo.mdui.UIConstants
import com.gs.erp.sap.demo.repository.OperationResult
import com.gs.erp.sap.demo.R
import com.gs.erp.sap.demo.viewmodel.supplier.SupplierViewModel
import com.sap.cloud.android.odata.com_sap_mbtepmdemoservice.Com_sap_mbtepmdemoServiceMetadata.*
import com.sap.cloud.android.odata.com_sap_mbtepmdemoservice.Supplier
import com.sap.cloud.mobile.fiori.`object`.ObjectHeader

import com.gs.erp.sap.demo.mdui.products.ProductsActivity
import com.gs.erp.sap.demo.mdui.purchaseorderheaders.PurchaseOrderHeadersActivity

/**
 * A fragment representing a single Supplier detail screen.
 * This fragment is contained in an SuppliersActivity.
 */
class SuppliersDetailFragment : InterfacedFragment<Supplier, FragmentSuppliersDetailBinding>() {

    /** Supplier entity to be displayed */
    private lateinit var supplierEntity: Supplier

    /** Fiori ObjectHeader component used when entity is to be displayed on phone */
    private var objectHeader: ObjectHeader? = null

    /** View model of the entity type that the displayed entity belongs to */
    private lateinit var viewModel: SupplierViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        menu = R.menu.itemlist_view_options
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        fragmentBinding.handler = this
        return fragmentBinding.root
    }

    override fun initBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentSuppliersDetailBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            currentActivity = it
            viewModel = ViewModelProvider(it)[SupplierViewModel::class.java]
            viewModel.deleteResult.observe(viewLifecycleOwner) { result ->
                onDeleteComplete(result)
            }

            viewModel.selectedEntity.observe(viewLifecycleOwner) { entity ->
                supplierEntity = entity
                fragmentBinding.supplier = entity
                setupObjectHeader()
            }
        }
    }


    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.update_item -> {
                listener?.onFragmentStateChange(UIConstants.EVENT_EDIT_ITEM, supplierEntity)
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
    private fun onDeleteComplete(result: OperationResult<Supplier>) {
        progressBar?.let {
            it.visibility = View.INVISIBLE
        }
        viewModel.removeAllSelected()
        result.error?.let {
            showError(getString(R.string.delete_failed_detail))
            return
        }
        listener?.onFragmentStateChange(UIConstants.EVENT_DELETION_COMPLETED, supplierEntity)
    }


    @Suppress("UNUSED", "UNUSED_PARAMETER") // parameter is needed because of the xml binding
    fun onNavigationClickedToProducts_Products(view: View) {
        val intent = Intent(currentActivity, ProductsActivity::class.java)
        intent.putExtra("parent", supplierEntity)
        intent.putExtra("navigation", "Products")
        startActivity(intent)
    }

    @Suppress("UNUSED", "UNUSED_PARAMETER") // parameter is needed because of the xml binding
    fun onNavigationClickedToPurchaseOrderHeaders_PurchaseOrders(view: View) {
        val intent = Intent(currentActivity, PurchaseOrderHeadersActivity::class.java)
        intent.putExtra("parent", supplierEntity)
        intent.putExtra("navigation", "PurchaseOrders")
        startActivity(intent)
    }

    /**
     * Set detail image of ObjectHeader.
     * When the entity does not provides picture, set the first character of the masterProperty.
     */
    private fun setDetailImage(objectHeader: ObjectHeader, supplierEntity: Supplier) {
        if (supplierEntity.getOptionalValue(Supplier.city) != null && !supplierEntity.getOptionalValue(Supplier.city).toString().isEmpty()) {
            objectHeader.detailImageCharacter = supplierEntity.getOptionalValue(Supplier.city).toString().substring(0, 1)
        } else {
            objectHeader.detailImageCharacter = "?"
        }
    }

    /**
     * Setup ObjectHeader with an instance of supplierEntity
     */
    private fun setupObjectHeader() {
        val secondToolbar = currentActivity.findViewById<Toolbar>(R.id.secondaryToolbar)
        if (secondToolbar != null) {
            secondToolbar.title = supplierEntity.entityType.localName
        } else {
            currentActivity.title = supplierEntity.entityType.localName
        }

        // Object Header is not available in tablet mode
        objectHeader = currentActivity.findViewById(R.id.objectHeader)
        val dataValue = supplierEntity.getOptionalValue(Supplier.city)

        objectHeader?.let {
            it.apply {
                headline = dataValue?.toString()
                subheadline = EntityKeyUtil.getOptionalEntityKey(supplierEntity)
                body = "You can set the header body text here."
                footnote = "You can set the header footnote here."
                description = "You can add a detailed item description here."
            }
            it.setTag("#tag1", 0)
            it.setTag("#tag3", 2)
            it.setTag("#tag2", 1)

            setDetailImage(it, supplierEntity)
            it.visibility = View.VISIBLE
        }
    }
}
