/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.extension.identity.verification.api.rest.common;

import org.wso2.carbon.extension.identity.verification.provider.IdVProviderManager;
import org.wso2.carbon.extension.identity.verification.mgt.IdentityVerificationManager;

/**
 * Service holder class for identity verification Rest API.
 */
public class IdentityVerificationServiceHolder {

    private static IdVProviderManager idVProviderManager;
    private static IdentityVerificationManager identityVerificationManager;

    /**
     * Get IdVProviderManager osgi service.
     *
     * @return IdVProviderManager
     */
    public static IdVProviderManager getIdVProviderManager() {

        return idVProviderManager;
    }

    /**
     * Set IdVProviderManager osgi service.
     *
     * @param idVProviderManager IdVProviderManager.
     */
    public static void setIdVProviderManager(IdVProviderManager idVProviderManager) {

        IdentityVerificationServiceHolder.idVProviderManager = idVProviderManager;
    }

    /**
     * Set IdentityVerificationManager osgi service.
     *
     * @param identityVerificationManager IdentityVerificationManager.
     */
    public static void setIdentityVerificationManager(IdentityVerificationManager identityVerificationManager) {

        IdentityVerificationServiceHolder.identityVerificationManager = identityVerificationManager;
    }

    /**
     * Get IdentityVerificationManager osgi service.
     *
     * @return IdentityVerificationManager
     */
    public static IdentityVerificationManager getIdentityVerificationManager() {

        return identityVerificationManager;
    }
}
