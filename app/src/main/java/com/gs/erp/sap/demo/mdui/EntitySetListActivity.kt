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

package com.gs.erp.sap.demo.mdui

import com.gs.erp.sap.demo.app.SAPWizardApplication

import com.sap.cloud.mobile.flowv2.core.DialogHelper
import com.sap.cloud.mobile.flowv2.core.Flow
import com.sap.cloud.mobile.flowv2.core.FlowContextRegistry
import com.sap.cloud.mobile.flowv2.model.FlowType
import com.sap.cloud.mobile.flowv2.securestore.UserSecureStoreDelegate
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.*
import android.widget.ArrayAdapter
import android.content.Context
import android.content.Intent
import java.util.ArrayList
import java.util.HashMap
import com.gs.erp.sap.demo.app.WelcomeActivity
import com.gs.erp.sap.demo.databinding.ActivityEntitySetListBinding
import com.gs.erp.sap.demo.databinding.ElementEntitySetListBinding
import com.gs.erp.sap.demo.mdui.customers.CustomersActivity
import com.gs.erp.sap.demo.mdui.productcategories.ProductCategoriesActivity
import com.gs.erp.sap.demo.mdui.producttexts.ProductTextsActivity
import com.gs.erp.sap.demo.mdui.products.ProductsActivity
import com.gs.erp.sap.demo.mdui.purchaseorderheaders.PurchaseOrderHeadersActivity
import com.gs.erp.sap.demo.mdui.purchaseorderitems.PurchaseOrderItemsActivity
import com.gs.erp.sap.demo.mdui.salesorderheaders.SalesOrderHeadersActivity
import com.gs.erp.sap.demo.mdui.salesorderitems.SalesOrderItemsActivity
import com.gs.erp.sap.demo.mdui.stock.StockActivity
import com.gs.erp.sap.demo.mdui.suppliers.SuppliersActivity
import org.slf4j.LoggerFactory
import com.gs.erp.sap.demo.R

/*
 * An activity to display the list of all entity types from the OData service
 */
class EntitySetListActivity : AppCompatActivity() {
    private val entitySetNames = ArrayList<String>()
    private val entitySetNameMap = HashMap<String, EntitySetName>()
    private lateinit var binding: ActivityEntitySetListBinding


    enum class EntitySetName constructor(val entitySetName: String, val titleId: Int, val iconId: Int) {
//        Customers("Customers", R.string.eset_customers,
//            BLUE_ANDROID_ICON),
//        ProductCategories("ProductCategories", R.string.eset_productcategories,
//            WHITE_ANDROID_ICON),
//        ProductTexts("ProductTexts", R.string.eset_producttexts,
//            BLUE_ANDROID_ICON),
        Products("Products", R.string.eset_products,
            WHITE_ANDROID_ICON),
//        PurchaseOrderHeaders("PurchaseOrderHeaders", R.string.eset_purchaseorderheaders,
//            BLUE_ANDROID_ICON),
//        PurchaseOrderItems("PurchaseOrderItems", R.string.eset_purchaseorderitems,
//            WHITE_ANDROID_ICON),
        SalesOrderHeaders("SalesOrderHeaders", R.string.eset_salesorderheaders,
            BLUE_ANDROID_ICON),
//        SalesOrderItems("SalesOrderItems", R.string.eset_salesorderitems,
//            WHITE_ANDROID_ICON),
        Stock("Stock", R.string.eset_stock,
            BLUE_ANDROID_ICON),
//        Suppliers("Suppliers", R.string.eset_suppliers,
//            WHITE_ANDROID_ICON)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //navigate to launch screen if SAPServiceManager or OfflineOdataProvider is not initialized
        navForInitialize()
        binding = ActivityEntitySetListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar = findViewById<Toolbar>(R.id.toolbar) // to avoid ambiguity
        setSupportActionBar(toolbar)

        entitySetNames.clear()
        entitySetNameMap.clear()
        for (entitySet in EntitySetName.values()) {
            val entitySetTitle = resources.getString(entitySet.titleId)
            entitySetNames.add(entitySetTitle)
            entitySetNameMap[entitySetTitle] = entitySet
        }

        val listView = binding.entityList
        val adapter = EntitySetListAdapter(this, R.layout.element_entity_set_list, entitySetNames)

        listView.adapter = adapter

        listView.setOnItemClickListener listView@{ _, _, position, _ ->
            val entitySetName = entitySetNameMap[adapter.getItem(position)!!]
            val context = this@EntitySetListActivity
            val intent: Intent = when (entitySetName) {
//                EntitySetName.Customers -> Intent(context, CustomersActivity::class.java)
//                EntitySetName.ProductCategories -> Intent(context, ProductCategoriesActivity::class.java)
//                EntitySetName.ProductTexts -> Intent(context, ProductTextsActivity::class.java)
                EntitySetName.Products -> Intent(context, ProductsActivity::class.java)
//                EntitySetName.PurchaseOrderHeaders -> Intent(context, PurchaseOrderHeadersActivity::class.java)
//                EntitySetName.PurchaseOrderItems -> Intent(context, PurchaseOrderItemsActivity::class.java)
                EntitySetName.SalesOrderHeaders -> Intent(context, SalesOrderHeadersActivity::class.java)
//                EntitySetName.SalesOrderItems -> Intent(context, SalesOrderItemsActivity::class.java)
                EntitySetName.Stock -> Intent(context, StockActivity::class.java)
//                EntitySetName.Suppliers -> Intent(context, SuppliersActivity::class.java)
                else -> return@listView
            }
            context.startActivity(intent)
        }
    }

    inner class EntitySetListAdapter internal constructor(context: Context, resource: Int, entitySetNames: List<String>)
                    : ArrayAdapter<String>(context, resource, entitySetNames) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var view = convertView
            var viewBind :ElementEntitySetListBinding
            val entitySetName = entitySetNameMap[getItem(position)!!]
            if (view == null) {
                viewBind = ElementEntitySetListBinding.inflate(LayoutInflater.from(context), parent, false)
                view = viewBind.root
            } else {
                viewBind = ElementEntitySetListBinding.bind(view)
            }
            val entitySetCell = viewBind.entitySetName
            entitySetCell.headline = entitySetName?.titleId?.let {
                context.resources.getString(it)
            }
            entitySetName?.iconId?.let { entitySetCell.setDetailImage(it) }
            return view
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.entity_set_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.menu_delete_registration)?.isEnabled =
            UserSecureStoreDelegate.getInstance().getRuntimeMultipleUserModeAsync() == true
        menu?.findItem(R.id.menu_delete_registration)?.isVisible =
            UserSecureStoreDelegate.getInstance().getRuntimeMultipleUserModeAsync() == true
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        LOGGER.debug("onOptionsItemSelected: " + item.title)
        return when (item.itemId) {
            R.id.menu_settings -> {
                LOGGER.debug("settings screen menu item selected.")
                Intent(this, SettingsActivity::class.java).also {
                    this.startActivity(it)
                }
                true
            }
            R.id.menu_logout -> {
                Flow.start(this, FlowContextRegistry.flowContext.copy(
                    flowType = FlowType.LOGOUT,
                )) { _, resultCode, _ ->
                    if (resultCode == RESULT_OK) {
                        Intent(this, WelcomeActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(this)
                        }
                    }
                }
                true
            }
            R.id.menu_delete_registration -> {
                DialogHelper.ErrorDialogFragment(
                    message = getString(R.string.delete_registration_warning),
                    title = getString(R.string.dialog_warn_title),
                    positiveButtonCaption = getString(R.string.confirm_yes),
                    negativeButtonCaption = getString(R.string.cancel),
                    positiveAction = {
                        Flow.start(this, FlowContextRegistry.flowContext.copy(
                            flowType = FlowType.DEL_REGISTRATION
                        )) { _, resultCode, _ ->
                            if (resultCode == RESULT_OK) {
                                Intent(this, WelcomeActivity::class.java).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    startActivity(this)
                                }
                            }
                        }
                    }
                ).apply {
                    isCancelable = false
                    show(supportFragmentManager, this@EntitySetListActivity.getString(R.string.delete_registration))
                }
                true
            }
            else -> false
        }
    }

    private fun navForInitialize() {
        if ((application as SAPWizardApplication).sapServiceManager == null) {
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }


    companion object {
        private val LOGGER = LoggerFactory.getLogger(EntitySetListActivity::class.java)
        private val BLUE_ANDROID_ICON = R.drawable.ic_sap_icon_product_filled_round
        private val WHITE_ANDROID_ICON = R.drawable.ic_sap_icon_product_outlined
    }
}
