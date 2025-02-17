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

package com.gs.erp.sap.demo.mdui.productcategories

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
import com.gs.erp.sap.demo.databinding.FragmentProductcategoriesDetailBinding
import com.gs.erp.sap.demo.mdui.EntityKeyUtil
import com.gs.erp.sap.demo.mdui.InterfacedFragment
import com.gs.erp.sap.demo.mdui.UIConstants
import com.gs.erp.sap.demo.repository.OperationResult
import com.gs.erp.sap.demo.R
import com.gs.erp.sap.demo.viewmodel.productcategory.ProductCategoryViewModel
import com.sap.cloud.android.odata.com_sap_mbtepmdemoservice.Com_sap_mbtepmdemoServiceMetadata.*
import com.sap.cloud.android.odata.com_sap_mbtepmdemoservice.ProductCategory
import com.sap.cloud.mobile.fiori.`object`.ObjectHeader


/**
 * A fragment representing a single ProductCategory detail screen.
 * This fragment is contained in an ProductCategoriesActivity.
 */
class ProductCategoriesDetailFragment : InterfacedFragment<ProductCategory, FragmentProductcategoriesDetailBinding>() {

    /** ProductCategory entity to be displayed */
    private lateinit var productCategoryEntity: ProductCategory

    /** Fiori ObjectHeader component used when entity is to be displayed on phone */
    private var objectHeader: ObjectHeader? = null

    /** View model of the entity type that the displayed entity belongs to */
    private lateinit var viewModel: ProductCategoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        menu = R.menu.itemlist_view_options
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        fragmentBinding.handler = this
        return fragmentBinding.root
    }

    override fun initBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentProductcategoriesDetailBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            currentActivity = it
            viewModel = ViewModelProvider(it)[ProductCategoryViewModel::class.java]
            viewModel.deleteResult.observe(viewLifecycleOwner) { result ->
                onDeleteComplete(result)
            }

            viewModel.selectedEntity.observe(viewLifecycleOwner) { entity ->
                productCategoryEntity = entity
                fragmentBinding.productCategory = entity
                setupObjectHeader()
            }
        }
    }


    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.update_item -> {
                listener?.onFragmentStateChange(UIConstants.EVENT_EDIT_ITEM, productCategoryEntity)
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
    private fun onDeleteComplete(result: OperationResult<ProductCategory>) {
        progressBar?.let {
            it.visibility = View.INVISIBLE
        }
        viewModel.removeAllSelected()
        result.error?.let {
            showError(getString(R.string.delete_failed_detail))
            return
        }
        listener?.onFragmentStateChange(UIConstants.EVENT_DELETION_COMPLETED, productCategoryEntity)
    }


    /**
     * Set detail image of ObjectHeader.
     * When the entity does not provides picture, set the first character of the masterProperty.
     */
    private fun setDetailImage(objectHeader: ObjectHeader, productCategoryEntity: ProductCategory) {
        if (productCategoryEntity.getOptionalValue(ProductCategory.categoryName) != null && !productCategoryEntity.getOptionalValue(ProductCategory.categoryName).toString().isEmpty()) {
            objectHeader.detailImageCharacter = productCategoryEntity.getOptionalValue(ProductCategory.categoryName).toString().substring(0, 1)
        } else {
            objectHeader.detailImageCharacter = "?"
        }
    }

    /**
     * Setup ObjectHeader with an instance of productCategoryEntity
     */
    private fun setupObjectHeader() {
        val secondToolbar = currentActivity.findViewById<Toolbar>(R.id.secondaryToolbar)
        if (secondToolbar != null) {
            secondToolbar.title = productCategoryEntity.entityType.localName
        } else {
            currentActivity.title = productCategoryEntity.entityType.localName
        }

        // Object Header is not available in tablet mode
        objectHeader = currentActivity.findViewById(R.id.objectHeader)
        val dataValue = productCategoryEntity.getOptionalValue(ProductCategory.categoryName)

        objectHeader?.let {
            it.apply {
                headline = dataValue?.toString()
                subheadline = EntityKeyUtil.getOptionalEntityKey(productCategoryEntity)
                body = "You can set the header body text here."
                footnote = "You can set the header footnote here."
                description = "You can add a detailed item description here."
            }
            it.setTag("#tag1", 0)
            it.setTag("#tag3", 2)
            it.setTag("#tag2", 1)

            setDetailImage(it, productCategoryEntity)
            it.visibility = View.VISIBLE
        }
    }
}
