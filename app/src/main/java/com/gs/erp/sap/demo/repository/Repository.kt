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

import android.util.Log
import androidx.lifecycle.MutableLiveData

import com.gs.erp.sap.demo.archcomp.SingleLiveEvent
import com.gs.erp.sap.demo.mediaresource.EntityMediaResource
import com.gs.erp.sap.demo.repository.OperationResult.Operation

import com.sap.cloud.android.odata.com_sap_mbtepmdemoservice.Com_sap_mbtepmdemoService

import com.sap.cloud.mobile.odata.ChangeSet
import com.sap.cloud.mobile.odata.DataQuery
import com.sap.cloud.mobile.odata.DataType
import com.sap.cloud.mobile.odata.EntitySet
import com.sap.cloud.mobile.odata.EntityValue
import com.sap.cloud.mobile.odata.EntityType
import com.sap.cloud.mobile.odata.EntityValueList
import com.sap.cloud.mobile.odata.Property
import com.sap.cloud.mobile.odata.RequestOptions
import com.sap.cloud.mobile.odata.SortOrder
import com.sap.cloud.mobile.odata.StreamBase
import com.sap.cloud.mobile.odata.UpdateMode
import com.sap.cloud.mobile.odata.core.Action0
import com.sap.cloud.mobile.odata.core.Action1
import com.sap.cloud.mobile.odata.http.HttpHeaders

import java.util.ArrayList
import org.slf4j.LoggerFactory

/**
 * Generic type representing repository with type being one of the entity types.
 * In other words, each entity type has its own repository and an in-memory store of all the entities
 * of that type.
 * Repository exposed the list of entities as LiveData and four events (CRUD) as SingleLiveEvent
 * @param com_sap_mbtepmdemoService OData service
 * @param orderByProperty used to order the collection retrieved from OData service
 */
class Repository<T : EntityValue>(
        private val com_sap_mbtepmdemoService: Com_sap_mbtepmdemoService,
        private val orderByProperty: Property?) {
    /*
     * Indicate if metadata=full parameter needs to be set during query for the entity set
     * V4 and higher OData version services do not return metadata as part of the result preventing the
     * the construction of download url for use by Glide.
     */
    private var needFullMetadata = false

    /*
     * parent entity value
     */
    private var parentEntity: EntityValue? = null

    /*
     * navigation property name of parent entity
     */
    private var navigationPropertyName: String? = null

    /*
     * entity set associated with this repository
     */
    private lateinit var entitySet: EntitySet

    /*
     * Cache is only in-memory but can be extended to persist to avoid fetching on application re-launches
     */
    private val entities: MutableList<T>
    
    /*
     * Cache for the related entities
     */
    private var relatedEntities: MutableList<T>

    /*
     * LiveData for the list of entities returned by OData service for this entity set
     */
    val observableEntities: MutableLiveData<List<T>>

    /*
     * Event to notify of async completion of create operation
     */
    val createResult = SingleLiveEvent<OperationResult<T>>()

    /*
     * Event to notify of async completion of read/query operation
     */
    val readResult = SingleLiveEvent<OperationResult<T>>()

    /*
     * Event to notify of async completion of update operation
     */
    val updateResult = SingleLiveEvent<OperationResult<T>>()

    /*
     * Event to notify of async completion of delete operation
     */
    val deleteResult = SingleLiveEvent<OperationResult<T>>()

    /**
     * Flag to indicate if repository has been populated with an initial read
     */
    private var initialReadDone: Boolean = false

    /**
     * Return a suitable HttpHeader based on whether full metadata parameter is required
     * @return HttpHeader for query
     */
    private val httpHeaders: HttpHeaders
        get() {
            val httpHeaders: HttpHeaders
            if (needFullMetadata) {
                httpHeaders = HttpHeaders()
                httpHeaders.set("Accept", "application/json;odata.metadata=full")
            } else {
                httpHeaders = HttpHeaders.empty
            }
            return httpHeaders
        }

    init {
        entities = ArrayList()
        relatedEntities = ArrayList()
        observableEntities = MutableLiveData()
    }

    /**
     * Creates a view model with navigation information
     * @param com_sap_mbtepmdemoService implementation class of DataService
     * @param entitySet entity set that this view represents
     * @param orderByProperty property used for ordering the entity list
     */
    constructor(com_sap_mbtepmdemoService: Com_sap_mbtepmdemoService, entitySet: EntitySet, orderByProperty: Property?) : this (com_sap_mbtepmdemoService, orderByProperty) {
        this.entitySet = entitySet
        if (EntityMediaResource.isV4(com_sap_mbtepmdemoService.metadata.versionCode) && EntityMediaResource.hasMediaResources(entitySet)) {
            this.needFullMetadata = true
        }
    }

    /**
     * Creates a view model with navigation information
     * @param com_sap_mbtepmdemoService implementation class of DataService
     * @param parentEntity parent entity value
     * @param navigationPropertyName navigation property name used for loading child data
     * @param orderByProperty property used for ordering the entity list
     */
    constructor(com_sap_mbtepmdemoService: Com_sap_mbtepmdemoService, entitySet: EntitySet, parentEntity: EntityValue, navigationPropertyName: String, orderByProperty: Property?) : this (com_sap_mbtepmdemoService, orderByProperty) {
        this.parentEntity = parentEntity
        this.navigationPropertyName = navigationPropertyName
        if (EntityMediaResource.isV4(com_sap_mbtepmdemoService.metadata.versionCode) && EntityMediaResource.hasMediaResources(entitySet)) {
            this.needFullMetadata = true
        }
    }


    /*
     * For convenience of code generation, read is implemented using dynamic API.
     * However, if we are to create an entity set specific repository, it is highly recommended that
     * the generated getters for the entity set being utilized as they return strongly type proxy class
     * that will simplify consumption. For example, get<entity set>Async should be used to simplify the
     * implementation for read when we are dealing the entity type associated with the entity set.
     *
     * Read method to retrieve all entities of the entity set
     */
    @Suppress("UNCHECKED_CAST")
    fun read() {
        relatedEntities.clear()

        if (parentEntity != null && navigationPropertyName != null) {
            read(parentEntity!!, navigationPropertyName!!)
        } else {
            var dataQuery = DataQuery().from(entitySet)
            if (!entitySet.isSingleton && orderByProperty != null) {
                dataQuery = dataQuery.orderBy(orderByProperty, SortOrder.ASCENDING)
            }

            com_sap_mbtepmdemoService.executeQueryAsync(dataQuery,
                { result ->
                    val entitiesRead = this.convert(result.entityList)
                    entities.clear()
                    entities.addAll(entitiesRead)
                    // Update observables
                    observableEntities.value = entitiesRead
                    val operationResult: OperationResult<T> = OperationResult(Operation.READ)
                    readResult.setValue(operationResult)
                },
                { error ->
                    LOGGER.debug("Error encountered during fetch of Category collection", error)
                    val operationResult: OperationResult<T> = OperationResult(error, Operation.READ)
                    readResult.setValue(operationResult)
                },
                httpHeaders)
        }

    }

    fun read(searchText: String) {
        relatedEntities.clear()

        if (parentEntity != null && navigationPropertyName != null) {
            read(parentEntity!!, navigationPropertyName!!)
        } else {
            var dataQuery = DataQuery().from(entitySet)
            if (!entitySet.isSingleton && orderByProperty != null) {
                dataQuery = dataQuery.orderBy(orderByProperty, SortOrder.ASCENDING)
            }
//            dataQuery.setSearchText(searchText)
            Log.d("xxxx", "read to query with dataQuery...:" + dataQuery
                    + ", parentEntity:" +  parentEntity + ", navigationPropertyName:" + navigationPropertyName
                    + ", entitySet:" + entitySet + ", orderByProperty:" + orderByProperty)

            com_sap_mbtepmdemoService.executeQueryAsync(dataQuery,
                { result ->
                    val entitiesRead = this.convert(result.entityList)
                    entities.clear()
                    for (entity in entitiesRead) {
                        if (entity.toString().contains(searchText)) {
                            entities.add(entity)
                        }
                    }
//                    entities.addAll(entitiesRead)

                    // Update observables
                    observableEntities.value = entities
                    android.util.Log.d("xxxx", "read entity get:" + entities)
                    val operationResult: OperationResult<T> = OperationResult(Operation.READ)
                    readResult.setValue(operationResult)
                },
                { error ->
                    LOGGER.debug("Error encountered during fetch of Category collection", error)
                    val operationResult: OperationResult<T> = OperationResult(error, Operation.READ)
                    readResult.setValue(operationResult)
                },
                HttpHeaders.empty)
        }

    }

    /**
     * This version of the read operation is used to get the related objects to a
     * given entity.
     *
     * @param parent - the original entity, the starting point of the navigation
     * @param navigationPropertyName - the name of the link to the related entity set
     */
    @Suppress("UNCHECKED_CAST")
    fun read(parent: EntityValue, navigationPropertyName: String) {
        relatedEntities.clear()
    
        val navigationProperty = parent.entityType.getProperty(navigationPropertyName)
        var dataQuery = DataQuery()
        if (!parent.entitySet.isSingleton && navigationProperty.isCollection && orderByProperty != null) {
            dataQuery = dataQuery.orderBy(orderByProperty, SortOrder.ASCENDING)
        }
        com_sap_mbtepmdemoService.loadPropertyAsync(navigationProperty, parent, dataQuery,
            {
                val relatedData = parent.getOptionalValue(navigationProperty)
                relatedEntities = ArrayList()
                when (navigationProperty.dataType.code) {
                    DataType.ENTITY_VALUE_LIST -> relatedEntities = this.convert((relatedData as EntityValueList?)!!)
                    DataType.ENTITY_VALUE -> if (relatedData != null) {
                        val entity = relatedData as EntityValue?
                        relatedEntities.add(entity as T)
                    }
                }
                initialReadDone = true
                
                // Update observables
                observableEntities.value = relatedEntities
                val operationResult = OperationResult<T>(Operation.READ)
                readResult.setValue(operationResult)
            },
            { error ->
                LOGGER.debug("Error encountered during fetch of Category collection", error)
                val operationResult: OperationResult<T> = OperationResult(error, Operation.READ)
                readResult.setValue(operationResult)
            },
            httpHeaders)
    }

    private fun createSuccessHandler(newEntity: T) = Action0 {
        insertToCache(newEntity, entities)
        if (relatedEntities.isNotEmpty()) {
            insertToCache(newEntity, relatedEntities)
            observableEntities.value = relatedEntities
        } else {
            observableEntities.value = entities
        }

        val operationResult: OperationResult<T> = OperationResult(newEntity, Operation.CREATE)
        createResult.setValue(operationResult)
    }

    private fun createFailureHandler(hintMessage: String) = Action1<RuntimeException> {
        LOGGER.debug(hintMessage, it)
        val operationResult: OperationResult<T> = OperationResult(it, Operation.CREATE)
        createResult.setValue(operationResult)
    }

    private fun createEntity(newEntity: T, media: StreamBase? = null, parent: EntityValue? = null, navPropName: String? = null) {
        val createWithMediaFailureHintMessage = "Media Linked Entity creation failed: "
        val createFailureHintMessage = "Entity creation failed: "
        val isSingleton = newEntity.entitySet.isSingleton
        val navProp = navPropName?.let { parent?.entityType?.getProperty(it) }
        if (isSingleton) {
            navProp?.also {
                com_sap_mbtepmdemoService.metadata.resolveEntity(parent!!)
                newEntity.parentEntity = parent
                newEntity.parentProperty = it
            }
            val requestOptions = RequestOptions()
            requestOptions.updateMode = UpdateMode.REPLACE
            com_sap_mbtepmdemoService.updateEntityAsync(newEntity,
                media?.let {
                    Action0 {
                        com_sap_mbtepmdemoService.uploadMediaAsync(newEntity,
                            it,
                            createSuccessHandler(newEntity),
                            createFailureHandler(createWithMediaFailureHintMessage)
                        )
                    }
                } ?: createSuccessHandler(newEntity),
                createFailureHandler(createFailureHintMessage),
                httpHeaders,
                requestOptions)
        } else {
            navProp?.also {
                media?.also {
                    com_sap_mbtepmdemoService.createRelatedMediaAsync(newEntity,
                        media,
                        parent!!,
                        navProp,
                        createSuccessHandler(newEntity),
                        createFailureHandler(createWithMediaFailureHintMessage)
                    )
                } ?: com_sap_mbtepmdemoService.createRelatedEntityAsync(newEntity,
                        parent!!,
                        navProp,
                        createSuccessHandler(newEntity),
                        createFailureHandler(createFailureHintMessage)
                    )
            } ?: run {
                media?.also {
                    com_sap_mbtepmdemoService.createMediaAsync(newEntity,
                        it,
                        createSuccessHandler(newEntity),
                        createFailureHandler(createWithMediaFailureHintMessage)
                    )
                } ?: com_sap_mbtepmdemoService.createEntityAsync(newEntity,
                        createSuccessHandler(newEntity),
                        createFailureHandler(createFailureHintMessage)
                    )
            }
        }
    }

    /**
     * Create method for Entity type that is a Media Linked Entity
     * caller must provide the media resource associated with the MLE
     * @param newEntity - the MLE entity instance
     * @param media - byte or character stream of the media resource
     */
    fun create(newEntity: T, media: StreamBase) {
        if (newEntity.entityType.isMedia) {
            if(parentEntity != null && navigationPropertyName != null){
                createRelatedEntity(newEntity, media)
            } else {
                createEntity(newEntity, media)
            }
        }
    }

    /**
     * Create method for the entity set
     * @param newEntity - entity to create
     */
    fun create(newEntity: T) {
        if (newEntity.entityType.isMedia) {
            val operationResult: OperationResult<T> = OperationResult(IllegalStateException("Specify media resource for Media Linked Entity"), Operation.CREATE)
            createResult.value = operationResult
            return
        }
        if(parentEntity != null && navigationPropertyName != null){
            createRelatedEntity(newEntity)
        } else {
            createEntity(newEntity)
        }
    }

    /**
     * Create method for entity type that is a Media Linked Entity
     * caller must provide the media resource associated with the MLE
     * @param newEntity - the MLE entity instance
     * @param media - byte or character stream of the media resource
     */
    fun createRelatedEntity(newEntity: T, media: StreamBase) {
        if (newEntity.entityType.isMedia) {
            createEntity(newEntity, media, parentEntity!!, navigationPropertyName!!)
        }
    }

    /**
     * Create method for the entity value
     * @param newEntity - child entity to create based on its parent
     */
    fun createRelatedEntity(newEntity: T) {
        if (newEntity.entityType.isMedia) {
            val operationResult: OperationResult<T> = OperationResult(IllegalStateException("Specify media resource for Media Linked Entity"), Operation.CREATE)
            createResult.value = operationResult
            return
        }
        createEntity(newEntity = newEntity, parent = parentEntity!!, navPropName = navigationPropertyName!!)
    }

    /**
     * Update method for the entity set
     * @param updateEntity - entity to update
     */
    fun update(updateEntity: T) {
        com_sap_mbtepmdemoService.updateEntityAsync(updateEntity,
            {
                replaceInCache(updateEntity, entities)
                if (relatedEntities.isNotEmpty()) {
                    replaceInCache(updateEntity, relatedEntities)
                    observableEntities.value = relatedEntities
                } else {
                    observableEntities.value = entities
                }

                val operationResult: OperationResult<T> = OperationResult(updateEntity, Operation.UPDATE)
                updateResult.setValue(operationResult)
            },
            { error ->
                LOGGER.debug("Error encountered during update of entity", error)
                val operationResult: OperationResult<T> = OperationResult(error, Operation.UPDATE)
                updateResult.setValue(operationResult)
            })
    }

    /**
     * Delete method for the entity set
     * @param deleteEntities - list of entities to be deleted
     *
     * Implementation uses a ChangeSet to guarantee that either all specified entities are deleted or none
     * For best effort delete, multiple ChangeSets within a Batch can be used
     */
    fun delete(deleteEntities: MutableList<T>) {
        val deleteChangeSet = ChangeSet()
        for (entityToDelete in deleteEntities) {
            deleteChangeSet.deleteEntity(entityToDelete)
        }
        com_sap_mbtepmdemoService.applyChangesAsync(deleteChangeSet,
            {
                // Change Set success means all deletes are completed
                for (entityToDelete in deleteEntities) {
                    if (relatedEntities.isNotEmpty()) {
                        removeFromCache(entityToDelete, relatedEntities)
                    }
                    removeFromCache(entityToDelete, entities)
                }
                
                if (relatedEntities.isNotEmpty()) {
                    observableEntities.value = relatedEntities
                } else {
                    observableEntities.value = entities
                }

                val operationResult: OperationResult<T> = OperationResult(deleteEntities, Operation.DELETE)
                deleteResult.setValue(operationResult)
            },
            { error ->
                LOGGER.debug("Error encountered during deletion of entities:", error)
                    val operationResult: OperationResult<T> = OperationResult(error, Operation.DELETE)
                    deleteResult.setValue(operationResult)
            })
    }


    /**
     * For use by View Model to populate the repository. Only if an initial read has not been done will
     * an attempt be made to read in data from the collection.
     * @param failureHandler
     */
    fun initialRead(successHandler: Action0?, failureHandler: Action1<RuntimeException>) {
        relatedEntities.clear()
    
        if (initialReadDone && entities.isNotEmpty()) {
            observableEntities.value = entities
            return
        }

        var dataQuery = DataQuery().from(entitySet)
        if (!entitySet.isSingleton && orderByProperty != null) {
            dataQuery = dataQuery.orderBy(orderByProperty, SortOrder.ASCENDING)
        }

        com_sap_mbtepmdemoService.executeQueryAsync(dataQuery,
            { queryResult ->
                val entitiesRead = convert(queryResult.entityList)
                entities.clear()
                entities.addAll(entitiesRead)
                initialReadDone = true
                observableEntities.value = entitiesRead
                successHandler?.call()
            },
            failureHandler,
            httpHeaders)
    }

    /*
     * A simple function to convert from generic EntityValueList to type specified list
     */
    @Suppress("UNCHECKED_CAST")
    private fun convert(entityValueList: EntityValueList): ArrayList<T> {
        val result = ArrayList<T>(entityValueList.length())
        val iterator = entityValueList.iterator()
        while (iterator.hasNext()) {
            result.add(iterator.next() as T)
        }
        return result
    }

    /**
     * Insert the new entity into cache and in order if needed
     * @param newEntity
     * @param cache
     */
    private fun insertToCache(newEntity: T, cache: MutableList<T>) {
        if (orderByProperty != null) {
            insertOrderByProperty(newEntity, cache)
        } else {
            cache.add(0, newEntity)
        }
    }

    /**
     * Replace the entity in cache that has the same key(s) of the updated entity
     * Since we do not know if the value for order by property has been change, we have to do remove
     * followed by insert
     * Note: implementation should be optimized to obtain better than linear scaling for very large collection
     *
     * @param updateEntity - updated entity to be replaced with
     * @param cache
     */
    private fun replaceInCache(updateEntity: T, cache: MutableList<T>) {
        var index = 0
        for (entity in cache) {
            if (EntityValue.equalKeys(entity, updateEntity)) {
                if (orderByProperty != null) {
                    cache.removeAt(index)
                    insertOrderByProperty(updateEntity, cache)
                } else {
                    cache[index] = updateEntity
                }
                break
            }
            index++
        }
    }

    /**
     * Remove the specified entity from cache
     * Note: implementation should be optimized to obtain better than linear scaling for very large collection
     *
     * @param deleteEntity - deleted entity to be removed from cache
     * @param cache
     */
    private fun removeFromCache(deleteEntity: T, cache: MutableList<T>) {
        var index = 0
        for (entity in cache) {
            if (EntityValue.equalKeys(entity, deleteEntity)) {
                cache.removeAt(index)
                break
            }
            index++
        }
    }

    /**
     * Insert the new entity into the cache list based on list is sorted in ascending order
     * It is possible that we have a null for the value of the order by property. In that case
     * we will assign a default string.
     * @param entity to insert
     * @param cache
     */
    private fun insertOrderByProperty(entity: T, cache: MutableList<T>) {
        val propertyValue = entity.getOptionalValue(orderByProperty!!)
        var insertOrderByPropertyString = " "
        var listOrderByPropertyString: String

        if (propertyValue != null) {
            insertOrderByPropertyString = propertyValue.toString()
        }
        for ((index, listEntity) in cache.withIndex()) {
            listOrderByPropertyString = if (listEntity.getOptionalValue(orderByProperty) == null) {
                " "
            } else {
                listEntity.getOptionalValue(orderByProperty)!!.toString()
            }
            if (insertOrderByPropertyString < listOrderByPropertyString) {
                cache.add(index, entity)
                return
            }
        }
        cache.add(entity)
    }

    /**
     * Repository provides an empty data list, but the in-memory cache is retained. Calling
     * read clears the cache, as well.
     */
    fun clear() {
        observableEntities.value = ArrayList<T>()
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(Repository::class.java)
    }
}
