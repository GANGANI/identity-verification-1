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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

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

import org.wso2.carbon.extension.identity.verification.mgt.model.IdVProperty;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdentityVerifierData;
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
    @Mock
    IdentityVerifierFactory identityVerifierFactory;
    @Mock
    IdentityVerifier identityVerifier;

    private final String idvClaimUUID = "d245799b-28bc-4fdb-abb4-e265038320by";
    private final String userId = "715558cb-d9c1-4a23-af09-3d95284d8e2b";
    private final String idvProviderId = "1c7ce08b-2ebc-4b9e-a107-3b129c019954";
    private final String idvProviderName = "ONFIDO";
    private final int tenantId = -1234;

    @BeforeMethod
    public void setUp() throws Exception {

        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());
        prepareConfigs();
        mockIsExistingUserCheck();
        mockIdentityVerificationClaimDAO();
        identityVerification = new IdentityVerificationManagerImpl();
    }

    @Test
    public void testVerifyIdentity() throws Exception {

        IdentityVerifierData identityVerifierData = new IdentityVerifierData();
        identityVerifierData.setIdVProviderName(idvProviderName);
        IdVProperty idVProperty = new IdVProperty();
        idVProperty.setName("token");
        idVProperty.setValue("123456");
        identityVerifierData.addIdVProperty(idVProperty);

        PowerMockito.mockStatic(IdentityVerificationDataHolder.class);
        when(IdentityVerificationDataHolder.getInstance()).thenReturn(identityVerificationDataHolder);
        when(identityVerificationDataHolder.getIdVProviderManager()).thenReturn(mockIdVProviderManager);
        when(mockIdVProviderManager.isIdVProviderExistsByName(anyString(), anyInt())).thenReturn(true);
        when(identityVerificationDataHolder.
                getIdentityVerifierFactory(anyString())).thenReturn(identityVerifierFactory);
        when(identityVerifierFactory.getIdentityVerifier(anyString())).thenReturn(identityVerifier);
        when(identityVerifier.verifyIdentity(anyString(), any(IdentityVerifierData.class), anyInt())).
                thenReturn(identityVerifierData);

        identityVerification.verifyIdentity(userId, identityVerifierData, tenantId);
    }

    @Test
    public void testGetIdVClaim() throws Exception {

        when(identityVerificationClaimDAO.isIdVClaimExist(anyString(), anyInt())).thenReturn(true);
        when(identityVerificationClaimDAO.getIDVClaim(anyString(), anyString(), anyInt())).
                thenReturn(getIdVClaim());
        PowerMockito.doNothing().when(identityVerificationClaimDAO).addIdVClaimList(any(), anyInt());

        when(identityVerificationDataHolder.getIdVProviderManager()).thenReturn(mockIdVProviderManager);
        when(mockIdVProviderManager.isIdVProviderExists(anyString(), anyInt())).thenReturn(true);

        IdVClaim idVClaim =
                identityVerification.getIdVClaim(userId, idvClaimUUID, tenantId);
        Assert.assertEquals(idVClaim.getClaimUri(), "http://wso2.org/claims/dob");
    }

    @Test
    public void testAddIdVClaims() throws Exception {

        when(identityVerificationClaimDAO.isIdVClaimDataExist(anyString(), anyString(), anyString(), anyInt())).
                thenReturn(false);
        PowerMockito.doNothing().when(identityVerificationClaimDAO).addIdVClaimList(anyList(), anyInt());

        when(identityVerificationDataHolder.getIdVProviderManager()).thenReturn(mockIdVProviderManager);
        when(mockIdVProviderManager.isIdVProviderExists(anyString(), anyInt())).thenReturn(true);

        List<IdVClaim> idVClaims = new ArrayList<>();
        idVClaims.add(getIdVClaim());

        List<IdVClaim> addedIdVClaims =
                identityVerification.addIdVClaims(userId, idVClaims, tenantId);
        Assert.assertEquals(addedIdVClaims.get(0).getClaimUri(), "http://wso2.org/claims/dob");
    }

    @Test
    public void testUpdateIdVClaims() throws Exception {

        when(identityVerificationClaimDAO.isIdVClaimExist(anyString(), anyInt())).
                thenReturn(true);
        PowerMockito.doNothing().when(identityVerificationClaimDAO).updateIdVClaim(any(IdVClaim.class), anyInt());

        IdVClaim idVClaim = getIdVClaim();

        IdVClaim updatedIdVClaim = identityVerification.updateIdVClaim(userId, idVClaim, tenantId);
        Assert.assertEquals(updatedIdVClaim.getClaimUri(), "http://wso2.org/claims/dob");
    }

    @Test
    public void testDeleteIDVClaim() throws Exception {

        PowerMockito.doNothing().when(identityVerificationClaimDAO).deleteIdVClaim(anyString(), anyString(), anyInt());

        identityVerification.deleteIDVClaim(userId, idvClaimUUID, tenantId);
    }

    @Test
    public void testGetIDVClaims() throws Exception {

        when(identityVerificationClaimDAO.isIdVClaimExist(anyString(), anyInt())).thenReturn(true);
        when(identityVerificationClaimDAO.getIDVClaims(anyString(), anyInt())).
                thenReturn(getIdVClaims());

        when(identityVerificationDataHolder.getIdVProviderManager()).thenReturn(mockIdVProviderManager);
        when(mockIdVProviderManager.isIdVProviderExists(anyString(), anyInt())).thenReturn(true);

        IdVClaim[] idVClaims =
                identityVerification.getIdVClaims(userId, tenantId);
        Assert.assertEquals(idVClaims.length, 1);
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

    private void prepareConfigs() {

        mockCarbonContextForTenant();
        mockIdentityTenantUtility();
    }

    private void mockCarbonContextForTenant() {

        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext privilegedCarbonContext = PowerMockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        PowerMockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn(
                org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        PowerMockito.when(privilegedCarbonContext.getTenantId()).thenReturn(
                org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID);
        PowerMockito.when(privilegedCarbonContext.getUsername()).thenReturn("admin");
    }

    private void mockIdentityTenantUtility() {

        PowerMockito.mockStatic(IdentityTenantUtil.class);
        PowerMockito.when(IdentityTenantUtil.getTenantDomain(any(Integer.class))).thenReturn(SUPER_TENANT_DOMAIN_NAME);
    }

    private IdVClaim[] getIdVClaims() {

        IdVClaim[] idVClaims = new IdVClaim[1];
        idVClaims[0] = getIdVClaim();
        return idVClaims;
    }

    private IdVClaim getIdVClaim() {

        IdVClaim idVClaim = new IdVClaim();
        idVClaim.setUuid(idvClaimUUID);
        idVClaim.setUserId(userId);
        idVClaim.setClaimUri("http://wso2.org/claims/dob");
        idVClaim.setStatus(true);
        idVClaim.setIdVPId(idvProviderId);
        idVClaim.setMetadata(new JSONObject("{\n" +
                "      \"source\": \"evidentID\",\n" +
                "      \"trackingId\": \"123e4567-e89b-12d3-a456-556642440000\"\n" +
                "    }"));
        return idVClaim;
    }
}