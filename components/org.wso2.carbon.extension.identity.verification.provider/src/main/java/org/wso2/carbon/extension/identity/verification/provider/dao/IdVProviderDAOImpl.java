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
package org.wso2.carbon.extension.identity.verification.provider.dao;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.extension.identity.verification.provider.IdVPSecretProcessor;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtException;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdvProviderMgtServerException;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVConfigProperty;
import org.wso2.carbon.extension.identity.verification.provider.model.IdentityVerificationProvider;
import org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants;
import org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtExceptionManagement;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.CLAIM;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.DESCRIPTION;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.ErrorMessage.ERROR_ADDING_IDV_PROVIDER;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.ErrorMessage.ERROR_ADDING_IDV_PROVIDER_CLAIMS;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.ErrorMessage.ERROR_ADDING_IDV_PROVIDER_CONFIGS;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.ErrorMessage.ERROR_DATABASE_CONNECTION;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.ErrorMessage.ERROR_DELETING_IDV_PROVIDER_CLAIMS;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.ErrorMessage.ERROR_DELETING_IDV_PROVIDER_CONFIGS;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.ErrorMessage.ERROR_GETTING_IDV_PROVIDER_COUNT;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.ErrorMessage.ERROR_RETRIEVING_IDV_PROVIDER;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.ErrorMessage.ERROR_RETRIEVING_IDV_PROVIDERS;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.ErrorMessage.ERROR_RETRIEVING_IDV_PROVIDER_CLAIMS;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.ErrorMessage.ERROR_RETRIEVING_IDV_PROVIDER_CONFIGS;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.ErrorMessage.ERROR_RETRIEVING_IDV_PROVIDER_SECRETS;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.ErrorMessage.ERROR_STORING_IDV_PROVIDER_SECRETS;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.ID;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.IS_ENABLED;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.IS_SECRET;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.LOCAL_CLAIM;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.NAME;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.PROPERTY_KEY;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.PROPERTY_VALUE;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.SQLQueries.ADD_IDVP_CLAIM_SQL;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.SQLQueries.ADD_IDVP_CONFIG_SQL;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.SQLQueries.ADD_IDVP_SQL;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.SQLQueries.DELETE_IDVP_CLAIM_SQL;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.SQLQueries.DELETE_IDVP_CONFIG_SQL;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.SQLQueries.GET_COUNT_OF_IDVPS_SQL;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.SQLQueries.GET_IDVPS_SQL;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.SQLQueries.GET_IDVP_BY_NAME_SQL;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.SQLQueries.GET_IDVP_CLAIMS_SQL;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.SQLQueries.GET_IDVP_CONFIG_SQL;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.SQLQueries.GET_IDVP_SQL;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.SQLQueries.IS_IDVP_EXIST_SQL;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.SQLQueries.UPDATE_IDVP_SQL;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.UUID;

/**
 * Data Access Layer functionality for Identity Verification Provider management.
 */
public class IdVProviderDAOImpl implements IdVProviderDAO {

    @Override
    public int getPriority() {

        return 1;
    }

    @Override
    public IdentityVerificationProvider getIdVProvider(String idVPUuid, int tenantId)
            throws IdVProviderMgtException {

        IdentityVerificationProvider identityVerificationProvider;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            identityVerificationProvider = getIDVPbyUUID(idVPUuid, tenantId, connection);
            if (identityVerificationProvider == null) {
                return null;
            }
            // Get configs of identity verification provider.
            identityVerificationProvider =
                    getIdVProviderWithConfigs(identityVerificationProvider, tenantId, connection);

            // Get claim mappings of identity verification provider.
            getIdVProvidersWithClaims(identityVerificationProvider, tenantId, connection);
        } catch (SQLException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_RETRIEVING_IDV_PROVIDERS, e);
        }
        return identityVerificationProvider;
    }

    @Override
    public boolean isIdVProviderExists(String idVPUuid, int tenantId) throws IdVProviderMgtException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement getIdVProvidersStmt = connection.prepareStatement(IS_IDVP_EXIST_SQL)) {
            getIdVProvidersStmt.setString(1, idVPUuid);
            getIdVProvidersStmt.setInt(2, tenantId);

            try (ResultSet idVProviderResultSet = getIdVProvidersStmt.executeQuery()) {
                return idVProviderResultSet.next();
            }
        } catch (SQLException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_RETRIEVING_IDV_PROVIDER, idVPUuid, e);
        }
    }

    @Override
    public void addIdVProvider(IdentityVerificationProvider identityVerificationProvider, int tenantId)
            throws IdVProviderMgtException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement addIdVProviderStmt = connection.prepareStatement(ADD_IDVP_SQL)) {
            addIdVProviderStmt.setString(1, identityVerificationProvider.getIdVProviderUuid());
            addIdVProviderStmt.setInt(2, tenantId);
            addIdVProviderStmt.setString(3, identityVerificationProvider.getIdVProviderName());
            addIdVProviderStmt.setString(4, identityVerificationProvider.getIdVProviderDescription());
            if (identityVerificationProvider.isEnabled()) {
                addIdVProviderStmt.setString(5, "1");
            } else {
                addIdVProviderStmt.setString(5, "0");
            }
            addIdVProviderStmt.executeUpdate();

            IdentityVerificationProvider createdIDVP = getIDVPbyUUID(identityVerificationProvider.getIdVProviderUuid(),
                    tenantId, connection);
            // Get the id of the just added identity verification provider.
            int idPVId = Integer.parseInt(createdIDVP.getId());

            // Add configs of identity verification provider.
            addIDVProviderConfigs(identityVerificationProvider, idPVId, tenantId, connection);

            // Add claims of identity verification provider.
            addIDVProviderClaims(identityVerificationProvider, idPVId, tenantId, connection);
        } catch (SQLException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_ADDING_IDV_PROVIDER, e);
        }
    }

    @Override
    public void updateIdVProvider(IdentityVerificationProvider oldIdVProvider,
                                  IdentityVerificationProvider updatedIdVProvider, int tenantId)
            throws IdVProviderMgtException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement updateIdVProviderStmt = connection.prepareStatement(UPDATE_IDVP_SQL)) {
                updateIdVProviderStmt.setString(1, updatedIdVProvider.getIdVProviderName());
                updateIdVProviderStmt.setString(2, updatedIdVProvider.getIdVProviderDescription());
                updateIdVProviderStmt.setBoolean(3, updatedIdVProvider.isEnabled());
                updateIdVProviderStmt.setString(4, oldIdVProvider.getIdVProviderUuid());
                updateIdVProviderStmt.setInt(5, tenantId);
                updateIdVProviderStmt.executeUpdate();

            // Update configs of identity verification provider.
            updateIDVProviderConfigs(updatedIdVProvider, Integer.parseInt(oldIdVProvider.getId()),
                    tenantId, connection);

            // Update claims of identity verification provider.
            updateIDVProviderClaims(updatedIdVProvider, Integer.parseInt(oldIdVProvider.getId()),
                    tenantId, connection);
        } catch (SQLException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_ADDING_IDV_PROVIDER, e);
        }
    }

    @Override
    public List<IdentityVerificationProvider> getIdVProviders(Integer limit, Integer offset, int tenantId)
            throws IdVProviderMgtException {

        List<IdentityVerificationProvider> identityVerificationProviders = new ArrayList<>();
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement getIdVProvidersStmt = connection.prepareStatement(GET_IDVPS_SQL)) {
            getIdVProvidersStmt.setInt(1, tenantId);
            getIdVProvidersStmt.setInt(2, offset);
            getIdVProvidersStmt.setInt(3, limit);
            try (ResultSet idVProviderResultSet = getIdVProvidersStmt.executeQuery()) {
                while (idVProviderResultSet.next()) {
                    IdentityVerificationProvider identityVerificationProvider = new IdentityVerificationProvider();
                    identityVerificationProvider.setId(idVProviderResultSet.getString(ID));
                    identityVerificationProvider.setIdVPUUID(idVProviderResultSet.getString(UUID));
                    identityVerificationProvider.setIdVProviderName(idVProviderResultSet.getString(NAME));
                    identityVerificationProvider.
                            setIdVProviderDescription(idVProviderResultSet.getString(DESCRIPTION));
                    identityVerificationProvider.setEnabled(idVProviderResultSet.getBoolean(IS_ENABLED));

                    // Get configs of identity verification provider.
                    identityVerificationProvider =
                            getIdVProviderWithConfigs(identityVerificationProvider, tenantId, connection);

                    // Get claim mappings of identity verification provider.
                    getIdVProvidersWithClaims(identityVerificationProvider, tenantId, connection);

                    identityVerificationProviders.add(identityVerificationProvider);
                }
            }
        } catch (SQLException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_RETRIEVING_IDV_PROVIDERS, e);
        }
        return identityVerificationProviders;
    }

    /**
     * Get Identity Verification Provider count in a given tenant.
     *
     * @param tenantId Tenant ID.
     * @throws IdVProviderMgtException Identity Verification Provider Management Exception.
     */
    public int getCountOfIdVProviders(int tenantId) throws IdVProviderMgtException {

        int count = 0;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement getIdVProvidersStmt = connection.prepareStatement(GET_COUNT_OF_IDVPS_SQL)) {
            getIdVProvidersStmt.setInt(1, tenantId);
            try (ResultSet idVProviderResultSet = getIdVProvidersStmt.executeQuery()) {
                while (idVProviderResultSet.next()) {
                    count = idVProviderResultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_GETTING_IDV_PROVIDER_COUNT,
                    IdentityTenantUtil.getTenantDomain(tenantId), e);
        }
        return count;
    }

    @Override
    public IdentityVerificationProvider getIdVPByName(String idVPName, int tenantId) throws IdVProviderMgtException {

        IdentityVerificationProvider identityVerificationProvider = null;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement getIdVProvidersStmt = connection.prepareStatement(GET_IDVP_BY_NAME_SQL)) {
            getIdVProvidersStmt.setString(1, idVPName);
            getIdVProvidersStmt.setInt(2, tenantId);
            try (ResultSet idVProviderResultSet = getIdVProvidersStmt.executeQuery()) {
                while (idVProviderResultSet.next()) {
                    identityVerificationProvider = new IdentityVerificationProvider();
                    identityVerificationProvider.setId(idVProviderResultSet.getString(ID));
                    identityVerificationProvider.setIdVPUUID(idVProviderResultSet.getString(UUID));
                    identityVerificationProvider.setIdVProviderName(idVProviderResultSet.getString(NAME));
                    identityVerificationProvider.setIdVProviderDescription(idVProviderResultSet.
                            getString(DESCRIPTION));
                    identityVerificationProvider.setEnabled(idVProviderResultSet.getBoolean(IS_ENABLED));
                }
            }
        } catch (SQLException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_RETRIEVING_IDV_PROVIDERS, e);
        }
        return identityVerificationProvider;
    }

    /**
     * Delete Identity Verification Provider by ID.
     *
     * @param idVProviderId Identity Verification Provider ID.
     * @param tenantId      Tenant ID.
     * @throws IdVProviderMgtException Error when getting Identity Verification Provider.
     */
    public void deleteIdVProvider(String idVProviderId, int tenantId) throws IdVProviderMgtException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            IdentityVerificationProvider identityVerificationProvider =
                    getIDVPbyUUID(idVProviderId, tenantId, connection);
            if (identityVerificationProvider == null) {
                return;
            }
            try (PreparedStatement deleteIdVProviderStmt =
                         connection.prepareStatement(IdVProviderMgtConstants.SQLQueries.DELETE_IDV_SQL)) {
                deleteIdVProviderStmt.setString(1, idVProviderId);
                deleteIdVProviderStmt.setInt(2, tenantId);
                deleteIdVProviderStmt.executeUpdate();
                IdentityDatabaseUtil.commitTransaction(connection);
            } catch (SQLException e1) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                throw IdVProviderMgtExceptionManagement.handleServerException(IdVProviderMgtConstants.ErrorMessage.
                        ERROR_DELETING_IDV_PROVIDER, idVProviderId, e1);
            }
        } catch (SQLException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_DATABASE_CONNECTION, e);
        }
    }

    private static IdentityVerificationProvider getIDVPbyUUID(String idVPUuid, int tenantId, Connection connection)
            throws IdvProviderMgtServerException {

        IdentityVerificationProvider identityVerificationProvider = null;
        try (PreparedStatement getIdVProvidersStmt = connection.prepareStatement(GET_IDVP_SQL)) {
            getIdVProvidersStmt.setString(1, idVPUuid);
            getIdVProvidersStmt.setInt(2, tenantId);

            try (ResultSet idVProviderResultSet = getIdVProvidersStmt.executeQuery()) {
                while (idVProviderResultSet.next()) {
                    identityVerificationProvider = new IdentityVerificationProvider();
                    identityVerificationProvider.setId(idVProviderResultSet.getString(ID));
                    identityVerificationProvider.setIdVPUUID(idVProviderResultSet.getString(UUID));
                    identityVerificationProvider.setIdVProviderName(idVProviderResultSet.getString(NAME));
                    identityVerificationProvider.setIdVProviderDescription(idVProviderResultSet.getString(DESCRIPTION));
                    identityVerificationProvider.setEnabled(idVProviderResultSet.getBoolean(IS_ENABLED));
                }
            }
        } catch (SQLException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_RETRIEVING_IDV_PROVIDER, idVPUuid, e);
        }
        return identityVerificationProvider;
    }

    private void addIDVProviderConfigs(IdentityVerificationProvider identityVerificationProvider, int idVPId,
                                       int tenantId, Connection connection) throws IdvProviderMgtServerException {

        if (identityVerificationProvider.getIdVConfigProperties() == null) {
            identityVerificationProvider.setIdVConfigProperties(new IdVConfigProperty[0]);
        }

        try (PreparedStatement addIDVProviderConfigsStmt = connection.prepareStatement(ADD_IDVP_CONFIG_SQL)) {
            IdVPSecretProcessor idVPSecretProcessor = new IdVPSecretProcessor();
            identityVerificationProvider = idVPSecretProcessor.encryptAssociatedSecrets(identityVerificationProvider);
            for (IdVConfigProperty idVConfigProperty : identityVerificationProvider.getIdVConfigProperties()) {
                addIDVProviderConfigsStmt.setInt(1, idVPId);
                addIDVProviderConfigsStmt.setInt(2, tenantId);
                addIDVProviderConfigsStmt.setString(3, idVConfigProperty.getName());
                addIDVProviderConfigsStmt.setString(4, idVConfigProperty.getValue());
                addIDVProviderConfigsStmt.setBoolean(5, idVConfigProperty.isConfidential());
                addIDVProviderConfigsStmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_ADDING_IDV_PROVIDER_CONFIGS, e);
        } catch (SecretManagementException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_STORING_IDV_PROVIDER_SECRETS,
                    identityVerificationProvider.getIdVProviderName(), e);
        }
    }

    private void addIDVProviderClaims(IdentityVerificationProvider identityVerificationProvider, int idPVId,
                                      int tenantId, Connection connection) throws IdvProviderMgtServerException {

        try (PreparedStatement addIDVProviderClaimsStmt = connection.prepareStatement(ADD_IDVP_CLAIM_SQL)) {
            for (Map.Entry<String, String> claimMapping : identityVerificationProvider.getClaimMappings().entrySet()) {
                addIDVProviderClaimsStmt.setInt(1, idPVId);
                addIDVProviderClaimsStmt.setInt(2, tenantId);
                addIDVProviderClaimsStmt.setString(3, claimMapping.getKey());
                addIDVProviderClaimsStmt.setString(4, claimMapping.getValue());
                addIDVProviderClaimsStmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_ADDING_IDV_PROVIDER_CLAIMS,
                    identityVerificationProvider.getIdVProviderName(), e);
        }
    }

    private void updateIDVProviderConfigs(IdentityVerificationProvider identityVerificationProvider,
                                          int idVPId, int tenantId, Connection connection)
            throws IdvProviderMgtServerException {

        deleteIDVProviderConfigs(idVPId, tenantId, connection);
        if (ArrayUtils.isEmpty(identityVerificationProvider.getIdVConfigProperties())) {
            return;
        }
        addIDVProviderConfigs(identityVerificationProvider, idVPId, tenantId, connection);
    }

    private void deleteIDVProviderConfigs(int idVId, int tenantId, Connection connection)
            throws IdvProviderMgtServerException {

        try (PreparedStatement deleteIDVProviderConfigsStmt = connection.prepareStatement(DELETE_IDVP_CONFIG_SQL)) {
            deleteIDVProviderConfigsStmt.setInt(1, idVId);
            deleteIDVProviderConfigsStmt.setInt(2, tenantId);
            deleteIDVProviderConfigsStmt.executeUpdate();
        } catch (SQLException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_DELETING_IDV_PROVIDER_CONFIGS,
                    String.valueOf(idVId), e);
        }
    }

    private void deleteIDVProviderClaims(int idVId, int tenantId, Connection connection)
            throws IdvProviderMgtServerException {

        try (PreparedStatement deleteIDVProviderClaimsStmt = connection.prepareStatement(DELETE_IDVP_CLAIM_SQL)) {
            deleteIDVProviderClaimsStmt.setInt(1, idVId);
            deleteIDVProviderClaimsStmt.setInt(2, tenantId);
            deleteIDVProviderClaimsStmt.executeUpdate();
        } catch (SQLException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_DELETING_IDV_PROVIDER_CLAIMS,
                    String.valueOf(idVId), e);
        }
    }

    private void updateIDVProviderClaims(IdentityVerificationProvider identityVerificationProvider,
                                         int idVPId, int tenantId, Connection connection)
            throws IdvProviderMgtServerException {

        deleteIDVProviderClaims(idVPId, tenantId, connection);
        if (MapUtils.isEmpty(identityVerificationProvider.getClaimMappings())) {
            return;
        }
        addIDVProviderClaims(identityVerificationProvider, idVPId, tenantId, connection);
    }

    private IdentityVerificationProvider getIdVProviderWithConfigs(IdentityVerificationProvider
                                                                           identityVerificationProvider, int tenantId,
                                                                   Connection connection)
            throws IdvProviderMgtServerException {

        IdVConfigProperty[] idVConfigProperties = new IdVConfigProperty[0];
        List<IdVConfigProperty> idVConfigPropertyList = new ArrayList<>();
        try (PreparedStatement getIdVProvidersStmt = connection.prepareStatement(GET_IDVP_CONFIG_SQL)) {
            getIdVProvidersStmt.setString(1, identityVerificationProvider.getId());
            getIdVProvidersStmt.setInt(2, tenantId);

            try (ResultSet idVProviderResultSet = getIdVProvidersStmt.executeQuery()) {
                while (idVProviderResultSet.next()) {
                    IdVConfigProperty idVConfigProperty = new IdVConfigProperty();
                    idVConfigProperty.setName(idVProviderResultSet.getString(PROPERTY_KEY));
                    idVConfigProperty.setValue(idVProviderResultSet.getString(PROPERTY_VALUE));
                    idVConfigProperty.setConfidential(idVProviderResultSet.getBoolean(IS_SECRET));
                    idVConfigPropertyList.add(idVConfigProperty);
                }
                identityVerificationProvider.setIdVConfigProperties(idVConfigPropertyList.toArray(idVConfigProperties));
            }
            IdVPSecretProcessor secretProcessor = new IdVPSecretProcessor();
            return secretProcessor.decryptAssociatedSecrets(identityVerificationProvider);
        } catch (SQLException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_RETRIEVING_IDV_PROVIDER_CONFIGS, e);
        } catch (SecretManagementException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_RETRIEVING_IDV_PROVIDER_SECRETS,
                    identityVerificationProvider.getIdVProviderName(), e);
        }
    }

    private void getIdVProvidersWithClaims(IdentityVerificationProvider identityVerificationProvider, int tenantId,
                                           Connection connection) throws IdvProviderMgtServerException {

        Map<String, String> idVClaimMap = new HashMap<>();
        try (PreparedStatement getIdVProvidersStmt = connection.prepareStatement(GET_IDVP_CLAIMS_SQL)) {
            getIdVProvidersStmt.setString(1, identityVerificationProvider.getId());
            getIdVProvidersStmt.setInt(2, tenantId);

            try (ResultSet idVProviderResultSet = getIdVProvidersStmt.executeQuery()) {
                while (idVProviderResultSet.next()) {
                    idVClaimMap.put(idVProviderResultSet.getString(CLAIM), idVProviderResultSet.getString(LOCAL_CLAIM));
                    identityVerificationProvider.setClaimMappings(idVClaimMap);
                }
            }
        } catch (SQLException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_RETRIEVING_IDV_PROVIDER_CLAIMS,
                    identityVerificationProvider.getIdVProviderName(), e);
        }
    }
}
