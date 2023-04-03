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

import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.extension.identity.verification.provider.dao.IdVProviderDAO;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtClientException;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtException;
import org.wso2.carbon.extension.identity.verification.provider.internal.IdVProviderDataHolder;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVConfigProperty;
import org.wso2.carbon.extension.identity.verification.provider.model.IdentityVerificationProvider;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementClientException;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;

/**
 * Test class for IdVProviderManagerImpl.
 */
@PrepareForTest({PrivilegedCarbonContext.class, IdentityDatabaseUtil.class, IdentityUtil.class,
        IdentityTenantUtil.class, IdVProviderDataHolder.class, IdVPSecretProcessor.class, IdentityVerificationProvider.class})
public class IdVProviderManagerImplTest extends PowerMockTestCase {

    private IdVProviderManager idVProviderManager;

    @Mock
    private IdVProviderDataHolder idVProviderDataHolder;
    @Mock
    private IdVProviderDAO idVProviderDAO;

    @BeforeMethod
    public void setUp() throws Exception {

        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());
        prepareConfigs();
    }

    @Test
    public void testGetIdVProvider() throws IdVProviderMgtException {

        mockIdVProviderDAO();
        IdentityVerificationProvider idvProvider = addTestIdVProvider();
        idVProviderManager = new IdVProviderManagerImpl();
        PowerMockito.doReturn(idvProvider).when(idVProviderDAO).getIdVProvider(anyString(), anyInt());
        IdentityVerificationProvider identityVerificationProvider =
                idVProviderManager.getIdVProvider("1c7ce08b-2ebc-4b9e-a107-3b129c019954", 1);
        Assert.assertEquals(identityVerificationProvider.getIdVProviderUuid(),
                "1c7ce08b-2ebc-4b9e-a107-3b129c019954");
    }

    @Test(expectedExceptions = IdVProviderMgtClientException.class)
    public void testGetIdVProviderEmptyIdVProviderID() {

        try {
            idVProviderManager.getIdVProvider(null, 1);
        } catch (IdVProviderMgtException e) {
            Assert.assertEquals(e.getErrorCode(), "IdVProvider ID cannot be empty.");
        }
    }

    @Test
    public void testAddIdVProvider() throws Exception {

        mockIdVProviderDAO();
        IdentityVerificationProvider idvProvider = addTestIdVProvider();
        idVProviderManager = new IdVProviderManagerImpl();

        PowerMockito.doNothing().when(idVProviderDAO).
                addIdVProvider(any(IdentityVerificationProvider.class), anyInt());
        IdentityVerificationProvider identityVerificationProvider =
                idVProviderManager.addIdVProvider(idvProvider, -1234);
        Assert.assertEquals(identityVerificationProvider.getIdVProviderName(), "ONFIDO");

    }

    @Test
    public void testGetCountOfIdVProviders() throws IdVProviderMgtException {

        mockIdVProviderDAO();
        idVProviderManager = new IdVProviderManagerImpl();

        PowerMockito.doReturn(5).when(idVProviderDAO).getCountOfIdVProviders(anyInt());
        int countOfIdVProviders = idVProviderManager.getCountOfIdVProviders(-1234);
        Assert.assertEquals(countOfIdVProviders, 5);
    }

    @Test
    public void testDeleteIdVProvider() throws IdVProviderMgtException {

        mockIdVProviderDAO();
        idVProviderManager = new IdVProviderManagerImpl();
        PowerMockito.doNothing().when(idVProviderDAO).deleteIdVProvider(anyString(), anyInt());
        idVProviderManager.deleteIdVProvider("1c7ce08b-2ebc-4b9e-a107-3b129c019954", 1);

    }

    @Test
    public void testUpdateIdVProvider() throws IdVProviderMgtException {

        mockIdVProviderDAO();
        IdentityVerificationProvider idvProvider = addTestIdVProvider();
        idVProviderManager = new IdVProviderManagerImpl();
        PowerMockito.doNothing().when(idVProviderDAO).
                updateIdVProvider(any(IdentityVerificationProvider.class),
                        any(IdentityVerificationProvider.class), anyInt());
        IdentityVerificationProvider idvProviderList =
                idVProviderManager.updateIdVProvider(idvProvider, idvProvider, 1);
        Assert.assertEquals(idvProviderList.getIdVProviderDescription(),
                "ONFIDO identity verification provider");
    }

    @Test
    public void testGetIdVProviders() throws IdVProviderMgtException {

        mockIdVProviderDAO();
        List<IdentityVerificationProvider> idvProviders = new ArrayList<>();
        idvProviders.add(addTestIdVProvider());
        idVProviderManager = new IdVProviderManagerImpl();
        PowerMockito.doReturn(idvProviders).when(idVProviderDAO).
                getIdVProviders(anyInt(), anyInt(), anyInt());
        List<IdentityVerificationProvider> idvProviderList =
                idVProviderManager.getIdVProviders(2, 1, 1);
        Assert.assertEquals(idvProviderList.size(), 1);
    }

    @Test
    public void testIsIdVProviderExists() throws IdVProviderMgtException {

        mockIdVProviderDAO();
        idVProviderManager = new IdVProviderManagerImpl();
        PowerMockito.doReturn(true).when(idVProviderDAO).isIdVProviderExists(anyString(), anyInt());
        boolean idVProviderExists =
                idVProviderManager.isIdVProviderExists("1c7ce08b-2ebc-4b9e-a107-3b129c019954", 1);
        Assert.assertTrue(idVProviderExists);
    }

    @Test
    public void testGetIdVPByName() throws IdVProviderMgtException {

        mockIdVProviderDAO();
        IdentityVerificationProvider idvProvider = addTestIdVProvider();
        idVProviderManager = new IdVProviderManagerImpl();
        PowerMockito.doReturn(idvProvider).when(idVProviderDAO).
                getIdVPByName(anyString(), anyInt());
        IdentityVerificationProvider identityVerificationProvider =
                idVProviderManager.getIdVPByName("ONFIDO", 1);
        Assert.assertNotNull(identityVerificationProvider);
        Assert.assertEquals(identityVerificationProvider.getIdVProviderName(), "ONFIDO");
    }

    private IdentityVerificationProvider addTestIdVProvider() {

        IdentityVerificationProvider idVProvider = new IdentityVerificationProvider();
        idVProvider.setIdVPUUID("1c7ce08b-2ebc-4b9e-a107-3b129c019954");
        idVProvider.setIdVProviderName("ONFIDO");
        idVProvider.setIdVProviderDescription("ONFIDO identity verification provider");

        Map<String, String> claimMappings = new HashMap<>();
        claimMappings.put("http://wso2.org/claims/givenname", "firstName");
        claimMappings.put("http://wso2.org/claims/lastname", "lastName");
        idVProvider.setClaimMappings(claimMappings);

        IdVConfigProperty[] idVConfigProperties = new IdVConfigProperty[2];
        IdVConfigProperty idVConfigProperty1 = new IdVConfigProperty();
        idVConfigProperty1.setName("token");
        idVConfigProperty1.setValue("1234-5678-91234-654246");
        idVConfigProperty1.setConfidential(true);
        idVConfigProperties[0] = idVConfigProperty1;

        IdVConfigProperty idVConfigProperty2 = new IdVConfigProperty();
        idVConfigProperty2.setName("apiUrl");
        idVConfigProperty2.setValue("https://api.test.com/v1/");
        idVConfigProperty2.setConfidential(false);
        idVConfigProperties[1] = idVConfigProperty2;

        idVProvider.setIdVConfigProperties(idVConfigProperties);
        return idVProvider;
    }

    private void mockIdVProviderDAO() {

        PowerMockito.mockStatic(IdVProviderDataHolder.class);
        Mockito.when(IdVProviderDataHolder.getInstance()).thenReturn(idVProviderDataHolder);
        Mockito.when(idVProviderDataHolder.getIdVProviderDAOs()).
                thenReturn(Collections.singletonList(idVProviderDAO));
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
}
