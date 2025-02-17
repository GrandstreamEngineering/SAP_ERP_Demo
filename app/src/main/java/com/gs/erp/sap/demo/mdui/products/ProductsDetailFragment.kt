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

package com.gs.erp.sap.demo.mdui.products

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
import com.gs.erp.sap.demo.databinding.FragmentProductsDetailBinding
import com.gs.erp.sap.demo.mdui.EntityKeyUtil
import com.gs.erp.sap.demo.mdui.InterfacedFragment
import com.gs.erp.sap.demo.mdui.UIConstants
import com.gs.erp.sap.demo.repository.OperationResult
import com.gs.erp.sap.demo.R
import com.gs.erp.sap.demo.viewmodel.product.ProductViewModel
import com.sap.cloud.android.odata.com_sap_mbtepmdemoservice.Com_sap_mbtepmdemoServiceMetadata.*
import com.sap.cloud.android.odata.com_sap_mbtepmdemoservice.Product
import com.sap.cloud.mobile.fiori.`object`.ObjectHeader

import com.gs.erp.sap.demo.mdui.suppliers.SuppliersActivity
import com.gs.erp.sap.demo.mdui.stock.StockActivity
import com.gs.erp.sap.demo.mdui.purchaseorderitems.PurchaseOrderItemsActivity
import com.gs.erp.sap.demo.mdui.salesorderitems.SalesOrderItemsActivity
import android.widget.ImageView
import com.gs.erp.sap.demo.app.SAPWizardApplication
import com.gs.erp.sap.demo.service.SAPServiceManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.gs.erp.sap.demo.mediaresource.EntityMediaResource

/**
 * A fragment representing a single Product detail screen.
 * This fragment is contained in an ProductsActivity.
 */
class ProductsDetailFragment : InterfacedFragment<Product, FragmentProductsDetailBinding>() {

    /** Product entity to be displayed */
    private lateinit var productEntity: Product

    /** Fiori ObjectHeader component used when entity is to be displayed on phone */
    private var objectHeader: ObjectHeader? = null

    /** View model of the entity type that the displayed entity belongs to */
    private lateinit var viewModel: ProductViewModel

    /**
     * Service manager to provide root URL of OData Service for Glide to load images if there are media resources
     * associated with the entity type
     */
    private var sapServiceManager: SAPServiceManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        menu = R.menu.itemlist_view_options
        sapServiceManager = (currentActivity.application as SAPWizardApplication).sapServiceManager
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        fragmentBinding.handler = this
        return fragmentBinding.root
    }

    override fun initBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentProductsDetailBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            currentActivity = it
            viewModel = ViewModelProvider(it)[ProductViewModel::class.java]
            viewModel.deleteResult.observe(viewLifecycleOwner) { result ->
                onDeleteComplete(result)
            }

            viewModel.selectedEntity.observe(viewLifecycleOwner) { entity ->
                productEntity = entity
                fragmentBinding.product = entity
                setupObjectHeader()
            }
        }
    }


    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.update_item -> {
                listener?.onFragmentStateChange(UIConstants.EVENT_EDIT_ITEM, productEntity)
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
    private fun onDeleteComplete(result: OperationResult<Product>) {
        progressBar?.let {
            it.visibility = View.INVISIBLE
        }
        viewModel.removeAllSelected()
        result.error?.let {
            showError(getString(R.string.delete_failed_detail))
            return
        }
        listener?.onFragmentStateChange(UIConstants.EVENT_DELETION_COMPLETED, productEntity)
    }


    @Suppress("UNUSED", "UNUSED_PARAMETER") // parameter is needed because of the xml binding
    fun onNavigationClickedToSuppliers_Supplier(view: View) {
        val intent = Intent(currentActivity, SuppliersActivity::class.java)
        intent.putExtra("parent", productEntity)
        intent.putExtra("navigation", "Supplier")
        startActivity(intent)
    }

    @Suppress("UNUSED", "UNUSED_PARAMETER") // parameter is needed because of the xml binding
    fun onNavigationClickedToStock_Stock(view: View) {
        val intent = Intent(currentActivity, StockActivity::class.java)
        intent.putExtra("parent", productEntity)
        intent.putExtra("navigation", "Stock")
        startActivity(intent)
    }

    @Suppress("UNUSED", "UNUSED_PARAMETER") // parameter is needed because of the xml binding
    fun onNavigationClickedToPurchaseOrderItems_PurchaseOrderItems(view: View) {
        val intent = Intent(currentActivity, PurchaseOrderItemsActivity::class.java)
        intent.putExtra("parent", productEntity)
        intent.putExtra("navigation", "PurchaseOrderItems")
        startActivity(intent)
    }

    @Suppress("UNUSED", "UNUSED_PARAMETER") // parameter is needed because of the xml binding
    fun onNavigationClickedToSalesOrderItems_SalesOrderItems(view: View) {
        val intent = Intent(currentActivity, SalesOrderItemsActivity::class.java)
        intent.putExtra("parent", productEntity)
        intent.putExtra("navigation", "SalesOrderItems")
        startActivity(intent)
    }

    /**
     * Set detail image of ObjectHeader.
     * When the entity does not provides picture, set the first character of the masterProperty.
     */
    private fun setDetailImage(objectHeader: ObjectHeader, productEntity: Product) {
        if (EntityMediaResource.hasMediaResources(EntitySets.products)) {
            // Glide offers caching in addition to fetching the images
            objectHeader.prepareDetailImageView().scaleType = ImageView.ScaleType.FIT_CENTER
            objectHeader.detailImageView?.let {
                Glide.with(currentActivity)
                        .load(productEntity.getOptionalValue(Product.pictureUrl).toString())
                        .apply(RequestOptions().fitCenter())
                        .into(it)
            }
        } else if (productEntity.getOptionalValue(Product.category) != null && !productEntity.getOptionalValue(Product.category).toString().isEmpty()) {
            objectHeader.detailImageCharacter = productEntity.getOptionalValue(Product.category).toString().substring(0, 1)
        } else {
            objectHeader.detailImageCharacter = "?"
        }
    }

    /**
     * Setup ObjectHeader with an instance of productEntity
     */
    private fun setupObjectHeader() {
        val secondToolbar = currentActivity.findViewById<Toolbar>(R.id.secondaryToolbar)
        if (secondToolbar != null) {
            secondToolbar.title = productEntity.entityType.localName
        } else {
            currentActivity.title = productEntity.entityType.localName
        }

        // Object Header is not available in tablet mode
        objectHeader = currentActivity.findViewById(R.id.objectHeader)
        val dataValue = productEntity.getOptionalValue(Product.name)

        objectHeader?.let {
            it.apply {
                headline = dataValue?.toString()
                subheadline = EntityKeyUtil.getOptionalEntityKey(productEntity)
                body = null
                footnote = null
                description = null
            }
            it.setTag("#tag1", 0)
            it.setTag("#tag3", 2)
            it.setTag("#tag2", 1)

            setDetailImage(it, productEntity)
            it.visibility = View.VISIBLE
        }
    }
}
