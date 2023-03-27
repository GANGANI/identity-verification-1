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
package org.wso2.carbon.extension.identity.verification.mgt.dao;

import org.json.JSONObject;
import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationException;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdVClaim;
import org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants;
import org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationExceptionMgt;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants.CLAIM_URI;
import static org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants.ErrorMessage.ERROR_ADDING_IDV_CLAIM;
import static org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants.ErrorMessage.ERROR_ADDING_IDV_CLAIMS;
import static org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants.ErrorMessage.ERROR_CHECKING_IDV_CLAIM_EXISTENCE;
import static org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants.ErrorMessage.ERROR_DELETING_IDV_CLAIM;
import static org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants.ErrorMessage.ERROR_RETRIEVING_IDV_CLAIM;
import static org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants.ErrorMessage.ERROR_RETRIEVING_IDV_CLAIMS;
import static org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants.ErrorMessage.ERROR_UPDATING_IDV_CLAIM;
import static org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants.IDVP_ID;
import static org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants.IS_VERIFIED;
import static org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants.METADATA;
import static org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants.SQLQueries.ADD_IDV_CLAIM_SQL;
import static org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants.SQLQueries.DELETE_IDV_CLAIM_SQL;
import static org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants.SQLQueries.GET_IDV_CLAIMS_SQL;
import static org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants.SQLQueries.GET_IDV_CLAIM_SQL;
import static org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants.SQLQueries.IS_IDV_CLAIM_DATA_EXIST_SQL;
import static org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants.SQLQueries.IS_IDV_CLAIM_EXIST_SQL;
import static org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants.SQLQueries.UPDATE_IDV_CLAIM_SQL;
import static org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants.USER_ID;
import static org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants.UUID;

/**
 * Identity verification claim DAO class.
 */
public class IdentityVerificationClaimDAOImpl implements IdentityVerificationClaimDAO {

    @Override
    public void addIdVClaimList(List<IdVClaim> idvClaimList, int tenantId) throws IdentityVerificationException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            for (IdVClaim idVClaim : idvClaimList) {
                try (PreparedStatement addIdVProviderStmt = connection.prepareStatement(ADD_IDV_CLAIM_SQL)) {
                    addIdVProviderStmt.setString(1, idVClaim.getUuid());
                    addIdVProviderStmt.setString(2, idVClaim.getUserId());
                    addIdVProviderStmt.setString(3, idVClaim.getClaimUri());
                    addIdVProviderStmt.setString(4, idVClaim.getIdVPId());
                    addIdVProviderStmt.setInt(5, tenantId);
                    if (idVClaim.getStatus()) {
                        addIdVProviderStmt.setString(6, "1");
                    } else {
                        addIdVProviderStmt.setString(6, "0");
                    }
                    addIdVProviderStmt.setBytes(7, getMetadata(idVClaim));
                    addIdVProviderStmt.executeUpdate();
                } catch (SQLException e1) {
                    throw IdentityVerificationExceptionMgt.handleServerException(ERROR_ADDING_IDV_CLAIM, e1);
                }
            }
        } catch (SQLException e) {
            throw IdentityVerificationExceptionMgt.handleServerException(ERROR_ADDING_IDV_CLAIMS, e);
        }
    }

    @Override
    public void updateIdVClaim(IdVClaim idVClaim, int tenantId) throws IdentityVerificationException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (PreparedStatement updateIdVProviderStmt = connection.prepareStatement(UPDATE_IDV_CLAIM_SQL)) {
                updateIdVProviderStmt.setBoolean(1, idVClaim.getStatus());
                updateIdVProviderStmt.setObject(2, getMetadata(idVClaim));
                updateIdVProviderStmt.setString(3, idVClaim.getUserId());
                updateIdVProviderStmt.setString(4, idVClaim.getUuid());
                updateIdVProviderStmt.setInt(5, tenantId);
                updateIdVProviderStmt.executeUpdate();
                IdentityDatabaseUtil.commitTransaction(connection);
            } catch (SQLException e1) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                throw IdentityVerificationExceptionMgt.handleServerException(ERROR_UPDATING_IDV_CLAIM, e1);
            }
        } catch (SQLException e) {
            throw IdentityVerificationExceptionMgt.handleServerException(ERROR_UPDATING_IDV_CLAIM, e);
        }
    }

    @Override
    public IdVClaim getIDVClaim(String userId, String idVClaimId, int tenantId) throws IdentityVerificationException {

        IdVClaim idVClaim = null;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement getIdVProviderStmt = connection.prepareStatement(GET_IDV_CLAIM_SQL)) {
            getIdVProviderStmt.setString(1, userId);
            getIdVProviderStmt.setString(2, idVClaimId);
            getIdVProviderStmt.setInt(3, tenantId);
            getIdVProviderStmt.execute();
            try (ResultSet idVProviderResultSet = getIdVProviderStmt.executeQuery()) {
                while (idVProviderResultSet.next()) {
                    idVClaim = new IdVClaim();
                    idVClaim.setUuid(idVProviderResultSet.getString(UUID));
                    idVClaim.setUserId(idVProviderResultSet.getString(USER_ID));
                    idVClaim.setClaimUri(idVProviderResultSet.getString(CLAIM_URI));
                    idVClaim.setIdVPId(idVProviderResultSet.getString(IDVP_ID));
                    idVClaim.setStatus(idVProviderResultSet.getBoolean(IS_VERIFIED));
                    idVClaim.setMetadata(getMetadataJsonObject(idVProviderResultSet.getBytes(METADATA)));
                }
            }
        } catch (SQLException e) {
            throw IdentityVerificationExceptionMgt.handleServerException(ERROR_RETRIEVING_IDV_CLAIM, e);
        }
        return idVClaim;
    }

    @Override
    public IdVClaim[] getIDVClaims(String userId, int tenantId) throws IdentityVerificationException {

        List<IdVClaim> idVClaims = new ArrayList<>();
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement getIdVProviderStmt = connection.prepareStatement(GET_IDV_CLAIMS_SQL)) {
            getIdVProviderStmt.setString(1, userId);
            getIdVProviderStmt.setInt(2, tenantId);
            getIdVProviderStmt.execute();
            try (ResultSet idVProviderResultSet = getIdVProviderStmt.executeQuery()) {
                while (idVProviderResultSet.next()) {
                    IdVClaim idVClaim = new IdVClaim();
                    idVClaim.setUuid(idVProviderResultSet.getString(UUID));
                    idVClaim.setUserId(idVProviderResultSet.getString(USER_ID));
                    idVClaim.setClaimUri(idVProviderResultSet.getString(CLAIM_URI));
                    idVClaim.setStatus(idVProviderResultSet.getBoolean(IS_VERIFIED));
                    idVClaim.setMetadata(getMetadataJsonObject(idVProviderResultSet.getBytes(METADATA)));
                    idVClaims.add(idVClaim);
                }
            }
        } catch (SQLException e) {
            throw IdentityVerificationExceptionMgt.handleServerException(ERROR_RETRIEVING_IDV_CLAIMS, e);
        }
        return idVClaims.toArray(new IdVClaim[0]);
    }

    @Override
    public void deleteIdVClaim(String idVClaimId, int tenantId) throws IdentityVerificationException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement deleteIdVProviderStmt = connection.prepareStatement(DELETE_IDV_CLAIM_SQL)) {
            deleteIdVProviderStmt.setString(1, idVClaimId);
            deleteIdVProviderStmt.setInt(2, tenantId);
            deleteIdVProviderStmt.executeUpdate();
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            throw IdentityVerificationExceptionMgt.handleServerException(ERROR_DELETING_IDV_CLAIM, e);
        }
    }

    @Override
    public boolean isIdVClaimDataExist(String userId, String idvId, String uri, int tenantId)
            throws IdentityVerificationException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement getIdVProviderStmt = connection.prepareStatement(IS_IDV_CLAIM_DATA_EXIST_SQL)) {
            getIdVProviderStmt.setString(1, userId);
            getIdVProviderStmt.setString(2, idvId);
            getIdVProviderStmt.setString(3, uri);
            getIdVProviderStmt.setInt(4, tenantId);
            getIdVProviderStmt.execute();
            try (ResultSet idVProviderResultSet = getIdVProviderStmt.executeQuery()) {
                if (idVProviderResultSet.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            throw IdentityVerificationExceptionMgt.handleServerException(ERROR_CHECKING_IDV_CLAIM_EXISTENCE, e);
        }
        return false;
    }

    @Override
    public boolean isIdVClaimExist(String claimId, int tenantId) throws IdentityVerificationException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement getIdVProviderStmt = connection.prepareStatement(IS_IDV_CLAIM_EXIST_SQL)) {
            getIdVProviderStmt.setString(1, claimId);
            getIdVProviderStmt.setInt(2, tenantId);
            getIdVProviderStmt.execute();
            try (ResultSet idVProviderResultSet = getIdVProviderStmt.executeQuery()) {
                if (idVProviderResultSet.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            throw IdentityVerificationExceptionMgt.handleServerException(ERROR_CHECKING_IDV_CLAIM_EXISTENCE, e);
        }
        return false;
    }

    private byte[] getMetadata(IdVClaim idVClaim) {

        String metadataString = idVClaim.getMetadata().toString();
        return metadataString.getBytes(StandardCharsets.UTF_8);
    }

    private JSONObject getMetadataJsonObject(byte[] metadata) {

        String metadataString = new String(metadata, StandardCharsets.UTF_8);
        return new JSONObject(metadataString);
    }
}
