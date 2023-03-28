package org.wso2.carbon.extension.identity.verification.mgt;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;

import org.json.JSONObject;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.extension.identity.verification.mgt.dao.IdentityVerificationClaimDAOImpl;
import org.wso2.carbon.extension.identity.verification.mgt.internal.IdentityVerificationDataHolder;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdVClaim;

import org.wso2.carbon.extension.identity.verification.provider.IdVProviderManager;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@PrepareForTest({PrivilegedCarbonContext.class, IdentityDatabaseUtil.class, IdentityUtil.class,
        IdentityTenantUtil.class, IdentityVerificationDataHolder.class, IdVProviderManager.class})

public class IdentityVerificationManagerImplTest extends PowerMockTestCase {

    private IdentityVerificationManager identityVerification;
    @Mock
    private IdentityVerificationClaimDAOImpl identityVerificationClaimDAO;
    @Mock
    IdentityVerificationDataHolder identityVerificationDataHolder;
    @Mock
    private RealmService mockRealmService;
    @Mock
    UserRealm mockUserRealmFromRealmService;
    @Mock
    IdVProviderManager mockIdVProviderManager;
    @Mock
    AbstractUserStoreManager mockAbstractUserStoreManager;

    @BeforeMethod
    public void setUp() throws Exception {

        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());
        prepareConfigs();
    }

//    @DataProvider(name = "validParameters")
//    public Object[][] createValidParameters() {
//        return new Object[][] { { "user1", "claim1", 1 } };
//    }
//
//    @DataProvider(name = "invalidParameters")
//    public Object[][] createInvalidParameters() {
//        return new Object[][] {
//                { "", "claim1", 1, IdentityVerificationConstants.ErrorMessage.ERROR_INVALID_USER_ID },
//                { "user1", "", 1, IdentityVerificationConstants.ErrorMessage.ERROR_INVALID_IDV_CLAIM_ID },
//                { "user1", "claim1", 1, null },
//                { "user1", "claim1", 2, IdentityVerificationConstants.ErrorMessage.ERROR_INVALID_IDV_CLAIM_ID }
//        };
//    }

    @Test
    public void testGetIdVClaim() throws Exception {

        mockIdentityVerificationClaimDAO();
        identityVerification = new IdentityVerificationManagerImpl();
        mockIsExistingUserCheck();
        when(identityVerificationClaimDAO.isIdVClaimExist(anyString(), anyInt())).thenReturn(true);
        when(identityVerificationClaimDAO.getIDVClaim(anyString(), anyString(), anyInt())).
                thenReturn(getIdVClaim());
        PowerMockito.doNothing().when(identityVerificationClaimDAO).addIdVClaimList(any(), anyInt());

        when(identityVerificationDataHolder.getIdVProviderManager()).thenReturn(mockIdVProviderManager);
        when(mockIdVProviderManager.isIdVProviderExists(anyString(), anyInt())).thenReturn(true);

        identityVerification = new IdentityVerificationManagerImpl();
        IdVClaim idVClaim =
                identityVerification.getIdVClaim("715558cb-d9c1-4a23-af09-3d95284d8e2b",
                        "575a3d28-c6fb-46c8-bf63-45530448ca17", -1234);
        Assert.assertEquals(idVClaim.getClaimUri(), "http://wso2.org/claims/dob");
    }

    @Test
    public void testAddIdVClaims() throws Exception {

        mockIdentityVerificationClaimDAO();
        identityVerification = new IdentityVerificationManagerImpl();
        mockIsExistingUserCheck();
        when(identityVerificationClaimDAO.isIdVClaimDataExist(anyString(), anyString(), anyString(), anyInt())).
                thenReturn(false);
        PowerMockito.doNothing().when(identityVerificationClaimDAO).addIdVClaimList(anyList(), anyInt());

        when(identityVerificationDataHolder.getIdVProviderManager()).thenReturn(mockIdVProviderManager);
        when(mockIdVProviderManager.isIdVProviderExists(anyString(), anyInt())).thenReturn(true);

        List<IdVClaim> idVClaims = new ArrayList<>();
        idVClaims.add(getIdVClaim());

        List<IdVClaim> addedIdVClaims =
                identityVerification.addIdVClaims("715558cb-d9c1-4a23-af09-3d95284d8e2b",
                        idVClaims, -1234);
        Assert.assertEquals(addedIdVClaims.get(0).getClaimUri(), "http://wso2.org/claims/dob");
    }

    @Test
    public void testUpdateIdVClaims() throws Exception {

        mockIdentityVerificationClaimDAO();
        identityVerification = new IdentityVerificationManagerImpl();
        mockIsExistingUserCheck();
        when(identityVerificationClaimDAO.isIdVClaimExist(anyString(), anyInt())).
                thenReturn(true);
        PowerMockito.doNothing().when(identityVerificationClaimDAO).updateIdVClaim(any(IdVClaim.class), anyInt());

        IdVClaim idVClaim = getIdVClaim();

        IdVClaim updatedIdVClaim =
                identityVerification.updateIdVClaim("715558cb-d9c1-4a23-af09-3d95284d8e2b",
                        idVClaim, -1234);
        Assert.assertEquals(updatedIdVClaim.getClaimUri(), "http://wso2.org/claims/dob");
    }

    private void mockIdentityVerificationClaimDAO() {

        PowerMockito.mockStatic(IdentityVerificationDataHolder.class);
        when(IdentityVerificationDataHolder.getInstance()).thenReturn(identityVerificationDataHolder);
        when(identityVerificationDataHolder.getIdVClaimDAOs()).
                thenReturn(Collections.singletonList(identityVerificationClaimDAO));
    }

    private void mockIsExistingUserCheck() throws UserStoreException {

        when(identityVerificationDataHolder.getRealmService()).thenReturn(mockRealmService);
        when(mockRealmService.getTenantUserRealm(anyInt())).thenReturn(mockUserRealmFromRealmService);
        when(mockUserRealmFromRealmService.getUserStoreManager()).thenReturn(mockAbstractUserStoreManager);
        when(mockAbstractUserStoreManager.isExistingUserWithID(anyString())).thenReturn(true);
    }

    @Test
    public void testDeleteIDVClaim() throws Exception {

        mockIdentityVerificationClaimDAO();
        identityVerification = new IdentityVerificationManagerImpl();
        mockIsExistingUserCheck();

        PowerMockito.doNothing().when(identityVerificationClaimDAO).deleteIdVClaim(anyString(), anyString(), anyInt());

        identityVerification.deleteIDVClaim("715558cb-d9c1-4a23-af09-3d95284d8e2b",
                "575a3d28-c6fb-46c8-bf63-45530448ca17", -1234);
    }

    @Test
    public void testGetIDVClaims() throws Exception {

        mockIdentityVerificationClaimDAO();
        identityVerification = new IdentityVerificationManagerImpl();
        mockIsExistingUserCheck();
        when(identityVerificationClaimDAO.isIdVClaimExist(anyString(), anyInt())).thenReturn(true);
        when(identityVerificationClaimDAO.getIDVClaims(anyString(), anyInt())).
                thenReturn(getIdVClaims());

        when(identityVerificationDataHolder.getIdVProviderManager()).thenReturn(mockIdVProviderManager);
        when(mockIdVProviderManager.isIdVProviderExists(anyString(), anyInt())).thenReturn(true);

        identityVerification = new IdentityVerificationManagerImpl();
        IdVClaim[] idVClaims =
                identityVerification.getIdVClaims("715558cb-d9c1-4a23-af09-3d95284d8e2b", -1234);
        Assert.assertEquals(idVClaims.length, 1);
    }

//    }
//
//    @Test
//    public void testIsIdVClaimExists() throws IdentityVerificationException {
//
////        mockStatic(IdentityDatabaseUtil.class);
////        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
////        when(IdentityDatabaseUtil.getDBConnection(false)).thenReturn(connection);
////        when(identityVerificationClaimDAO.isIdVClaimExist("idVClaimId", -1234)).thenReturn(true);
////        Assert.assertTrue(identityVerificationClaimDAO.isIdVClaimExist("idVClaimId", -1234));
//    }
//
//    public Object[][] getIdVClaimTestData() {
//        String sampleUserId = "sampleUserId";
//        String sampleIdvClaimId = "sampleIdvClaimId";
//        int sampleTenantId = 123;
//        IdVClaim expectedClaim = mock(IdVClaim.class);
//
//        return new Object[][] {
//                // Valid input case
//                { sampleUserId, sampleIdvClaimId, sampleTenantId, true, true, expectedClaim, expectedClaim },
//
//                // Invalid IdVClaimId case
//                { sampleUserId, "", sampleTenantId, false, true, null,
//                        IdentityVerificationConstants.ErrorMessage.ERROR_INVALID_IDV_CLAIM_ID },
//
//                // Invalid userId case
//                { "", sampleIdvClaimId, sampleTenantId, true, false, null,
//                        IdentityVerificationConstants.ErrorMessage.ERROR_INVALID_USER_ID }
//        };
//    }

    private void prepareConfigs() {

        mockCarbonContextForTenant(SUPER_TENANT_ID, SUPER_TENANT_DOMAIN_NAME);
        mockIdentityTenantUtility();
    }


    private void mockCarbonContextForTenant(int tenantId, String tenantDomain) {

        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext privilegedCarbonContext = PowerMockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        PowerMockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn(tenantDomain);
        PowerMockito.when(privilegedCarbonContext.getTenantId()).thenReturn(tenantId);
        PowerMockito.when(privilegedCarbonContext.getUsername()).thenReturn("admin");
    }

    private void mockIdentityTenantUtility() {

        PowerMockito.mockStatic(IdentityTenantUtil.class);
        IdentityTenantUtil identityTenantUtil = PowerMockito.mock(IdentityTenantUtil.class);
        PowerMockito.when(identityTenantUtil.getTenantDomain(any(Integer.class))).thenReturn(SUPER_TENANT_DOMAIN_NAME);
    }

    private IdVClaim[] getIdVClaims() {

        IdVClaim[] idVClaims = new IdVClaim[1];
        idVClaims[0] = getIdVClaim();
        return idVClaims;
    }

    private static IdVClaim getIdVClaim() {

        IdVClaim idVClaim = new IdVClaim();
        idVClaim.setUuid("d245799b-28bc-4fdb-abb4-e265038320by");
        idVClaim.setUserId("715558cb-d9c1-4a23-af09-3d95284d8e2b");
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