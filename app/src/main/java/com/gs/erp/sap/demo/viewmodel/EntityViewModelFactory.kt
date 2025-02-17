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

package com.gs.erp.sap.demo.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.os.Parcelable

import com.gs.erp.sap.demo.viewmodel.customer.CustomerViewModel
import com.gs.erp.sap.demo.viewmodel.productcategory.ProductCategoryViewModel
import com.gs.erp.sap.demo.viewmodel.producttext.ProductTextViewModel
import com.gs.erp.sap.demo.viewmodel.product.ProductViewModel
import com.gs.erp.sap.demo.viewmodel.purchaseorderheader.PurchaseOrderHeaderViewModel
import com.gs.erp.sap.demo.viewmodel.purchaseorderitem.PurchaseOrderItemViewModel
import com.gs.erp.sap.demo.viewmodel.salesorderheader.SalesOrderHeaderViewModel
import com.gs.erp.sap.demo.viewmodel.salesorderitem.SalesOrderItemViewModel
import com.gs.erp.sap.demo.viewmodel.stock.StockViewModel
import com.gs.erp.sap.demo.viewmodel.supplier.SupplierViewModel

/**
 * Custom factory class, which can create view models for entity subsets, which are
 * reached from a parent entity through a navigation property.
 *
 * @param application parent application
 * @param navigationPropertyName name of the navigation link
 * @param entityData parent entity
 */
class EntityViewModelFactory (
        val application: Application, // name of the navigation property
        val navigationPropertyName: String, // parent entity
        val entityData: Parcelable) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass.simpleName) {
    			"CustomerViewModel" -> CustomerViewModel(application, navigationPropertyName, entityData) as T
                    			"ProductCategoryViewModel" -> ProductCategoryViewModel(application, navigationPropertyName, entityData) as T
                    			"ProductTextViewModel" -> ProductTextViewModel(application, navigationPropertyName, entityData) as T
                    			"ProductViewModel" -> ProductViewModel(application, navigationPropertyName, entityData) as T
                    			"PurchaseOrderHeaderViewModel" -> PurchaseOrderHeaderViewModel(application, navigationPropertyName, entityData) as T
                    			"PurchaseOrderItemViewModel" -> PurchaseOrderItemViewModel(application, navigationPropertyName, entityData) as T
                    			"SalesOrderHeaderViewModel" -> SalesOrderHeaderViewModel(application, navigationPropertyName, entityData) as T
                    			"SalesOrderItemViewModel" -> SalesOrderItemViewModel(application, navigationPropertyName, entityData) as T
                    			"StockViewModel" -> StockViewModel(application, navigationPropertyName, entityData) as T
                    			else -> SupplierViewModel(application, navigationPropertyName, entityData) as T
        }
    }
}
