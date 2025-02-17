## PREPARATION
### THE SERVER ENV
1. Start a SAP trial and login to [SAP BTP cockpit](https://account.cn1.platform.sapcloud.cn/cockpit#/home/allaccounts)
2. [Setup a MDK app](https://developers.sap.com/tutorials/cp-mobile-dev-kit-ms-setup.html), get the ***url for your MDK app***,which looks like: https://****trial-dev-**.cfapps.us10-001.hana.ondemand.com
3. Change the security configuration of the MDK app to "API Key Only" and get the ***API Key*** for future usage.
4. New OData Service with the metadata include in this repo(path: app/src/main/res/raw/com_gs_erp_sap_demo.xml), and you will get a ***URL of your OData Service*** which will be used after. You can refer to [How to new a SAP OData Service](How_to_new_a_SAP_OData_Service.md) for more details
5. Bind the OData to the MDK app instance, the config path is: App Info -> Assigned Features -> Mobile Connectivity -> Destination Name: com.gs.sap.erp.demo -> URL: ***URL of your OData Service***

### THE BUILD ENV
Download SAP BTP SDK and setup Android Studio, refers to: https://help.sap.com/docs/btp/btp-developers-guide/what-is-btp-developers-guide?version=Cloud

## HOW TO BUILD THIS DEMO APP
1. Open SAP_ERP directory in Android Studio and let gradle do the initialization work
2. Modify the source as below:
- app/src/main/res/raw/sap_mobile_services.json - replace the Service Url with ***url for your MDK app*** above, replace the appID with the MDK app id at the server side
- app/src/main/java/com/gs/erp/sap/demo/app/SAPWizardApplication.kt - repleace the apikey at line#113 with the ***API Key*** above
2. Go to the Grable tool window at the right of Android Studio
3. Find out the task as Path(GsSAPDemo -> app -> build -> assemble) and excute it
4. When build finished, the apks will output to path as belows:
- ./app/build/outputs/apk/tencentAppStoreforChinaMarket/debug/app-tencentAppStoreforChinaMarket-debug.apk
- ./app/build/outputs/apk/tencentAppStoreforChinaMarket/release/app-tencentAppStoreforChinaMarket-release-unsigned.apk
- ./app/build/outputs/apk/googlePlayStoreforGlobalMarket/debug/app-googlePlayStoreforGlobalMarket-debug.apk
- ./app/build/outputs/apk/googlePlayStoreforGlobalMarket/release/app-googlePlayStoreforGlobalMarket-release-unsigned.apk
5. Choose the target apk according to your device env