package org.wso2.carbon.extension.identity.verification.mgt.dao;

import org.apache.commons.dbcp.BasicDataSource;
import org.json.JSONObject;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationException;
import org.wso2.carbon.extension.identity.verification.mgt.internal.IdentityVerificationDataHolder;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdVClaim;
import org.wso2.carbon.extension.identity.verification.provider.IdVPSecretProcessor;
import org.wso2.carbon.extension.identity.verification.provider.IdVProviderManagerImpl;
import org.wso2.carbon.extension.identity.verification.provider.dao.IdVProviderDAO;
import org.wso2.carbon.extension.identity.verification.provider.dao.IdVProviderDAOImpl;
import org.wso2.carbon.extension.identity.verification.provider.internal.IdVProviderDataHolder;
import org.wso2.carbon.extension.identity.verification.provider.model.IdentityVerificationProvider;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertNotNull;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;
import static org.wso2.carbon.extension.identity.verification.mgt.util.TestUtils.DB_NAME;
import static org.wso2.carbon.extension.identity.verification.mgt.util.TestUtils.H2_SCRIPT_NAME;
import static org.wso2.carbon.extension.identity.verification.mgt.util.TestUtils.dataSourceMap;
import static org.wso2.carbon.extension.identity.verification.mgt.util.TestUtils.getConnection;
import static org.wso2.carbon.extension.identity.verification.mgt.util.TestUtils.getFilePath;

@PrepareForTest({PrivilegedCarbonContext.class, IdentityDatabaseUtil.class, IdentityUtil.class,
        IdentityTenantUtil.class})
public class IdentityVerificationClaimDAOImplTest extends PowerMockTestCase {

    private IdentityVerificationClaimDAO identityVerificationClaimDAO;

    public void setUp() throws Exception {

        initiateH2Base();
        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());



        prepareConfigs();
    }

    @Test
    public void testAddIdVClaimList() throws Exception {

        Connection connection = getConnection();
        mockStatic(IdentityDatabaseUtil.class);
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
        identityVerificationClaimDAO = new IdentityVerificationClaimDAOImpl();
        IdentityVerificationDataHolder.getInstance().
                setIdVClaimDAOs(Collections.singletonList(identityVerificationClaimDAO));
        List<IdVClaim> idVClaimList = addTestIdVClaims();
        identityVerificationClaimDAO.addIdVClaimList(idVClaimList, -1234);
        for (IdVClaim claim : idVClaimList) {
            assertNotNull(claim.getId());
        }
    }

    @Test
    public void testUpdateIdVClaim() {

    }

//    @Test
//    public void testGetIDVClaim() throws Exception {
//
//        IdVClaim idVClaim = identityVerificationClaimDAO.getIDVClaim("715558cb-d9c1-4a23-af09-3d95284d8e2b",
//                "575a3d28-c6fb-46c8-bf63-45530448ca17", -1234);
//    }

    @Test
    public void testGetIDVClaims() {

    }

    @Test
    public void testDeleteIdVClaim() {

    }

    @Test
    public void testIsIdVClaimDataExist() {

    }

    @Test
    public void testIsIdVClaimExist() {

    }

    public static void initiateH2Base() throws Exception {

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("username");
        dataSource.setPassword("password");
        dataSource.setUrl("jdbc:h2:mem:test" + DB_NAME);
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().executeUpdate("RUNSCRIPT FROM '" + getFilePath(H2_SCRIPT_NAME) + "'");
        }
        dataSourceMap.put(DB_NAME, dataSource);
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

    private List<IdVClaim> addTestIdVClaims() {

        List<IdVClaim> idVClaims = new ArrayList<>();
        IdVClaim idVClaim = getIdVClaim();
        idVClaims.add(idVClaim);
        return idVClaims;
    }

    private static IdVClaim getIdVClaim() {

        IdVClaim idVClaim = new IdVClaim();
        idVClaim.setUuid("d245799b-28bc-4fdb-abb4-e265038320by");
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
}
