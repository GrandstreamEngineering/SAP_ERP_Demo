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

package com.gs.erp.sap.demo.repository
import com.gs.erp.sap.demo.service.SAPServiceManager

import com.sap.cloud.android.odata.com_sap_mbtepmdemoservice.Com_sap_mbtepmdemoServiceMetadata
import com.sap.cloud.android.odata.com_sap_mbtepmdemoservice.Customer
import com.sap.cloud.android.odata.com_sap_mbtepmdemoservice.ProductCategory
import com.sap.cloud.android.odata.com_sap_mbtepmdemoservice.ProductText
import com.sap.cloud.android.odata.com_sap_mbtepmdemoservice.Product
import com.sap.cloud.android.odata.com_sap_mbtepmdemoservice.PurchaseOrderHeader
import com.sap.cloud.android.odata.com_sap_mbtepmdemoservice.PurchaseOrderItem
import com.sap.cloud.android.odata.com_sap_mbtepmdemoservice.SalesOrderHeader
import com.sap.cloud.android.odata.com_sap_mbtepmdemoservice.SalesOrderItem
import com.sap.cloud.android.odata.com_sap_mbtepmdemoservice.Stock
import com.sap.cloud.android.odata.com_sap_mbtepmdemoservice.Supplier

import com.sap.cloud.mobile.odata.EntitySet
import com.sap.cloud.mobile.odata.EntityValue
import com.sap.cloud.mobile.odata.Property

import java.util.WeakHashMap

/*
 * Repository factory to construct repository for an entity set
 */
class RepositoryFactory
/**
 * Construct a RepositoryFactory instance. There should only be one repository factory and used
 * throughout the life of the application to avoid caching entities multiple times.
 * @param sapServiceManager - Service manager for interaction with OData service
 */
(private val sapServiceManager: SAPServiceManager?) {
    private val repositories: WeakHashMap<String, Repository<out EntityValue>> = WeakHashMap()
    private val containmentEntityRepository: WeakHashMap<String, Repository<out EntityValue>> = WeakHashMap()

    /**
     * Construct or return an existing repository for the specified entity set
     * @param entitySet - entity set for which the repository is to be returned
     * @param orderByProperty - if specified, collection will be sorted ascending with this property
     * @return a repository for the entity set
     */
    fun getRepository(entitySet: EntitySet, orderByProperty: Property?): Repository<out EntityValue> {
        val com_sap_mbtepmdemoService = sapServiceManager?.com_sap_mbtepmdemoService
        val key = entitySet.localName
        var repository: Repository<out EntityValue>? = repositories[key]
        if (repository == null) {
            repository = when (key) {
                Com_sap_mbtepmdemoServiceMetadata.EntitySets.customers.localName -> Repository<Customer>(com_sap_mbtepmdemoService!!, Com_sap_mbtepmdemoServiceMetadata.EntitySets.customers, orderByProperty)
                Com_sap_mbtepmdemoServiceMetadata.EntitySets.productCategories.localName -> Repository<ProductCategory>(com_sap_mbtepmdemoService!!, Com_sap_mbtepmdemoServiceMetadata.EntitySets.productCategories, orderByProperty)
                Com_sap_mbtepmdemoServiceMetadata.EntitySets.productTexts.localName -> Repository<ProductText>(com_sap_mbtepmdemoService!!, Com_sap_mbtepmdemoServiceMetadata.EntitySets.productTexts, orderByProperty)
                Com_sap_mbtepmdemoServiceMetadata.EntitySets.products.localName -> Repository<Product>(com_sap_mbtepmdemoService!!, Com_sap_mbtepmdemoServiceMetadata.EntitySets.products, orderByProperty)
                Com_sap_mbtepmdemoServiceMetadata.EntitySets.purchaseOrderHeaders.localName -> Repository<PurchaseOrderHeader>(com_sap_mbtepmdemoService!!, Com_sap_mbtepmdemoServiceMetadata.EntitySets.purchaseOrderHeaders, orderByProperty)
                Com_sap_mbtepmdemoServiceMetadata.EntitySets.purchaseOrderItems.localName -> Repository<PurchaseOrderItem>(com_sap_mbtepmdemoService!!, Com_sap_mbtepmdemoServiceMetadata.EntitySets.purchaseOrderItems, orderByProperty)
                Com_sap_mbtepmdemoServiceMetadata.EntitySets.salesOrderHeaders.localName -> Repository<SalesOrderHeader>(com_sap_mbtepmdemoService!!, Com_sap_mbtepmdemoServiceMetadata.EntitySets.salesOrderHeaders, orderByProperty)
                Com_sap_mbtepmdemoServiceMetadata.EntitySets.salesOrderItems.localName -> Repository<SalesOrderItem>(com_sap_mbtepmdemoService!!, Com_sap_mbtepmdemoServiceMetadata.EntitySets.salesOrderItems, orderByProperty)
                Com_sap_mbtepmdemoServiceMetadata.EntitySets.stock.localName -> Repository<Stock>(com_sap_mbtepmdemoService!!, Com_sap_mbtepmdemoServiceMetadata.EntitySets.stock, orderByProperty)
                Com_sap_mbtepmdemoServiceMetadata.EntitySets.suppliers.localName -> Repository<Supplier>(com_sap_mbtepmdemoService!!, Com_sap_mbtepmdemoServiceMetadata.EntitySets.suppliers, orderByProperty)
                else -> throw AssertionError("Fatal error, entity set[$key] missing in generated code")
            }
            repositories[key] = repository
        }
        return repository
    }

    /**
     * Construct or return an existing repository for the specified entity set
     * @param entitySet - entity set for which the repository is to be returned
     * @param orderByProperty - if specified, collection will be sorted ascending with this property
     * @return a repository for the entity set
     */
    fun getRepository(parentEntity: EntityValue, navigationPropertyName: String, entitySet: EntitySet, orderByProperty: Property?): Repository<out EntityValue> {
        val com_sap_mbtepmdemoService = sapServiceManager?.com_sap_mbtepmdemoService
        val key = entitySet.localName  + "_" + parentEntity.entityID + "_" +  navigationPropertyName
        var repository: Repository<out EntityValue>? = repositories[key]
        if (repository == null) {
            repository = when (entitySet.localName ) {
                Com_sap_mbtepmdemoServiceMetadata.EntitySets.customers.localName -> Repository<Customer>(com_sap_mbtepmdemoService!!, entitySet, parentEntity, navigationPropertyName, orderByProperty)
                Com_sap_mbtepmdemoServiceMetadata.EntitySets.productCategories.localName -> Repository<ProductCategory>(com_sap_mbtepmdemoService!!, entitySet, parentEntity, navigationPropertyName, orderByProperty)
                Com_sap_mbtepmdemoServiceMetadata.EntitySets.productTexts.localName -> Repository<ProductText>(com_sap_mbtepmdemoService!!, entitySet, parentEntity, navigationPropertyName, orderByProperty)
                Com_sap_mbtepmdemoServiceMetadata.EntitySets.products.localName -> Repository<Product>(com_sap_mbtepmdemoService!!, entitySet, parentEntity, navigationPropertyName, orderByProperty)
                Com_sap_mbtepmdemoServiceMetadata.EntitySets.purchaseOrderHeaders.localName -> Repository<PurchaseOrderHeader>(com_sap_mbtepmdemoService!!, entitySet, parentEntity, navigationPropertyName, orderByProperty)
                Com_sap_mbtepmdemoServiceMetadata.EntitySets.purchaseOrderItems.localName -> Repository<PurchaseOrderItem>(com_sap_mbtepmdemoService!!, entitySet, parentEntity, navigationPropertyName, orderByProperty)
                Com_sap_mbtepmdemoServiceMetadata.EntitySets.salesOrderHeaders.localName -> Repository<SalesOrderHeader>(com_sap_mbtepmdemoService!!, entitySet, parentEntity, navigationPropertyName, orderByProperty)
                Com_sap_mbtepmdemoServiceMetadata.EntitySets.salesOrderItems.localName -> Repository<SalesOrderItem>(com_sap_mbtepmdemoService!!, entitySet, parentEntity, navigationPropertyName, orderByProperty)
                Com_sap_mbtepmdemoServiceMetadata.EntitySets.stock.localName -> Repository<Stock>(com_sap_mbtepmdemoService!!, entitySet, parentEntity, navigationPropertyName, orderByProperty)
                Com_sap_mbtepmdemoServiceMetadata.EntitySets.suppliers.localName -> Repository<Supplier>(com_sap_mbtepmdemoService!!, entitySet, parentEntity, navigationPropertyName, orderByProperty)
                else -> throw AssertionError("Fatal error, entity set[$key] missing in generated code")
            }
            repositories[key] = repository
        }
        return repository
    }


    /**
     * Get rid of all cached repositories
     */
    fun reset() {
        repositories.clear()
        containmentEntityRepository.clear()
    }
}
