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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.extension.identity.verification.mgt.dao.IdentityVerificationClaimDAO;
import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationException;
import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationServerException;
import org.wso2.carbon.extension.identity.verification.mgt.internal.IdentityVerificationDataHolder;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdVClaim;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdentityVerifierData;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtException;
import org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants;
import org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationExceptionMgt;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UniqueIDUserStoreManager;
import org.wso2.carbon.user.core.common.User;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.List;
import java.util.UUID;

import static org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants.ErrorMessage.ERROR_CODE_GET_DAO;
import static org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants.ErrorMessage.ERROR_INVALID_USER_ID;

/**
 * This class contains the implementation for the IdentityVerificationService.
 */
public class IdentityVerificationManagerImpl implements IdentityVerificationManager {

    private static final Log log = LogFactory.getLog(IdentityVerificationManagerImpl.class);
    private final List<IdentityVerificationClaimDAO> idVClaimDAOs;

    public IdentityVerificationManagerImpl() {

        this.idVClaimDAOs = IdentityVerificationDataHolder.getInstance().getIdVClaimDAOs();
    }

    /**
     * Select highest priority IdVProvider DAO from an already sorted list of IdVClaim DAOs.
     *
     * @return Highest priority IdVClaim DAO.
     *
     * @throws IdentityVerificationException If an error occurs while getting the IdVClaim DAO.
     */
    private IdentityVerificationClaimDAO getIdVClaimDAO() throws IdentityVerificationException {

        if (!this.idVClaimDAOs.isEmpty()) {
            return idVClaimDAOs.get(idVClaimDAOs.size() - 1);
        } else {
            throw IdentityVerificationExceptionMgt.handleServerException(ERROR_CODE_GET_DAO);
        }
    }

    @Override
    public IdentityVerifierData verifyIdentity(String userId, IdentityVerifierData identityVerifierData, int tenantId)
            throws IdentityVerificationException {

        if (StringUtils.isBlank(userId) || !isValidUserId(userId, tenantId)) {
            throw IdentityVerificationExceptionMgt.handleClientException(ERROR_INVALID_USER_ID);
        }
        String identityVerifierName = identityVerifierData.getIdentityVerificationProviderId();
        IdentityVerifierFactory identityVerifierFactory =
                IdentityVerificationDataHolder.getInstance().getIdentityVerifierFactory(identityVerifierName);
        if (identityVerifierFactory == null) {
            // todo
            throw new IdentityVerificationException(identityVerifierName);
        }
        return identityVerifierFactory.getIdentityVerifier(identityVerifierName).
                verifyIdentity(identityVerifierData, tenantId);
    }

    @Override
    public IdVClaim getIdVClaim(String userId, String idvClaimId, int tenantId) throws IdentityVerificationException {

        if (StringUtils.isBlank(idvClaimId) || !isIdVClaimExists(idvClaimId, tenantId)) {
            throw IdentityVerificationExceptionMgt.handleClientException(
                    IdentityVerificationConstants.ErrorMessage.ERROR_INVALID_IDV_CLAIM_ID, idvClaimId);
        }
        if (StringUtils.isBlank(userId) || !isValidUserId(userId, tenantId)) {
            throw IdentityVerificationExceptionMgt.handleClientException(
                    ERROR_INVALID_USER_ID);
        }
        return getIdVClaimDAO().getIDVClaim(userId, idvClaimId, tenantId);
    }

    @Override
    public List<IdVClaim> addIdVClaims(String userId, List<IdVClaim> idVClaims, int tenantId)
            throws IdentityVerificationException {

        if (StringUtils.isBlank(userId) || !isValidUserId(userId, tenantId)) {
            throw IdentityVerificationExceptionMgt.handleClientException(
                    ERROR_INVALID_USER_ID);
        }
        for (IdVClaim idVClaim : idVClaims) {
            idVClaim.setUuid(UUID.randomUUID().toString());
            validateAddIDVClaimInputs(userId, idVClaim, tenantId);
        }
        getIdVClaimDAO().addIdVClaimList(idVClaims, tenantId);
        return idVClaims;
    }

    @Override
    public IdVClaim updateIdVClaim(String userId, IdVClaim idvClaim, int tenantId)
            throws IdentityVerificationException {

        String idvClaimId = idvClaim.getUuid();
        if (StringUtils.isBlank(idvClaimId) || !isIdVClaimExists(idvClaimId, tenantId)) {
            throw IdentityVerificationExceptionMgt.handleClientException(
                    IdentityVerificationConstants.ErrorMessage.ERROR_INVALID_IDV_CLAIM_ID, idvClaimId);
        }
        if (StringUtils.isBlank(userId) || !isValidUserId(userId, tenantId)) {
            throw IdentityVerificationExceptionMgt.handleClientException(
                    ERROR_INVALID_USER_ID);
        }
        if (idvClaim.getMetadata() == null) {
            throw IdentityVerificationExceptionMgt.handleClientException(
                    IdentityVerificationConstants.ErrorMessage.ERROR_EMPTY_CLAIM_METADATA, null);
        }
        getIdVClaimDAO().updateIdVClaim(idvClaim, tenantId);
        return idvClaim;
    }

    @Override
    public void deleteIDVClaim(String userId, String idvClaimId, int tenantId) throws IdentityVerificationException {

        if (StringUtils.isBlank(userId) || !isValidUserId(userId, tenantId)) {
            throw IdentityVerificationExceptionMgt.handleClientException(
                    ERROR_INVALID_USER_ID);
        }
        getIdVClaimDAO().deleteIdVClaim(userId, idvClaimId, tenantId);
    }

    @Override
    public IdVClaim[] getIDVClaims(String userId, int tenantId) throws IdentityVerificationException {

        if (StringUtils.isBlank(userId) || !isValidUserId(userId, tenantId)) {
            throw IdentityVerificationExceptionMgt.handleClientException(
                    ERROR_INVALID_USER_ID);
        }
        return getIdVClaimDAO().getIDVClaims(userId, tenantId);
    }

    @Override
    public boolean isIdVClaimDataExists(String userId, String idvId, String uri, int tenantId)
            throws IdentityVerificationException {

        return getIdVClaimDAO().isIdVClaimDataExist(userId, idvId, uri, tenantId);
    }

    private void validateAddIDVClaimInputs(String userId, IdVClaim idVClaim, int tenantId)
            throws IdentityVerificationException {

        String idvProviderId = idVClaim.getIdVPId();
        String claimUri = idVClaim.getClaimUri();
        if (StringUtils.isBlank(idvProviderId) || !isValidIdVProviderId(idvProviderId, tenantId)) {
            throw IdentityVerificationExceptionMgt.handleClientException(
                    IdentityVerificationConstants.ErrorMessage.ERROR_INVALID_IDV_PROVIDER_ID);
        }
        if (StringUtils.isBlank(claimUri)) {
            // todo: validate claim URI.
            throw IdentityVerificationExceptionMgt.handleClientException(
                    IdentityVerificationConstants.ErrorMessage.ERROR_INVALID_CLAIM_URI);
        }
        if (isIdVClaimDataExists(userId, idvProviderId, claimUri, tenantId)) {
            throw IdentityVerificationExceptionMgt.handleClientException(
                    IdentityVerificationConstants.ErrorMessage.ERROR_IDV_CLAIM_DATA_ALREADY_EXISTS);
        };
    }

    private boolean isValidIdVProviderId(String idvProviderId, int tenantId) throws IdentityVerificationException {

        try {
            if (IdentityVerificationDataHolder.getInstance().
                    getIdVProviderManager().isIdVProviderExists(idvProviderId, tenantId)) {
                return true;
            }
        } catch (IdVProviderMgtException e) {
            throw IdentityVerificationExceptionMgt.handleServerException(
                    IdentityVerificationConstants.ErrorMessage.ERROR_VALIDATING_IDV_PROVIDER_ID, null);
        }
        return false;
    }

    private boolean isValidUserId(String userId, int tenantId) throws IdentityVerificationServerException {

        UniqueIDUserStoreManager uniqueIDUserStoreManager;
        try {
            uniqueIDUserStoreManager =
                    getUniqueIdEnabledUserStoreManager(IdentityVerificationDataHolder.getInstance().getRealmService(),
                            IdentityTenantUtil.getTenantDomain(tenantId));
            User user = uniqueIDUserStoreManager.getUserWithID(userId, null, null);
            if (user != null) {
                return true;
            }
        } catch (UserStoreException e) {
            if (StringUtils.isNotBlank(e.getMessage()) && e.getMessage().contains("30007")) {
                return false;
            }
            throw IdentityVerificationExceptionMgt.handleServerException(
                    IdentityVerificationConstants.ErrorMessage.ERROR_CHECKING_USER_ID_EXISTENCE, userId, e);
        } catch (IdentityVerificationServerException e) {
            throw IdentityVerificationExceptionMgt.handleServerException(
                    IdentityVerificationConstants.ErrorMessage.ERROR_CHECKING_USER_ID_EXISTENCE, userId, e);
        }
        return false;
    }

    @Override
    public boolean isIdVClaimExists(String idVClaimId, int tenantId) throws IdentityVerificationException {

        return getIdVClaimDAO().isIdVClaimExist(idVClaimId, tenantId);
    }

    private UniqueIDUserStoreManager getUniqueIdEnabledUserStoreManager(RealmService realmService, String tenantDomain)
            throws IdentityVerificationServerException, UserStoreException {

        UserStoreManager userStoreManager = realmService.getTenantUserRealm(
                IdentityTenantUtil.getTenantId(tenantDomain)).getUserStoreManager();
        if (!(userStoreManager instanceof UniqueIDUserStoreManager)) {
            if (log.isDebugEnabled()) {
                log.debug("Provided user store manager does not support unique user IDs.");
            }
            throw IdentityVerificationExceptionMgt.handleServerException(
                    ERROR_INVALID_USER_ID);
        }
        return (UniqueIDUserStoreManager) userStoreManager;
    }
}
