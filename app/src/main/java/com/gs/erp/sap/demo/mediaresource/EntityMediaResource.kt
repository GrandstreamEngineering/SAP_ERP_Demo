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

package com.gs.erp.sap.demo.mediaresource

import com.sap.cloud.mobile.odata.EntitySet
import com.sap.cloud.mobile.odata.EntityType
import com.sap.cloud.mobile.odata.EntityValue

/*
 * Utility class to support the use of Glide to download media resources.
 */
object EntityMediaResource {
    /**
     * Determine if an entity set has media resource
     * @param entitySet
     * @return true if entity type is a Media Linked Entry (MLE) or it has stream properties
     */
    @JvmStatic
    fun hasMediaResources(entitySet: EntitySet) = entitySet.entityType.isMedia || entitySet.entityType.streamProperties.length() > 0
    /**
     * Determine if an entity type has media resource
     * @param entityType
     * @return true if entity type is a Media Linked Entry (MLE) or it has stream properties
     */
    @JvmStatic
    fun hasMediaResources(entityType: EntityType) = entityType.isMedia || entityType.streamProperties.length() > 0
    /**
     * Determine if the version within the metadata document is OData V4 or higher
     * Server of a V4 service usually do not return metadata with the query. As a result, one cannot construct
     * the download Url for media resources to use with Glide. This method is used to conditionally add parameter
     * to have server returns full metadata information during query so that we will always be able to construct
     * the download Url for Glide.
     * @param [version] version of OData service specified in metadata document.
     *      It is version multiplied by 100 i.e. 4.0 is 400, 4.0.1 is 401
     * @return true if version passed in is V4 or higher
     */
    @JvmStatic
    fun isV4(version: Int) = version > 399
    /**
     * Return download Url for one of the media resource associated with the entity parameter.
     * @param entityValue
     * @param rootUrl
     * @return If the entity type associated with the entity parameter is a Media Linked Entry,
     * the MLE url will be returned. Otherwise, download url for one of the stream
     * properties will be returned.
     */
    @JvmStatic
    fun getMediaResourceUrl(entityValue: EntityValue, rootUrl: String): String? {
        if (entityValue.entityType.isMedia) {
            android.util.Log.d("xxxx", "getMediaRsourceurl is 1:")
            return mediaLinkedEntityUrl(entityValue, rootUrl)
        } else {
            if (entityValue.entityType.streamProperties.length() > 0) {
                android.util.Log.d("xxxx", "getMediaRsourceurl is 2:")
                return namedResourceUrl(entityValue, rootUrl)
            }
        }
        return null
    }

    /**
     * Get the media linked entity url
     * @param entityValue entity whose MLE url is to return
     * @param rootUrl OData Service base url
     * @return the media linked entity url or null if one cannot be constructed from the entity
     */
    private fun mediaLinkedEntityUrl(entityValue: EntityValue, rootUrl: String): String? {
        val mediaLink = entityValue.mediaStream.readLink
        return if (mediaLink != null) {
            rootUrl + mediaLink
        } else null
    }

    /**
     * Get the named resource url. If there are more than one named resources, only one will be returned
     * @param entityValue entity whose MLE url is to return
     * @param rootUrl
     * @return
     */
    private fun namedResourceUrl(entityValue: EntityValue, rootUrl: String): String? {
        val namedResourceProp = entityValue.entityType.streamProperties.first()
        val streamLink = namedResourceProp.getStreamLink(entityValue)
        var mediaLink: String? = streamLink.readLink
        if (mediaLink != null) {
            return rootUrl + mediaLink
        } else {
            // This is to get around the problem that after we writeToParcel and read it back, we lost the url for stream link
            // To be removed when bug is fixed
            if (entityValue.readLink != null) {
                mediaLink = entityValue.readLink + '/'.toString() + namedResourceProp.name
                return rootUrl + mediaLink
            }
        }
        return null
    }
}