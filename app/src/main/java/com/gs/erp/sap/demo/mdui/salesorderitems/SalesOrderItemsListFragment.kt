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

package com.gs.erp.sap.demo.mdui.salesorderitems

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.CheckBox
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.gs.erp.sap.demo.service.SAPServiceManager
import com.gs.erp.sap.demo.app.SAPWizardApplication
import com.gs.erp.sap.demo.databinding.ElementEntityitemListBinding
import com.gs.erp.sap.demo.databinding.FragmentEntityitemListBinding
import com.gs.erp.sap.demo.R
import com.gs.erp.sap.demo.viewmodel.EntityViewModelFactory
import com.gs.erp.sap.demo.viewmodel.salesorderitem.SalesOrderItemViewModel
import com.gs.erp.sap.demo.repository.OperationResult
import com.gs.erp.sap.demo.mdui.UIConstants
import com.gs.erp.sap.demo.mdui.EntitySetListActivity.EntitySetName
import com.gs.erp.sap.demo.mdui.InterfacedFragment
import com.sap.cloud.android.odata.com_sap_mbtepmdemoservice.Com_sap_mbtepmdemoServiceMetadata.*
import com.sap.cloud.android.odata.com_sap_mbtepmdemoservice.SalesOrderItem
import com.sap.cloud.mobile.fiori.`object`.ObjectCell
import com.sap.cloud.mobile.fiori.`object`.ObjectHeader
import com.sap.cloud.mobile.odata.EntityValue
import android.widget.SearchView
import org.slf4j.LoggerFactory

/**
 * An activity representing a list of SalesOrderItem. This activity has different presentations for handset and tablet-size
 * devices. On handsets, the activity presents a list of items, which when touched, lead to a view representing
 * SalesOrderItem details. On tablets, the activity presents the list of SalesOrderItem and SalesOrderItem details side-by-side using two
 * vertical panes.
 */

class SalesOrderItemsListFragment : InterfacedFragment<SalesOrderItem, FragmentEntityitemListBinding>() {

    /**
     * Service manager to provide root URL of OData Service for Glide to load images if there are media resources
     * associated with the entity type
     */
    private var sapServiceManager: SAPServiceManager? = null

    /**
     * List adapter to be used with RecyclerView containing all instances of salesOrderItems
     */
    private var adapter: SalesOrderItemListAdapter? = null

    private lateinit var refreshLayout: SwipeRefreshLayout
    private var actionMode: ActionMode? = null
    private var isInActionMode: Boolean = false
    private var isNavigationPropertyConnection: Boolean = true
    private val selectedItems = ArrayList<Int>()
    private lateinit var mSearchView: SearchView
    private var searchText: String = ""


    /**
     * View model of the entity type
     */
    private lateinit var viewModel: SalesOrderItemViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityTitle = getString(R.string.eset_salesorderitems)
        menu = R.menu.itemlist_menu
        savedInstanceState?.let {
            isInActionMode = it.getBoolean("ActionMode")
        }

        sapServiceManager = (currentActivity.application as SAPWizardApplication).sapServiceManager
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        currentActivity.findViewById<ObjectHeader>(R.id.objectHeader)?.let {
            it.visibility = View.GONE
        }
        mSearchView = currentActivity.findViewById(R.id.search_view)
        mSearchView.queryHint = "Input to Search..."
        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // 查询提交时的逻辑
//                search(query)
                Log.d("xxxx","query text submit query:" + query)
                searchText = query!!
                refreshListData()
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                // 查询文本变化时的逻辑
//                search(newText)
                Log.d("xxxx","query text update:" + query)
                searchText = query!!
                refreshListData()
                return true
            }
        })
        return fragmentBinding.root
    }

    override fun initBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentEntityitemListBinding.inflate(inflater, container, false)

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.menu_refresh -> {
                refreshLayout.isRefreshing = true
                refreshListData()
                true
            }
            R.id.menu_search -> {
                toggleSearchView()
                android.util.Log.d("xxxx","search menu clicked...")
                true
            }
            else -> return super.onMenuItemSelected(menuItem)
        }
    }
    fun toggleSearchView() {
        if (mSearchView.visibility != View.VISIBLE) {
            Log.d("xxxx","show search view")
            showSearchView()
        } else {
            Log.d("xxxx","hide search view")
            dismissSearchView()
        }
    }

    fun showSearchView() {
        mSearchView.visibility = View.VISIBLE
    }

    fun hideSearchView() {
        mSearchView.visibility = View.GONE
    }

    fun dismissSearchView() {
        mSearchView.setQuery("", true)
        mSearchView.visibility = View.GONE
    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("ActionMode", isInActionMode)
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        currentActivity.title = activityTitle

        fragmentBinding.itemList?.let {
            this.adapter = SalesOrderItemListAdapter(currentActivity, it)
            it.adapter = this.adapter
        } ?: throw AssertionError()

        setupRefreshLayout()
        refreshLayout.isRefreshing = true

        navigationPropertyName = currentActivity.intent.getStringExtra("navigation")
        parentEntityData = when {
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) -> {
                currentActivity.intent.getParcelableExtra("parent", Parcelable::class.java)
            }
            else -> @Suppress("DEPRECATION") currentActivity.intent.getParcelableExtra("parent")
        }
        navigationPropertyName?.let {navPropName ->
            parentEntityData?.let {
                val navigationProperty = (it as EntityValue).entityType.getProperty(navPropName)
                isNavigationPropertyConnection = navigationProperty.isCollection
            }
        }

        fragmentBinding.fab?.let {
            it.hide()
            it.contentDescription = getString(R.string.add_new) + " SalesOrderItem"
            it.setOnClickListener {
                listener?.onFragmentStateChange(UIConstants.EVENT_CREATE_NEW_ITEM, null)
            }
        }

        sapServiceManager?.openODataStore {
            prepareViewModel()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshListData()
    }

    /** Initializes the view model and add observers on it */
    private fun prepareViewModel() {
        viewModel = if( navigationPropertyName != null && parentEntityData != null ) {
            ViewModelProvider(currentActivity, EntityViewModelFactory(currentActivity.application, navigationPropertyName!!, parentEntityData!!))
                .get(SalesOrderItemViewModel::class.java)
        } else {
            ViewModelProvider(currentActivity).get(SalesOrderItemViewModel::class.java)
        }
        viewModel.observableItems.observe(viewLifecycleOwner, Observer<List<SalesOrderItem>> { items ->
            items?.let { entityList ->
                adapter?.let { listAdapter ->
                    listAdapter.setItems(entityList)

                    var item = viewModel.selectedEntity.value?.let { containsItem(entityList, it) }
                    if (item == null) {
                        item = if (entityList.isEmpty()) null else entityList[0]
                    }

                    item?.let {
                        viewModel.inFocusId = listAdapter.getItemIdForSalesOrderItem(it)
                        if (currentActivity.resources.getBoolean(R.bool.two_pane)) {
                            viewModel.setSelectedEntity(it)
                            if(!isInActionMode && !(currentActivity as SalesOrderItemsActivity).isNavigationDisabled) {
                                listener?.onFragmentStateChange(UIConstants.EVENT_ITEM_CLICKED, it)
                            }
                        }
                        listAdapter.notifyDataSetChanged()
                    }

                    if( item == null ) hideDetailFragment()
                }

                refreshLayout.isRefreshing = false
                fragmentBinding.fab?.let {createButton ->
                    if (entityList.isNotEmpty() && entityList.first().entitySet.isSingleton) {
                        createButton.hide()
                    } else {
                        createButton.show()
                    }
                    parentEntityData?.let {parent ->
                        navigationPropertyName?.let {
                            if (!isNavigationPropertyConnection && entityList.isNotEmpty()){
                                createButton.hide()
                            } else {
                                createButton.show()
                            }
                        }
                    }
                }
            }
        })

        viewModel.readResult.observe(viewLifecycleOwner, Observer {
            if (refreshLayout.isRefreshing) {
                refreshLayout.isRefreshing = false
            }
        })

        viewModel.deleteResult.observe(viewLifecycleOwner, Observer {
            this.onDeleteComplete(it!!)
        })
    }

    /**
     * Checks if [item] exists in the list [items] based on the item id, which in offline is the read readLink,
     * while for online the primary key.
     */
    private fun containsItem(items: List<SalesOrderItem>, item: SalesOrderItem) : SalesOrderItem? {
        return items.find { entry ->
            adapter?.getItemIdForSalesOrderItem(entry) == adapter?.getItemIdForSalesOrderItem(item)
        }
    }

    /** when no items return from server, hide the detail fragment on tablet */
    private fun hideDetailFragment() {
        currentActivity.supportFragmentManager.findFragmentByTag(UIConstants.DETAIL_FRAGMENT_TAG)?.let {
            currentActivity.supportFragmentManager.beginTransaction()
                .remove(it).commit()
        }
        secondaryToolbar?.let {
            it.menu.clear()
            it.title = ""
        }
        currentActivity.findViewById<ObjectHeader>(R.id.objectHeader)?.let {
            it.visibility = View.GONE
        }
    }

    /** Completion callback for delete operation  */
    private fun onDeleteComplete(result: OperationResult<SalesOrderItem>) {
        progressBar?.let {
            it.visibility = View.INVISIBLE
        }
        viewModel.removeAllSelected()
        actionMode?.let {
            it.finish()
            isInActionMode = false
        }

        result.error?.let {
            handleDeleteError()
            return
        }
        refreshListData()
    }

    /** Handles the deletion error */
    private fun handleDeleteError() {
        showError(resources.getString(R.string.delete_failed_detail))
        refreshLayout.isRefreshing = false
    }

    /** sets up the refresh layout */
    private fun setupRefreshLayout() {
        refreshLayout = fragmentBinding.swiperefresh
        refreshLayout.setColorSchemeColors(UIConstants.FIORI_STANDARD_THEME_GLOBAL_DARK_BASE)
        refreshLayout.setProgressBackgroundColorSchemeColor(UIConstants.FIORI_STANDARD_THEME_BACKGROUND)
        refreshLayout.setOnRefreshListener(this::refreshListData)
    }

    /** Refreshes the list data */
    internal fun refreshListData() {
        navigationPropertyName?.let { _navigationPropertyName ->
            parentEntityData?.let { _parentEntityData ->
                viewModel.refresh(_parentEntityData as EntityValue, _navigationPropertyName)
            }
        } ?: run {
            Log.d("xxxx","start to refresh data...")
            if (searchText.isNotEmpty()) {
                viewModel.refresh(searchText)
            } else {
                viewModel.refresh()
            }
        }
        adapter?.notifyDataSetChanged()
    }

    /** Sets the id for the selected item into view model */
    private fun setItemIdSelected(itemId: Int): SalesOrderItem? {
        viewModel.observableItems.value?.let { salesOrderItems ->
            if (salesOrderItems.isNotEmpty()) {
                adapter?.let {
                    viewModel.inFocusId = it.getItemIdForSalesOrderItem(salesOrderItems[itemId])
                    return salesOrderItems[itemId]
                }
            }
        }
        return null
    }

    /** Sets the detail image for the given [viewHolder] */
    private fun setDetailImage(viewHolder: SalesOrderItemListAdapter.ViewHolder<ElementEntityitemListBinding>, salesOrderItemEntity: SalesOrderItem?) {
        if (isInActionMode) {
            val drawable: Int = if (viewHolder.isSelected) {
                R.drawable.ic_sap_icon_done
            } else {
                R.drawable.ic_sap_icon_shape_circle
            }
            viewHolder.objectCell.prepareDetailImageView().scaleType = ImageView.ScaleType.FIT_CENTER
            Glide.with(currentActivity)
                .load(resources.getDrawable(drawable, null))
                .apply(RequestOptions().fitCenter())
                .into(viewHolder.objectCell.prepareDetailImageView())
        } else if (!viewHolder.masterPropertyValue.isNullOrEmpty()) {
            viewHolder.objectCell.detailImageCharacter = viewHolder.masterPropertyValue?.substring(0, 1)
        } else {
            viewHolder.objectCell.detailImageCharacter = "?"
        }
    }

    /**
     * Represents the listener to start the action mode. 
     */
    inner class OnActionModeStartClickListener(internal var holder: SalesOrderItemListAdapter.ViewHolder<ElementEntityitemListBinding>) : View.OnClickListener, View.OnLongClickListener {

        override fun onClick(view: View) {
            onAnyKindOfClick()
        }

        override fun onLongClick(view: View): Boolean {
            return onAnyKindOfClick()
        }

        /** callback function for both normal and long click of an entity */
        private fun onAnyKindOfClick(): Boolean {
            val isNavigationDisabled = (activity as SalesOrderItemsActivity).isNavigationDisabled
            if (isNavigationDisabled) {
                Toast.makeText(activity, "Please save your changes first...", Toast.LENGTH_LONG).show()
            } else {
                if (!isInActionMode) {
                    actionMode = (currentActivity as AppCompatActivity).startSupportActionMode(SalesOrderItemsListActionMode())
                    adapter?.notifyDataSetChanged()
                }
                holder.isSelected = !holder.isSelected
            }
            return true
        }
    }

    /**
     * Represents list action mode.
     */
    inner class SalesOrderItemsListActionMode : ActionMode.Callback {
        override fun onCreateActionMode(actionMode: ActionMode, menu: Menu): Boolean {
            isInActionMode = true
            fragmentBinding.fab?.let {
                it.hide()
            }
            //(currentActivity as SalesOrderItemsActivity).onSetActionModeFlag(isInActionMode)
            val inflater = actionMode.menuInflater
            inflater.inflate(R.menu.itemlist_view_options, menu)

            hideDetailFragment()
            return true
        }

        override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(actionMode: ActionMode, menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.update_item -> {
                    val salesOrderItemEntity = viewModel.getSelected(0)
                    if (viewModel.numberOfSelected() == 1 && salesOrderItemEntity != null) {
                        isInActionMode = false
                        actionMode.finish()
                        viewModel.setSelectedEntity(salesOrderItemEntity)
                        if(currentActivity.resources.getBoolean(R.bool.two_pane)) {
                            //make sure 'view' is under 'crt/update',
                            //so after done or back, the right panel has things to view
                            listener?.onFragmentStateChange(UIConstants.EVENT_ITEM_CLICKED, salesOrderItemEntity)
                        }
                        listener?.onFragmentStateChange(UIConstants.EVENT_EDIT_ITEM, salesOrderItemEntity)
                    }
                    true
                }
                R.id.delete_item -> {
                    listener?.onFragmentStateChange(UIConstants.EVENT_ASK_DELETE_CONFIRMATION,null)
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(actionMode: ActionMode) {
            isInActionMode = false
            if (!(navigationPropertyName != null && parentEntityData != null)) {
                fragmentBinding.fab?.let {
                    it.show()
                }
            }
            selectedItems.clear()
            viewModel.removeAllSelected()

            //if in big screen, make sure one item is selected.
            refreshListData()
        }
    }

    /**
    * List adapter to be used with RecyclerView. It contains the set of salesOrderItems.
    */
    inner class SalesOrderItemListAdapter(private val context: Context, private val recyclerView: RecyclerView) : RecyclerView.Adapter<SalesOrderItemListAdapter.ViewHolder<ElementEntityitemListBinding>>() {

        /** Entire list of SalesOrderItem collection */
        private var salesOrderItems: MutableList<SalesOrderItem> = ArrayList()

        /** Flag to indicate whether we have checked retained selected salesOrderItems */
        private var checkForSelectedOnCreate = false

        private lateinit var binding: ElementEntityitemListBinding

        init {
            setHasStableIds(true)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalesOrderItemListAdapter.ViewHolder<ElementEntityitemListBinding> {
            binding = ElementEntityitemListBinding.inflate( LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun getItemCount(): Int {
            return salesOrderItems.size
        }

        override fun getItemId(position: Int): Long {
            return getItemIdForSalesOrderItem(salesOrderItems[position])
        }

        override fun onBindViewHolder(holder: ViewHolder<ElementEntityitemListBinding>, position: Int) {
            checkForRetainedSelection()

            val salesOrderItemEntity = salesOrderItems[holder.bindingAdapterPosition]
            (salesOrderItemEntity.getOptionalValue(SalesOrderItem.mac))?.let {
                holder.masterPropertyValue = it.toString()
            }
            populateObjectCell(holder, salesOrderItemEntity)

            val isActive = getItemIdForSalesOrderItem(salesOrderItemEntity) == viewModel.inFocusId
            if (isActive) {
                setItemIdSelected(holder.bindingAdapterPosition)
            }
            val isSalesOrderItemSelected = viewModel.selectedContains(salesOrderItemEntity)
            setViewBackground(holder.objectCell, isSalesOrderItemSelected, isActive)

            holder.itemView.setOnLongClickListener(OnActionModeStartClickListener(holder))
            setOnClickListener(holder, salesOrderItemEntity)

            setOnCheckedChangeListener(holder, salesOrderItemEntity)
            holder.isSelected = isSalesOrderItemSelected
            setDetailImage(holder, salesOrderItemEntity)
        }

        /**
        * Check to see if there are an retained selected salesOrderItemEntity on start.
        * This situation occurs when a rotation with selected salesOrderItems is triggered by user.
        */
        private fun checkForRetainedSelection() {
            if (!checkForSelectedOnCreate) {
                checkForSelectedOnCreate = true
                if (viewModel.numberOfSelected() > 0) {
                    manageActionModeOnCheckedTransition()
                }
            }
        }

        /**
        * Computes a stable ID for each SalesOrderItem object for use to locate the ViewHolder
        *
        * @param [salesOrderItemEntity] to get the items for
        * @return an ID based on the primary key of SalesOrderItem
        */
        internal fun getItemIdForSalesOrderItem(salesOrderItemEntity: SalesOrderItem): Long {
            return salesOrderItemEntity.entityKey.toString().hashCode().toLong()
        }

        /**
        * Start Action Mode if it has not been started
        *
        * This is only called when long press action results in a selection. Hence action mode may not have been
        * started. Along with starting action mode, title will be set. If this is an additional selection, adjust title
        * appropriately.
        */
        private fun manageActionModeOnCheckedTransition() {
            if (actionMode == null) {
                actionMode = (activity as AppCompatActivity).startSupportActionMode(SalesOrderItemsListActionMode())
            }
            if (viewModel.numberOfSelected() > 1) {
                actionMode?.menu?.findItem(R.id.update_item)?.isVisible = false
            }
            actionMode?.title = viewModel.numberOfSelected().toString()
        }

        /**
        * This is called when one of the selected salesOrderItems has been de-selected
        *
        * On this event, we will determine if update action needs to be made visible or action mode should be
        * terminated (no more selected)
        */
        private fun manageActionModeOnUncheckedTransition() {
            when (viewModel.numberOfSelected()) {
                1 -> actionMode?.menu?.findItem(R.id.update_item)?.isVisible = true
                0 -> {
                    actionMode?.finish()
                    actionMode = null
                    return
                }
            }
            actionMode?.title = viewModel.numberOfSelected().toString()
        }

        private fun populateObjectCell(viewHolder: ViewHolder<ElementEntityitemListBinding>, salesOrderItemEntity: SalesOrderItem) {

            val dataValue = salesOrderItemEntity.getOptionalValue(SalesOrderItem.mac)
            var masterPropertyValue: String? = null
            if (dataValue != null) {
                masterPropertyValue = dataValue.toString()
            }
            viewHolder.objectCell.apply {
                headline = masterPropertyValue
                setUseCutOut(false)
                setDetailImage(viewHolder, salesOrderItemEntity)
                subheadline = "Ctn:" + salesOrderItemEntity.getOptionalValue(SalesOrderItem.ctn).toString()
                footnote = null
                if (masterPropertyValue == null || masterPropertyValue.isEmpty()) {
                setIcon("?", 0)
                } else {
                setIcon(masterPropertyValue.substring(0, 1), 0)
                }
                setIcon(R.drawable.default_dot, 1, R.string.attachment_item_content_desc)
            }
        }

        private fun processClickAction(viewHolder: ViewHolder<ElementEntityitemListBinding>, salesOrderItemEntity: SalesOrderItem) {
            resetPreviouslyClicked()
            setViewBackground(viewHolder.objectCell, false, true)
            viewModel.inFocusId = getItemIdForSalesOrderItem(salesOrderItemEntity)
        }

        /**
        * Attempt to locate previously clicked view and reset its background
        * Reset view model's inFocusId
        */
        private fun resetPreviouslyClicked() {
            (recyclerView.findViewHolderForItemId(viewModel.inFocusId) as ViewHolder<ElementEntityitemListBinding>?)?.let {
                setViewBackground(it.objectCell, it.isSelected, false)
            } ?: run {
                viewModel.refresh()
            }
        }

        /**
        * If there are selected salesOrderItems via long press, clear them as click and long press are mutually exclusive
        * In addition, since we are clearing all selected salesOrderItems via long press, finish the action mode.
        */
        private fun resetSelected() {
            if (viewModel.numberOfSelected() > 0) {
                viewModel.removeAllSelected()
                if (actionMode != null) {
                    actionMode?.finish()
                    actionMode = null
                }
            }
        }

        /**
        * Set up checkbox value and visibility based on salesOrderItemEntity selection status
        *
        * @param [checkBox] to set
        * @param [isSalesOrderItemSelected] true if salesOrderItemEntity is selected via long press action
        */
        private fun setCheckBox(checkBox: CheckBox, isSalesOrderItemSelected: Boolean) {
            checkBox.isChecked = isSalesOrderItemSelected
        }

        /**
        * Use DiffUtil to calculate the difference and dispatch them to the adapter
        * Note: Please use background thread for calculation if the list is large to avoid blocking main thread
        */
        @WorkerThread
        fun setItems(currentSalesOrderItems: List<SalesOrderItem>) {
            if (salesOrderItems.isEmpty()) {
                salesOrderItems = java.util.ArrayList(currentSalesOrderItems)
                notifyItemRangeInserted(0, currentSalesOrderItems.size)
            } else {
                val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                    override fun getOldListSize(): Int {
                        return salesOrderItems.size
                    }

                    override fun getNewListSize(): Int {
                        return currentSalesOrderItems.size
                    }

                    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                        return salesOrderItems[oldItemPosition].entityKey.toString() == currentSalesOrderItems[newItemPosition].entityKey.toString()
                    }

                    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                        val salesOrderItemEntity = salesOrderItems[oldItemPosition]
                        return !salesOrderItemEntity.isUpdated && currentSalesOrderItems[newItemPosition] == salesOrderItemEntity
                    }
                })
                salesOrderItems.clear()
                salesOrderItems.addAll(currentSalesOrderItems)
                result.dispatchUpdatesTo(this)
            }
        }

        /**
        * Set ViewHolder's CheckBox onCheckedChangeListener
        *
        * @param [holder] to set
        * @param [salesOrderItemEntity] associated with this ViewHolder
        */
        private fun setOnCheckedChangeListener(holder: ViewHolder<ElementEntityitemListBinding>, salesOrderItemEntity: SalesOrderItem) {
            holder.checkBox.setOnCheckedChangeListener { _, checked ->
                if (checked) {
                    //(currentActivity as SalesOrderItemsActivity).onUnderDeletion(salesOrderItemEntity, true)
                    viewModel.addSelected(salesOrderItemEntity)
                    manageActionModeOnCheckedTransition()
                    resetPreviouslyClicked()
                } else {
                    //(currentActivity as SalesOrderItemsActivity).onUnderDeletion(salesOrderItemEntity, false)
                    viewModel.removeSelected(salesOrderItemEntity)
                    manageActionModeOnUncheckedTransition()
                }
                setViewBackground(holder.objectCell, viewModel.selectedContains(salesOrderItemEntity), false)
                setDetailImage(holder, salesOrderItemEntity)
            }
        }

        /**
        * Set ViewHolder's view onClickListener
        *
        * @param [holder] to set
        * @param [salesOrderItemEntity] associated with this ViewHolder
        */
        private fun setOnClickListener(holder: ViewHolder<ElementEntityitemListBinding>, salesOrderItemEntity: SalesOrderItem) {
            holder.itemView.setOnClickListener { view ->
                val isNavigationDisabled = (currentActivity as SalesOrderItemsActivity).isNavigationDisabled
                if( !isNavigationDisabled ) {
                    hideSearchView()
                    resetSelected()
                    resetPreviouslyClicked()
                    processClickAction(holder, salesOrderItemEntity)
                    viewModel.setSelectedEntity(salesOrderItemEntity)
                    listener?.onFragmentStateChange(UIConstants.EVENT_ITEM_CLICKED, salesOrderItemEntity)
                } else {
                    Toast.makeText(currentActivity, "Please save your changes first...", Toast.LENGTH_LONG).show()
                }
            }
        }

        /**
        * Set background of view to indicate salesOrderItemEntity selection status
        * Selected and Active are mutually exclusive. Only one can be true
        *
        * @param [view]
        * @param [isSalesOrderItemSelected] - true if salesOrderItemEntity is selected via long press action
        * @param [isActive]           - true if salesOrderItemEntity is selected via click action
        */
        private fun setViewBackground(view: View, isSalesOrderItemSelected: Boolean, isActive: Boolean) {
            val isMasterDetailView = currentActivity.resources.getBoolean(R.bool.two_pane)
            if (isSalesOrderItemSelected) {
                view.background = ContextCompat.getDrawable(context, R.drawable.list_item_selected)
            } else if (isActive && isMasterDetailView && !isInActionMode) {
                view.background = ContextCompat.getDrawable(context, R.drawable.list_item_active)
            } else {
                view.background = ContextCompat.getDrawable(context, R.drawable.list_item_default)
            }
        }

        /**
        * ViewHolder for RecyclerView.
        * Each view has a Fiori ObjectCell and a checkbox (used by long press)
        */
        inner class ViewHolder<VB: ElementEntityitemListBinding>(private val viewBinding: VB) : RecyclerView.ViewHolder(viewBinding.root) {

            var isSelected = false
                set(selected) {
                    field = selected
                    checkBox.isChecked = selected
                }

            var masterPropertyValue: String? = null

            /** Fiori ObjectCell to display salesOrderItemEntity in list */
            val objectCell: ObjectCell = viewBinding.content

            /** Checkbox for long press selection */
            val checkBox: CheckBox = viewBinding.cbx

            override fun toString(): String {
                return super.toString() + " '" + objectCell.description + "'"
            }
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SalesOrderItemsActivity::class.java)
    }
}
