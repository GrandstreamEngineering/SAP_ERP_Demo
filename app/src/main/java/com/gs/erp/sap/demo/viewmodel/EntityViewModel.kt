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
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.os.Parcelable
import com.gs.erp.sap.demo.R
import com.gs.erp.sap.demo.app.SAPWizardApplication
import com.gs.erp.sap.demo.archcomp.SingleLiveEvent
import com.gs.erp.sap.demo.repository.OperationResult
import com.gs.erp.sap.demo.repository.Repository
import com.sap.cloud.mobile.odata.EntitySet
import com.sap.cloud.mobile.odata.EntityType
import com.sap.cloud.mobile.odata.EntityValue
import com.sap.cloud.mobile.odata.Property
import com.sap.cloud.mobile.odata.StreamBase
import java.util.ArrayList


/**
 * Generic type representing the view model with type being one of the entity types.
 *
 * Each entity type has its own view model. An entity set is a collection of entities of an entity set. View model
 * exposed the list of entities as LiveData and four events (CRUD) as SingleLiveEvent acquired from the repository for
 * the entity type. This class extends AndroidViewModel so it has access to the application object.
 *
 * @param [application] required as it extends AndroidViewModel
 */
open class EntityViewModel<T : EntityValue>(application: Application) : AndroidViewModel(application) {
    /** Event to notify of async completion of read/query operation */
    val readResult: SingleLiveEvent<OperationResult<T>> by lazy { repository.readResult }

    /** Event to notify of async completion of create operation */
    val createResult: SingleLiveEvent<OperationResult<T>> by lazy { repository.createResult }

    /** Event to notify of async completion of update operation */
    val updateResult: SingleLiveEvent<OperationResult<T>> by lazy { repository.updateResult }

    /** Event to notify of async completion of delete operation */
    val deleteResult: SingleLiveEvent<OperationResult<T>> by lazy { repository.deleteResult }

    /** LiveData of entities for this entity set */
    val observableItems: LiveData<List<T>> by lazy { repository.observableEntities }

    /** Repository of type of the entity type for a specified entity set */
    lateinit var  repository: Repository<T>

    /*
     * Selected items via long press action
     */
    private val selectedItems = ArrayList<T>()

    /*
     * Identifier of item in focus via click action
     */
    var inFocusId: Long = 0
    
    /*
     * Flag for avoiding continuous retry
     */
    private var isDownloadError = false

    /**
     * Creates a view model with navigation information
     * @param application required as it extends AndroidViewModel
     * @param entitySet entity set that this view represents
     * @param orderByProperty property used for ordering the entity list
     */
    constructor(application: Application, entitySet: EntitySet, orderByProperty: Property?) :  this (application) {
        repository = (application as SAPWizardApplication).repositoryFactory.getRepository(entitySet!!, orderByProperty) as Repository<T>
        repository.clear()
    }

    /**
     * Creates a view model with navigation information
     * @param application required as it extends AndroidViewModel
     * @param entitySet entity set that this view represents
     * @param orderByProperty property used for ordering the entity list
     * @param navigationPropertyName name of the navigation property
     * @param parentEntity parent entity
     */
    constructor(application: Application, entitySet: EntitySet, orderByProperty: Property?, navigationPropertyName: String, parentEntity: Parcelable) : this (application, entitySet, orderByProperty) {
        repository = (application as SAPWizardApplication)
            .repositoryFactory
            .getRepository(parentEntity as EntityValue, navigationPropertyName, entitySet, orderByProperty) as Repository<T>
        repository.clear()
        repository.read(parentEntity as EntityValue, navigationPropertyName)
    }


    fun create(entity: T) {
        repository.create(entity)
    }

    fun create(entity: T, media: StreamBase) {
        repository.create(entity, media)
    }

    fun refresh() {
        repository.read()
    }

    fun refresh(searchText: String) {
        repository.read(searchText)
    }
    
    fun clear() {
        repository.clear()
    }

    fun refresh(parent: EntityValue, navPropName: String) {
        repository.read(parent, navPropName)
    }

    fun update(entity: T) {
        repository.update(entity)
    }

    fun delete(entities: MutableList<T>) {
        repository.delete(entities)
    }

    fun deleteSelected() {
        repository.delete(selectedItems)
    }


    /**
     * Perform initial read of repository. However, if data is already available, read is not be performed.
     */
    fun initialRead(onError: ((errorMessage: String) -> Unit)? = null) {
        if (!isDownloadError) {
            repository.initialRead(
                { isDownloadError = false},
                {
                    isDownloadError = true
                    onError?.let {
                        val resources = getApplication<Application>().resources
                        onError.invoke(resources.getString(R.string.read_failed_detail))
                    }
                }
            )
        }
    }

    /*
     * For management of items selected via long press action
     */
    fun addSelected(selected: T) {
        var found = false
        for (item in selectedItems) {
            if (item == selected) {
                found = true
                break
            }
        }
        if (!found) {
            selectedItems.add(selected)
        }
    }

    fun removeSelected(selected: T) {
        if (selectedItems.contains(selected)) {
            selectedItems.remove(selected)
        }
    }

    fun getSelected(index: Int): T? {
        return if (index >= selectedItems.size || index < 0) {
            null
        } else selectedItems[index]
    }

    fun removeAllSelected() {
        selectedItems.clear()
    }

    fun numberOfSelected(): Int {
        return selectedItems.size
    }

    fun selectedContains(member: T): Boolean {
        return selectedItems.contains(member)
    }

    /** The observable data for the selection in the list */
    val selectedEntity: MutableLiveData<T> = MutableLiveData<T>()
    fun setSelectedEntity(v: T) {
        selectedEntity.value = v
    }
}
