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

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import android.widget.ProgressBar
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.viewbinding.ViewBinding
import com.gs.erp.sap.demo.R
import com.sap.cloud.mobile.flowv2.core.DialogHelper

abstract class InterfacedFragment<TE, TVB: ViewBinding>: Fragment(), MenuProvider {

    /** Hold the current context */
    internal lateinit var currentActivity: FragmentActivity

    /** Store the toolbar title of the actual fragment */
    internal var activityTitle: String = ""

    /** Store the toolbar menu resource of the actual fragment */
    internal var menu: Int = 0

    /** Navigation parameter: name of the link */
    internal var parentEntityData: Parcelable? = null

    /** Navigation parameter: starting entity */
    internal var navigationPropertyName: String? = null

    private var _binding: TVB? = null
    val fragmentBinding get() = _binding!!

    /** The progress bar */
    internal val secondaryToolbar: Toolbar?
        get() = currentActivity.findViewById<Toolbar>(R.id.secondaryToolbar)

    /** The progress bar */
    internal val progressBar : ProgressBar?
        get() = currentActivity.findViewById<ProgressBar>(R.id.indeterminateBar)

    /** The listener **/
    internal var listener: InterfacedFragmentListener<TE>? = null

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let {
            currentActivity = it
            if (it is InterfacedFragmentListener<*>) {
                listener = it as InterfacedFragmentListener<TE>
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = initBinding(inflater, container)
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val menuHost : MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    abstract fun initBinding(inflater: LayoutInflater, container: ViewGroup?): TVB

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        secondaryToolbar?.let {
            it.menu.clear()
            it.inflateMenu(this.menu)
            it.setOnMenuItemClickListener(this::onMenuItemSelected)
            return@onCreateMenu
        }
        menuInflater.inflate(this.menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
    }

    protected fun showError(message: String) {
        DialogHelper(requireContext()).showOKOnlyDialog(
            fragmentManager = requireActivity().supportFragmentManager,
            message = message
        )
    }

    interface InterfacedFragmentListener<T> {
        fun onFragmentStateChange(evt: Int, entity: T?)
    }
}