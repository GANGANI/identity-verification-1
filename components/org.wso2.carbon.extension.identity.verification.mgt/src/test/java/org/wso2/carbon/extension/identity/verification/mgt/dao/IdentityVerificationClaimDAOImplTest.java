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

    @BeforeMethod
    public void setUp() throws Exception {

        identityVerificationClaimDAO = new IdentityVerificationClaimDAOImpl();
        IdentityVerificationDataHolder.getInstance().
                setIdVClaimDAOs(Collections.singletonList(identityVerificationClaimDAO));
        initiateH2Database(DB_NAME, getFilePath("h2.sql"));
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
            identityVerificationClaimDAO.addIdVClaimList(idVClaimList, -1234);
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
            identityVerificationClaimDAO.updateIdVClaim(updatedClaim, -1234);
            Assert.assertFalse(updatedClaim.getStatus());
        }
    }

    @Test
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
                    getIDVClaim("715558cb-d9c1-4a23-af09-3d95284d8e2b",
                            "575a3d28-c6fb-46c8-bf63-45530448ca17", -1234);
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
            identityVerificationClaimDAO.addIdVClaimList(idVClaimList, -1234);
        }

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));

            IdVClaim[] retrievedIdVClaimList = identityVerificationClaimDAO.
                    getIDVClaims("715558cb-d9c1-4a23-af09-3d95284d8e2b", -1234);
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

            identityVerificationClaimDAO.deleteIdVClaim("715558cb-d9c1-4a23-af09-3d95284d8e2b",
                    "575a3d28-c6fb-46c8-bf63-45530448ca17", -1234);
            // todo
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
                    identityVerificationClaimDAO.isIdVClaimDataExist("715558cb-d9c1-4a23-af09-3d95284d8e2b",
                    "1c7ce08b-2ebc-4b9e-a107-3b129c019954", "http://wso2.org/claims/dob", -1234);
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

            boolean isIdVClaimDataExist = identityVerificationClaimDAO.
                            isIdVClaimExist("575a3d28-c6fb-46c8-bf63-45530448ca17", -1234);
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

    private static IdVClaim getIdVClaim() {

        IdVClaim idVClaim = new IdVClaim();
        idVClaim.setUserId("715558cb-d9c1-4a23-af09-3d95284d8e2b");
        idVClaim.setUuid("575a3d28-c6fb-46c8-bf63-45530448ca17");
        idVClaim.setClaimUri("http://wso2.org/claims/dob");
        idVClaim.setStatus(true);
        idVClaim.setIdVPId("1c7ce08b-2ebc-4b9e-a107-3b129c019954");
        idVClaim.setMetadata(new JSONObject("{\n" +
                "      \"source\": \"evidentID\",\n" +
                "      \"trackingId\": \"123e4567-e89b-12d3-a456-556642440000\"\n" +
                "    }"));
        return idVClaim;
    }

    private static IdVClaim getIdVClaim2() {

        IdVClaim idVClaim = new IdVClaim();
        idVClaim.setUserId("715558cb-d9c1-4a23-af09-3d95284d8e2b");
        idVClaim.setUuid("25uhd8-c6fb-46c8-bf63-45530448ca17");
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

    private void initiateH2Database(String databaseName, String scriptPath) throws Exception {

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("username");
        dataSource.setPassword("password");
        dataSource.setUrl("jdbc:h2:mem:test" + databaseName);
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().executeUpdate("RUNSCRIPT FROM '" + scriptPath + "'");
        }
        dataSourceMap.put(databaseName, dataSource);
    }
}
