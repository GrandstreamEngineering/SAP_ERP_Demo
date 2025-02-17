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

package com.gs.erp.sap.demo.viewmodel.product

import android.app.Application
import android.os.Parcelable

import com.gs.erp.sap.demo.viewmodel.EntityViewModel
import com.sap.cloud.android.odata.com_sap_mbtepmdemoservice.Product
import com.sap.cloud.android.odata.com_sap_mbtepmdemoservice.Com_sap_mbtepmdemoServiceMetadata

/*
 * Represents View model for Product
 *
 * Having an entity view model for each <T> allows the ViewModelProvider to cache and return the view model of that
 * type. This is because the ViewModelStore of ViewModelProvider cannot not be able to tell the difference between
 * EntityViewModel<type1> and EntityViewModel<type2>.
 */
class ProductViewModel(application: Application): EntityViewModel<Product>(application, Com_sap_mbtepmdemoServiceMetadata.EntitySets.products, Product.category) {
    /**
     * Constructor for a specific view model with navigation data.
     * @param [navigationPropertyName] - name of the navigation property
     * @param [entityData] - parent entity (starting point of the navigation)
     */
    constructor(application: Application, navigationPropertyName: String, entityData: Parcelable): this(application) {
        this.repository = EntityViewModel<Product>(application, Com_sap_mbtepmdemoServiceMetadata.EntitySets.products, Product.category, navigationPropertyName, entityData).repository
    }
}
