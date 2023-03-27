package org.wso2.carbon.extension.identity.verification.mgt;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;
import static org.wso2.carbon.extension.identity.verification.mgt.util.TestUtils.closeH2Base;
import static org.wso2.carbon.extension.identity.verification.mgt.util.TestUtils.getConnection;
import static org.wso2.carbon.extension.identity.verification.mgt.util.TestUtils.initiateH2Base;

import org.json.JSONObject;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.extension.identity.verification.mgt.dao.IdentityVerificationClaimDAO;
import org.wso2.carbon.extension.identity.verification.mgt.dao.IdentityVerificationClaimDAOImpl;
import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationException;
import org.wso2.carbon.extension.identity.verification.mgt.internal.IdentityVerificationDataHolder;
import org.wso2.carbon.extension.identity.verification.mgt.internal.IdentityVerificationServiceComponent;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdVClaim;

import org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants;
import org.wso2.carbon.extension.identity.verification.provider.IdVPSecretProcessor;
import org.wso2.carbon.extension.identity.verification.provider.IdVProviderManager;
import org.wso2.carbon.extension.identity.verification.provider.IdVProviderManagerImpl;
import org.wso2.carbon.extension.identity.verification.provider.dao.IdVProviderDAO;
import org.wso2.carbon.extension.identity.verification.provider.dao.IdVProviderDAOImpl;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtException;
import org.wso2.carbon.extension.identity.verification.provider.internal.IdVProviderDataHolder;
import org.wso2.carbon.extension.identity.verification.provider.model.IdentityVerificationProvider;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UniqueIDUserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.User;
import org.wso2.carbon.user.core.service.RealmService;

import java.nio.file.Paths;
import java.sql.Connection;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.Boolean.FALSE;

@PrepareForTest({PrivilegedCarbonContext.class, IdentityDatabaseUtil.class, IdentityUtil.class,
        IdentityTenantUtil.class, IdentityVerificationDataHolder.class, IdVProviderManager.class})
public class IdentityVerificationManagementTest extends PowerMockTestCase {

    private IdentityVerificationMgt identityVerification;
    private org.wso2.carbon.user.api.UserRealm mockUserRealm;
    private final IdentityVerificationClaimDAO identityVerificationClaimDAO = new IdentityVerificationClaimDAOImpl();

    @Mock
    IdentityVerificationDataHolder identityVerificationDataHolder;

    @BeforeMethod
    public void setUp() throws Exception {

//        initiateH2Base();
        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());

//        Connection connection = getConnection();


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
//
//    @Test(dataProvider = "validParameters")
//    public void testGetIdVClaim(String userId, String idvClaimId, int tenantId) throws IdentityVerificationException {
////        // Given
////        IdVClaim claim = new IdVClaim();
////        when(identityVerificationClaimDAO.getIDVClaim(userId, idvClaimId, tenantId)).thenReturn(claim);
////
////        // When
////        IdVClaim result = identityVerification.getIdVClaim(userId, idvClaimId, tenantId);
////
////        // Then
////        Assert.assertEquals(result, claim);
////        verify(identityVerificationClaimDAO).getIDVClaim(userId, idvClaimId, tenantId);
//    }
//
////    @Test(dataProvider = "invalidParameters")
////    public void testGetIdVClaimInvalidParameters(String userId, String idvClaimId, int tenantId,
////                                                 IdentityVerificationConstants.ErrorMessage expectedErrorMessage) {
////        // Given
////        if (expectedErrorMessage != null) {
////            when(identityVerificationClaimDAO.isIdVClaimExist(idvClaimId, tenantId)).thenReturn(false);
////        }
////
////        try {
////            // When
////            identityVerification.getIdVClaim(userId, idvClaimId, tenantId);
////
////            // Then expect IdentityVerificationException to be thrown
////            Assert.fail("Expected IdentityVerificationException not thrown.");
////        } catch (IdentityVerificationException ex) {
////            // Then
////            if (expectedErrorMessage != null) {
////                Assert.assertEquals(ex.getErrorMessage(), expectedErrorMessage);
////            } else {
////                Assert.assertNull(ex.getErrorMessage());
////            }
////        }
////    }
//
//    @Test
//    public void testGetInstance() {
//
//    }
//
//    @Test
//    public void testVerifyIdentity() {
//
//    }
//
////    @Test(dataProvider = "getIdVClaimTestData")
////    public void testGetIdVClaim(String userId, String idvClaimId, int tenantId, boolean isIdVClaimExists,
////                                boolean isValidUserId, IdVClaim expectedClaim, Object expectedException)
////            throws IdentityVerificationException {
////
////        when(identityVerificationClaimDAO.getIDVClaim(userId, idvClaimId, tenantId)).thenReturn(idVClaim);
////        when(identityVerificationClaimDAO.isIdVClaimExist(idvClaimId, tenantId)).thenReturn(true);
////
////        RealmService realmService = Mockito.mock(RealmService.class);
////        UserRealm userRealm = Mockito.mock(UserRealm.class);
////        UserStoreManager userStoreManager = Mockito.mock(UniqueIDUserStoreManager.class);
////
////        Mockito.when(realmService.getTenantUserRealm(Mockito.anyInt())).thenReturn(userRealm);
////        Mockito.when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
////
////        UniqueIDUserStoreManager result = myClass.getUniqueIdEnabledUserStoreManager(realmService, "test");
////
////        when(identityVerificationClaimManager.isValidUserId(userId, tenantId)).thenReturn(true);
////        IdVClaim expectedClaim = mock(IdVClaim.class);
////        when(identityVerificationClaimDAO.getIDVClaim(userId, idvClaimId, tenantId))
////                .thenReturn(expectedClaim);
////        when(identityVerificationClaimManager.isIdVClaimExists(sampleIdvClaimId, sampleTenantId)).thenReturn(true);
////        when(identityVerificationClaimManager.isValidUserId(sampleUserId, sampleTenantId)).thenReturn(true);
////
////        // Execution
////        IdentityClaim idvClaim = identityVerificationManagement.getIdVClaim(identityProvider, sampleClaimUri);
////
////        // Verification
////        assertEquals(idvClaim, identityClaim);
////    }

    @Test
    public void testAddIdVClaims() throws Exception {

        PowerMockito.mockStatic(IdentityDatabaseUtil.class);

        IdentityVerificationClaimDAOImpl identityVerificationClaimDAO =
                mock(IdentityVerificationClaimDAOImpl.class);
        PowerMockito.whenNew(IdentityVerificationClaimDAOImpl.class).withNoArguments().
                thenReturn(identityVerificationClaimDAO);
        when(identityVerificationClaimDAO.isIdVClaimDataExist(anyString(), anyString(), anyString(), anyInt()))
                .thenReturn(FALSE);
        PowerMockito.doNothing().when(identityVerificationClaimDAO).addIdVClaimList(any(), anyInt());
        RealmService mockRealmService = mock(RealmService.class);
        UserRealm mockUserRealmFromRealmService = mock(UserRealm.class);
        AbstractUserStoreManager mockAbstractUserStoreManager = mock(AbstractUserStoreManager.class);
        IdVProviderManager mockIdVProviderManager = mock(IdVProviderManager.class);

        PowerMockito.mockStatic(IdentityVerificationDataHolder.class);
        when(IdentityVerificationDataHolder.getInstance())
                .thenReturn(identityVerificationDataHolder);
        when(identityVerificationDataHolder.getRealmService()).thenReturn(mockRealmService);
        when(mockRealmService.getTenantUserRealm(anyInt())).thenReturn(mockUserRealmFromRealmService);
        when(mockUserRealmFromRealmService.getUserStoreManager()).thenReturn(mockAbstractUserStoreManager);
        when(mockAbstractUserStoreManager.isExistingUserWithID(anyString())).thenReturn(true);

        when(identityVerificationDataHolder.getIdVProviderManager()).thenReturn(mockIdVProviderManager);
        when(mockIdVProviderManager.isIdVProviderExists(anyString(), anyInt())).thenReturn(true);

        List<IdVClaim> idVClaims = addTestIdVClaims();
        List<IdVClaim> addedIdVClaims =
                identityVerification.addIdVClaims("715558cb-d9c1-4a23-af09-3d95284d8e2b",
                        idVClaims, -1234);
        Assert.assertEquals(addedIdVClaims.get(0).getUuid(), "d245799b-28bc-4fdb-abb4-e265038320by");
//        connection.close();
//        closeH2Base();
    }
//
//    @Test
//    public void testUpdateIdVClaim() {
//
//    }
//
//    @Test
//    public void testDeleteIDVClaim() {
//
//    }
//
//    @Test
//    public void testGetIDVClaims() {
//
//    }
//
//    @Test
//    public void testIsIdVClaimDataExists() {
//
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
        identityVerification = IdentityVerificationManagement.getInstance();
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

    private List<IdVClaim> addTestIdVClaims() {

        List<IdVClaim> idVClaims = new ArrayList<>();
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
        idVClaims.add(idVClaim);
        return idVClaims;
    }
}