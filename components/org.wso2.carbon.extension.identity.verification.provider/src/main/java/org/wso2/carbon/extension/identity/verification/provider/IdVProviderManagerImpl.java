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
package org.wso2.carbon.extension.identity.verification.provider;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.extension.identity.verification.provider.dao.IdVProviderManagementDAO;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtClientException;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtException;
import org.wso2.carbon.extension.identity.verification.provider.model.IdentityVerificationProvider;
import org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants;
import org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtExceptionManagement;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.List;

/**
 * This class contains the implementation for the IdVProviderManager.
 */
public class IdVProviderManagerImpl implements IdVProviderManager {

    private static final Log log = LogFactory.getLog(IdVProviderManagerImpl.class);
    IdVProviderManagementDAO idVProviderManagementDAO = new IdVProviderManagementDAO();

    @Override
    public IdentityVerificationProvider addIdVProvider(IdentityVerificationProvider identityVerificationProvider,
                                                       int tenantId) throws IdVProviderMgtException {

        validateAddIdPVInputValues(identityVerificationProvider.getIdVProviderName(), tenantId);
        idVProviderManagementDAO.addIdVProvider(identityVerificationProvider, tenantId);
        return identityVerificationProvider;
    }

    @Override
    public IdentityVerificationProvider getIdVProvider(String idVProviderId, int tenantId)
            throws IdVProviderMgtException {

        validateIdPId(idVProviderId);
        return idVProviderManagementDAO.getIdVProvider(idVProviderId, tenantId);
    }

    @Override
    public int getCountOfIdVProviders(int tenantId) throws IdVProviderMgtException {

        return idVProviderManagementDAO.getCountOfIdVProviders(tenantId);
    }

    @Override
    public void deleteIdVProvider(String idVProviderId, int tenantId) throws IdVProviderMgtException {

        idVProviderManagementDAO.deleteIdVProvider(idVProviderId, tenantId);
    }

    @Override
    public IdentityVerificationProvider updateIdVProvider(IdentityVerificationProvider oldIdVProvider,
                                                          IdentityVerificationProvider updatedIdVProvider,
                                                          int tenantId) throws IdVProviderMgtException {

        idVProviderManagementDAO.updateIdVProvider(oldIdVProvider, updatedIdVProvider, tenantId);
        return updatedIdVProvider;
    }

    @Override
    public List<IdentityVerificationProvider> getIdVProviders(Integer limit, Integer offset, int tenantId)
            throws IdVProviderMgtException {

        return idVProviderManagementDAO.getIdVProviders(validateLimit(limit), validateOffset(offset), tenantId);
    }

    @Override
    public boolean isIdVProviderExists(String idvProviderId, int tenantId) throws IdVProviderMgtException {

        return idVProviderManagementDAO.isIdVProviderExists(idvProviderId, tenantId);
    }

    /**
     * Validate input parameters for the addIdVProvider function.
     *
     * @param idVPName Identity Verification Provider name.
     * @param tenantId Tenant Id.
     * @throws IdVProviderMgtException IdVProviderMgtException
     */
    private void validateAddIdPVInputValues(String idVPName, int tenantId) throws IdVProviderMgtException {

        if (getIdVPByName(idVPName, tenantId) != null) {
            throw IdVProviderMgtExceptionManagement.handleClientException(IdVProviderMgtConstants.ErrorMessage.
                    ERROR_IDVP_ALREADY_EXISTS, idVPName, null);
        }
    }

    /**
     * Validate input parameters for the getIdPByResourceId function.
     *
     * @param idVProviderId Identity Provider ID.
     * @throws IdVProviderMgtException IdVProviderMgtException.
     */
    private void validateIdPId(String idVProviderId) throws IdVProviderMgtException {

        if (StringUtils.isEmpty(idVProviderId)) {
            String data = "Invalid argument: Identity Verification Provider ID value is empty";
            throw IdVProviderMgtExceptionManagement.handleClientException(IdVProviderMgtConstants.ErrorMessage.
                    ERROR_IDVP_REQUEST_INVALID, data);
        }
    }

    @Override
    public IdentityVerificationProvider getIdVPByName(String idVPName, int tenantId)
            throws IdVProviderMgtException {

        if (StringUtils.isEmpty(idVPName)) {
            // todo
            String msg = "Invalid argument: Identity Verification Provider Name value is empty";
            throw new IdVProviderMgtClientException(msg);
        }

        return idVProviderManagementDAO.getIdVPByName(idVPName, tenantId);
    }

    /**
     * Validate limit.
     *
     * @param limit given limit value.
     * @return validated limit and offset value.
     */
    private int validateLimit(Integer limit) throws IdVProviderMgtClientException {

        if (limit == null) {
            if (log.isDebugEnabled()) {
                log.debug("Given limit is null. Therefore we get the default limit from identity.xml.");
            }
            limit = IdentityUtil.getDefaultItemsPerPage();
        }
        if (limit < 0) {
            String message = "Given limit: " + limit + " is a negative value.";
            throw IdVProviderMgtExceptionManagement.
                    handleClientException(IdVProviderMgtConstants.ErrorMessage.ERROR_RETRIEVING_IDV_PROVIDER, message);
        }

        int maximumItemsPerPage = IdentityUtil.getMaximumItemPerPage();
        if (limit > maximumItemsPerPage) {
            if (log.isDebugEnabled()) {
                log.debug("Given limit exceed the maximum limit. Therefore we get the default limit from " +
                        "identity.xml. limit: " + maximumItemsPerPage);
            }
            limit = maximumItemsPerPage;
        }
        return limit;
    }

    /**
     * Validate offset.
     *
     * @param offset given offset value.
     * @return validated limit and offset value.
     * @throws IdVProviderMgtClientException Error while set offset
     */
    private int validateOffset(Integer offset) throws IdVProviderMgtClientException {

        if (offset == null) {
            // Return first page offset.
            offset = 0;
        }

        if (offset < 0) {
            String message = "Invalid offset applied. Offset should not negative. offSet: " + offset;
            throw IdVProviderMgtExceptionManagement.handleClientException(IdVProviderMgtConstants.
                            ErrorMessage.ERROR_RETRIEVING_IDV_PROVIDER, message);
        }
        return offset;
    }
}
