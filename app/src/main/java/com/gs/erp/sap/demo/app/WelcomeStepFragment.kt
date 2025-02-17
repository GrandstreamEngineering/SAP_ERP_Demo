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

package com.gs.erp.sap.demo.app

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.gs.erp.sap.demo.BuildConfig
import com.gs.erp.sap.demo.R
import com.gs.erp.sap.demo.databinding.ActivityWelcomeBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sap.cloud.mobile.fiori.onboarding.LaunchScreen
import com.sap.cloud.mobile.fiori.onboarding.ext.LaunchScreenSettings
import com.sap.cloud.mobile.flowv2.core.FlowStepFragment
import com.sap.cloud.mobile.flowv2.ext.ButtonSingleClickListener
import com.sap.cloud.mobile.foundation.mobileservices.SDKInitializer.getService
import com.sap.cloud.mobile.foundation.theme.ThemeDownloadService
import java.util.regex.Pattern

class WelcomeStepFragment : FlowStepFragment() {
    private lateinit var binding: ActivityWelcomeBinding
    private val logos: Pair<Bitmap?, Bitmap?> ? by lazy {
        var lightLogo: Bitmap? = null
        var darkLogo: Bitmap? = null
        var options = BitmapFactory.Options()
        options.inJustDecodeBounds = false

        getService(ThemeDownloadService::class)?.let { service ->
            service.getLightLogo()?.also { logo ->  lightLogo = BitmapFactory.decodeFile(logo.path, options)}
        }
        getService(ThemeDownloadService::class)?.let { service ->
            service.getDarkLogo()?.also { logo ->  darkLogo = BitmapFactory.decodeFile(logo.path, options)}
        }
        return@lazy Pair(lightLogo, darkLogo)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityWelcomeBinding.inflate(cloneLayoutInflater(inflater), container, false)
        val settings = LaunchScreenSettings.Builder()
            .setDemoButtonVisible(true)
            .setHeaderLineLabel(getString(R.string.application_name))
            .setPrimaryButtonText(getString(R.string.welcome_screen_primary_button_label))
            .setFooterVisible(true)
            .setUrlTermsOfService("http://www.sap.com")
            .setEulaAgreeVisibile(true)
            .setUrlAgreePrivacy("http://www.sap.com")
            .setUrlEula("http://www.sap.com")
            .addInfoViewSettings(
                LaunchScreenSettings.LaunchScreenInfoViewSettings(
                    R.drawable.ic_sap_icon_sdk_transparent,
                    getString(R.string.welcome_screen_headline_label),
                    getString(R.string.welcome_screen_detail_label)
                )
            )
            .build()
        binding.launchScreen.apply {
            initialize(settings)
            setPrimaryButtonOnClickListener(ButtonSingleClickListener{
                stepDone(R.id.stepWelcome, popCurrent = true)
            })
            setAgreeButtonOnClickListener {
                primaryButton.isEnabled = agreeCheckBox.isChecked
            }
            setDemoButtonOnClickListener(ButtonSingleClickListener{
                showDemoDialog()
            })
        }
        binding.launchScreen.findViewById<TextView>(R.id.launchscreen_description)?.also {
            Linkify.addLinks(
                it,
                Pattern.compile("SAP BTP SDK for Android"),
                null, null) {_, _ ->
                    "https://help.sap.com/doc/f53c64b93e5140918d676b927a3cd65b/Cloud/en-US/docs-en/guides/getting-started/android/overview.html"
                }
        }

        if (BuildConfig.FLAVOR == "tencentAppStoreforChinaMarket") {
            showEULADialog()
        }
        checkLogo(binding.launchScreen)
        return binding.root
    }

    private fun checkLogo(launchScreen: LaunchScreen) {
        var isDarkMode = (requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        var logoFile : Bitmap?
        if (isDarkMode){
            logoFile = logos?.second
        } else {
            logoFile = logos?.first
        }
        logoFile?.let {
            var launchScreenImages = launchScreen.findViewById<ImageView>(R.id.launchscreen_sapLogo)
            launchScreenImages.setImageBitmap(it)
        }
    }

    private fun showEULADialog() {
        val fragmentActivity = requireActivity()
        val builder =
            MaterialAlertDialogBuilder(fragmentActivity, R.style.OnboardingDefaultTheme_Dialog_Alert)
        builder
            .setMessage(Html.fromHtml(getString(R.string.launch_screen_dialog_eula_text)))
            .setPositiveButton(getString(R.string.launch_screen_dialog_button_agree)) { dialog, _ ->
                binding.launchScreen.agreeCheckBox.visibility = View.INVISIBLE
                binding.launchScreen.eulaText.visibility = View.INVISIBLE
                binding.launchScreen.primaryButton.isEnabled = true
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.launch_screen_dialog_button_disagree)) { dialog, _ ->
                showConfirmDialog()
                dialog.dismiss()
            }
            .setTitle(getString(R.string.launch_screen_dialog_title))
            .setCancelable(false)
        val privacyDialog = builder.create()
        privacyDialog.show()

        val textView = privacyDialog.findViewById<TextView>(android.R.id.message)
        textView?.setLinkTextColor(resources.getColor(R.color.link_text))
        textView?.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun showConfirmDialog() {
        val fragmentActivity = requireActivity()
        val builder =
            MaterialAlertDialogBuilder(fragmentActivity, R.style.OnboardingDefaultTheme_Dialog_Alert)
        builder
            .setMessage(Html.fromHtml(getString(R.string.launch_screen_dialog_disagree_text)))
            .setNegativeButton(getString(R.string.launch_screen_dialog_disagree_confirm)) { dialog, _ ->
                dialog.dismiss()
            }
            .setTitle(getString(R.string.launch_screen_dialog_title))
            .setCancelable(false)
        val confirmDialog = builder.create()
        confirmDialog.show()

        val textView = confirmDialog.findViewById<TextView>(android.R.id.message)
        textView?.setLinkTextColor(resources.getColor(R.color.link_text))
        textView?.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun showDemoDialog() {
        val fragmentActivity = requireActivity()
        val builder =
            MaterialAlertDialogBuilder(fragmentActivity, R.style.OnboardingDefaultTheme_Dialog_Alert)
        builder
            .setMessage(getString(R.string.launch_screen_demo_dialog_message))
            .setTitle(getString(R.string.launch_screen_demo_dialog_title))
            .setNegativeButton(getString(R.string.launch_screen_demo_dialog_button_goback)) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
        val demoModeDialog = builder.create()
        demoModeDialog.show()
    }
}
