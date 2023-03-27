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
package org.wso2.carbon.extension.identity.verification.mgt;

import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationException;
import org.wso2.carbon.extension.identity.verification.mgt.internal.IdentityVerificationDataHolder;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdVClaim;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdentityVerifierData;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtException;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVConfigProperty;
import org.wso2.carbon.extension.identity.verification.provider.model.IdentityVerificationProvider;

import java.util.List;
import java.util.Map;

/**
 * This is the abstract class of IdentityVerifier.
 */
public abstract class AbstractIdentityVerifier implements IdentityVerifier {

    public IdentityVerificationProvider getIdVProvider(IdentityVerifierData identityVerifierData, int tenantId) {

        try {
            String idVProviderId = identityVerifierData.getIdentityVerificationProviderId();
            return IdentityVerificationDataHolder.getInstance().
                    getIdVProviderManager().getIdVProvider(idVProviderId, tenantId);
        } catch (IdVProviderMgtException e) {
            throw new RuntimeException(e);
        }
    }

    public IdVConfigProperty[] getIdVConfigProperties(IdentityVerificationProvider identityVerificationProvider) {

        return identityVerificationProvider.getIdVConfigProperties();
    }

    public Map<String, String> getClaimMappings(IdentityVerificationProvider identityVerificationProvider) {

        return identityVerificationProvider.getClaimMappings();
    }

    public List<IdVClaim> storeIdVClaims(String userId, List<IdVClaim> idVClaims, int tenantId) {

        try {
            IdentityVerificationManager identityVerificationManager = new IdentityVerificationManagerImpl();
            return identityVerificationManager.addIdVClaims(userId, idVClaims, tenantId);
        } catch (IdentityVerificationException e) {
            throw new RuntimeException(e);
        }
    }

    public IdVClaim updateIdVClaims(String userId, IdVClaim idvClaim, int tenantId) {

        try {
            IdentityVerificationManager identityVerificationManager = new IdentityVerificationManagerImpl();
            return identityVerificationManager.updateIdVClaim(userId, idvClaim, tenantId);
        } catch (IdentityVerificationException e) {
            throw new RuntimeException(e);
        }
    }
}
