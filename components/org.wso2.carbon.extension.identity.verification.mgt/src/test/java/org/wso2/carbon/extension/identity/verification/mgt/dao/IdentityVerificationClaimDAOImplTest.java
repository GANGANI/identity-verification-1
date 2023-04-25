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

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.extension.identity.verification.mgt.internal.IdentityVerificationDataHolder;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdVClaim;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertNotNull;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;

@PrepareForTest({PrivilegedCarbonContext.class, IdentityDatabaseUtil.class, IdentityUtil.class,
        IdentityTenantUtil.class})
public class IdentityVerificationClaimDAOImplTest extends PowerMockTestCase {

    private IdentityVerificationClaimDAO identityVerificationClaimDAO;
    private static Map<String, BasicDataSource> dataSourceMap = new HashMap<>();
    private static final String DB_NAME = "test";
    private final String idvClaimUUID = "d245799b-28bc-4fdb-abb4-e265038320by";
    private final String userId = "715558cb-d9c1-4a23-af09-3d95284d8e2b";
    private final String idvProviderId = "1c7ce08b-2ebc-4b9e-a107-3b129c019954";
    private final int tenantId = -1234;

    @BeforeMethod
    public void setUp() throws Exception {

        identityVerificationClaimDAO = new IdentityVerificationClaimDAOImpl();
        IdentityVerificationDataHolder.getInstance().
                setIdVClaimDAOs(Collections.singletonList(identityVerificationClaimDAO));
        initiateH2Database(getFilePath("h2.sql"));
        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());
        prepareConfigs();
    }

    @AfterMethod
    public void tearDown() throws Exception {

        closeH2Database();
    }

    @Test(priority = 1)
    public void testAddIdVClaimList() throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            List<IdVClaim> idVClaimList = getTestIdVClaims();
            identityVerificationClaimDAO.addIdVClaimList(idVClaimList, tenantId);
            for (IdVClaim claim : idVClaimList) {
                // todo
                assertNotNull(claim);
            }
        }
    }

    @Test(priority = 2)
    public void testUpdateIdVClaim() throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            IdVClaim updatedClaim = getIdVClaim2();
            identityVerificationClaimDAO.updateIdVClaim(updatedClaim, tenantId);
            Assert.assertFalse(updatedClaim.getStatus());
        }
    }

    @Test(priority = 3)
    public void testGetIDVClaim() throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        List<IdVClaim> idVClaimList = getTestIdVClaims();
        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            identityVerificationClaimDAO.addIdVClaimList(idVClaimList, -1234);
        }

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));

            IdVClaim identityVerificationClaim = identityVerificationClaimDAO.
                    getIDVClaim(userId, idvClaimUUID, tenantId);
            Assert.assertEquals(identityVerificationClaim.getClaimUri(), idVClaimList.get(0).getClaimUri());
        }
    }

    @Test
    public void testGetIDVClaims() throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        List<IdVClaim> idVClaimList = getTestIdVClaims();
        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            identityVerificationClaimDAO.addIdVClaimList(idVClaimList, tenantId);
        }

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));

            IdVClaim[] retrievedIdVClaimList = identityVerificationClaimDAO.
                    getIDVClaims(userId, tenantId);
            Assert.assertEquals(retrievedIdVClaimList.length, idVClaimList.size());
        }
    }

    @Test
    public void testDeleteIdVClaim() throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        List<IdVClaim> idVClaimList = getTestIdVClaims();
        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            identityVerificationClaimDAO.addIdVClaimList(idVClaimList, -1234);
        }

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));

            identityVerificationClaimDAO.deleteIdVClaim(userId, idvClaimUUID, tenantId);
        }
    }

    @Test
    public void testIsIdVClaimDataExist() throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        List<IdVClaim> idVClaimList = getTestIdVClaims();
        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            identityVerificationClaimDAO.addIdVClaimList(idVClaimList, -1234);
        }

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));

            boolean isIdVClaimDataExist =
                    identityVerificationClaimDAO.isIdVClaimDataExist(userId, idvProviderId,
                            "http://wso2.org/claims/dob", tenantId);
            Assert.assertTrue(isIdVClaimDataExist);
        }
    }

    @Test
    public void testIsIdVClaimExist() throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        List<IdVClaim> idVClaimList = getTestIdVClaims();
        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            identityVerificationClaimDAO.addIdVClaimList(idVClaimList, -1234);
        }

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));

            boolean isIdVClaimDataExist = identityVerificationClaimDAO.isIdVClaimExist(idvClaimUUID, tenantId);
            Assert.assertTrue(isIdVClaimDataExist);
        }
    }

    @Test
    public void testGetPriority() {

        int priority = identityVerificationClaimDAO.getPriority();
        Assert.assertEquals(priority, 1);
    }

    private void prepareConfigs() {

        mockCarbonContextForTenant(SUPER_TENANT_ID, SUPER_TENANT_DOMAIN_NAME);
        mockIdentityTenantUtility();
    }

    private void mockCarbonContextForTenant(int tenantId, String tenantDomain) {

        mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext privilegedCarbonContext = mock(PrivilegedCarbonContext.class);
        when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        when(privilegedCarbonContext.getTenantDomain()).thenReturn(tenantDomain);
        when(privilegedCarbonContext.getTenantId()).thenReturn(tenantId);
        when(privilegedCarbonContext.getUsername()).thenReturn("admin");
    }

    private void mockIdentityTenantUtility() {

        mockStatic(IdentityTenantUtil.class);
        IdentityTenantUtil identityTenantUtil = mock(IdentityTenantUtil.class);
        when(identityTenantUtil.getTenantDomain(any(Integer.class))).thenReturn(SUPER_TENANT_DOMAIN_NAME);
    }

    private List<IdVClaim> getTestIdVClaims() {

        List<IdVClaim> idVClaims = new ArrayList<>();
        IdVClaim idVClaim = getIdVClaim();
        idVClaims.add(idVClaim);
        return idVClaims;
    }

    private IdVClaim getIdVClaim() {

        IdVClaim idVClaim = new IdVClaim();
        idVClaim.setUserId(userId);
        idVClaim.setUuid(idvClaimUUID);
        idVClaim.setClaimUri("http://wso2.org/claims/dob");
        idVClaim.setStatus(true);
        idVClaim.setIdVPId(idvProviderId);
        idVClaim.setMetadata(new JSONObject("{\n" +
                "      \"source\": \"evidentID\",\n" +
                "      \"trackingId\": \"123e4567-e89b-12d3-a456-556642440000\"\n" +
                "    }"));
        return idVClaim;
    }

    private static IdVClaim getIdVClaim2() {

        IdVClaim idVClaim = new IdVClaim();
        idVClaim.setUserId("715558cb-d9c1-4a23-af09-3d95284d8e2b");
        idVClaim.setUuid("575a3d28-c6fb-46c8-bf63-45530448ca17");
        idVClaim.setClaimUri("http://wso2.org/claims/dob");
        idVClaim.setStatus(false);
        idVClaim.setIdVPId("1c7ce08b-2ebc-4b9e-a107-3b129c019954");
        idVClaim.setMetadata(new JSONObject("{\n" +
                "      \"source\": \"evidentID\",\n" +
                "      \"trackingId\": \"123e4567-e89b-12d3-a456-556642440000\"\n" +
                "    }"));
        return idVClaim;
    }

    public static void closeH2Database() throws Exception {

        BasicDataSource dataSource = dataSourceMap.get(DB_NAME);
        if (dataSource != null) {
            dataSource.close();
        }
    }

    private static String getFilePath(String fileName) {

        if (StringUtils.isNotBlank(fileName)) {
            return Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "dbscripts", fileName)
                    .toString();
        }
        throw new IllegalArgumentException("DB Script file name cannot be empty.");
    }

    private static Connection getConnection(String database) throws SQLException {

        if (dataSourceMap.get(database) != null) {
            return dataSourceMap.get(database).getConnection();
        }
        throw new RuntimeException("No datasource initiated for database: " + database);
    }

    private void initiateH2Database(String scriptPath) throws Exception {

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("username");
        dataSource.setPassword("password");
        dataSource.setUrl("jdbc:h2:mem:test" + IdentityVerificationClaimDAOImplTest.DB_NAME);
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().executeUpdate("RUNSCRIPT FROM '" + scriptPath + "'");
        }
        dataSourceMap.put(IdentityVerificationClaimDAOImplTest.DB_NAME, dataSource);
    }
}
