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
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.extension.identity.verification.provider.dao.IdVProviderDAO;
import org.wso2.carbon.extension.identity.verification.provider.dao.IdVProviderDAOImpl;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtException;
import org.wso2.carbon.extension.identity.verification.provider.internal.IdVProviderDataHolder;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVConfigProperty;
import org.wso2.carbon.extension.identity.verification.provider.model.IdentityVerificationProvider;
import org.wso2.carbon.extension.identity.verification.provider.util.TestUtils;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.secret.mgt.core.IdPSecretsProcessor;
import org.wso2.carbon.identity.secret.mgt.core.SecretManager;
import org.wso2.carbon.identity.secret.mgt.core.SecretManagerImpl;
import org.wso2.carbon.identity.secret.mgt.core.SecretResolveManagerImpl;
import org.wso2.carbon.identity.secret.mgt.core.SecretsProcessor;
import org.wso2.carbon.identity.secret.mgt.core.dao.SecretDAO;
import org.wso2.carbon.identity.secret.mgt.core.dao.impl.SecretDAOImpl;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.internal.SecretManagerComponentDataHolder;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.closeH2Base;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.getConnection;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.initiateH2Base;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.spyConnection;

/**
 * Test class for IdVProviderManagerImpl.
 */
@PrepareForTest({PrivilegedCarbonContext.class, IdentityDatabaseUtil.class, IdentityUtil.class,
        IdentityTenantUtil.class, CryptoUtil.class, IdVPSecretProcessor.class, IdentityVerificationProvider.class})
public class IdVProviderManagerImplTest extends PowerMockTestCase {

    private Connection connection;
    private DataSource dataSource;
    private IdVProviderManager idVProviderManager;
    private IdVPSecretProcessor idVPSecretProcessor;

    @Mock
    private SecretManager secretManager;
    @Mock
    private IdVProviderDAOImpl idVProviderDAOImpl;

    @BeforeMethod
    public void setUp() throws Exception {

        initiateH2Base();
        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());

        connection = getConnection();
        mockStatic(IdentityDatabaseUtil.class);
        when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);

//        mockStatic(IdVPSecretProcessor.class);
//        idVPSecretProcessor = mock(IdVPSecretProcessor.class);
//        when(idVPSecretProcessor.getTenantDomain(any(Integer.class))).thenReturn(SUPER_TENANT_DOMAIN_NAME);

//        IdVPSecretProcessor idVPSecretProcessor = Mockito.mock(IdVPSecretProcessor.class);
//        PowerMockito.whenNew(IdVPSecretProcessor.class).withNoArguments().thenReturn(
//                idVPSecretProcessor);
//        when(idVPSecretProcessor.encryptAssociatedSecrets(anyObject())).thenAnswer(
//                invocation -> invocation.getArguments()[0]);
//        when(idVPSecretProcessor.encryptAssociatedSecrets(any(IdentityVerificationProvider.class))).
//                thenReturn(idvProvider);
//
//        connection = getConnection();
//        Connection spyConnection = spyConnection(connection);
//        when(dataSource.getConnection(anyBoolean())).thenReturn(spyConnection);

        prepareConfigs();
    }

    @AfterMethod
    public void tearDown() throws Exception {

        connection.close();
        closeH2Base();
    }

//    @Test
//    public void testGetIdVProvider() throws IdVProviderMgtException {
//
//        addTestIdVProvider();
//        IdentityVerificationProvider result =
//                idVProviderManager.getIdVProvider("1c7ce08b-2ebc-4b9e-a107-3b129c019954", 1);
//        Assert.assertEquals(result.getIdVProviderUuid(), "1c7ce08b-2ebc-4b9e-a107-3b129c019954");
//    }

//    private static void assertIdentityProvider(String idVProviderId, IdentityVerificationProvider result) {
//
//        assertEquals(result.getIdVProviderUuid(), idVProviderId);
//        assertEquals(result.getIdVProviderName(), "ONFIDO");
//        assertEquals(result.getIdVProviderDescription(), "ONFIDO identity verification provider");
//        assertEquals(result.getClaimMappings().get("http://wso2.org/claims/givenname"), "firstName");
//        assertEquals(result.getClaimMappings().get("http://wso2.org/claims/lastname"), "lastName");
//        assertEquals(result.getIdVConfigProperties().length, 2);
//        assertEquals(result.getIdVConfigProperties()[0].getName(), "token");
//        assertEquals(result.getIdVConfigProperties()[0].getValue(), "1234-5678-91234-654246");
//        Assert.assertTrue(result.getIdVConfigProperties()[0].isConfidential());
//        assertEquals(result.getIdVConfigProperties()[1].getName(), "apiUrl");
//        assertEquals(result.getIdVConfigProperties()[1].getValue(), "https://api.test.com/v1/");
//    }
//
    @Test
    public void testAddIdVProvider() throws Exception {


        IdentityVerificationProvider idvProvider = addTestIdVProvider();

        IdVPSecretProcessor mockSecretProcessor = mock(IdVPSecretProcessor.class);
        when(mockSecretProcessor.encryptAssociatedSecrets(idvProvider)).thenReturn(idvProvider);

//        when(secretManager.isSecretExist(anyString(), anyString())).thenReturn(false);
//        when(idVPSecretProcessor.encryptAssociatedSecrets(any(IdentityVerificationProvider.class))).
//                thenReturn(idvProvider);

//        when(idVProviderManager.getIdVProvider(anyString(), anyInt())).thenReturn(idvProvider);

//        when(idVPSecretProcessor.encryptAssociatedSecrets(any(IdentityVerificationProvider.class))).
//                thenAnswer(invocation -> invocation.getArguments()[0]);
        IdentityVerificationProvider result =
                idVProviderManager.addIdVProvider(idvProvider, 1);
        Assert.assertEquals(result.getIdVProviderName(), "ONFIDO");
    }
//        try (Connection connection = getConnection()) {
//            Connection spyConnection = spyConnection(connection);
//            when(dataSource.getConnection()).thenReturn(spyConnection);
//
//            IdentityVerificationProvider idvProvider = addTestIdVProvider();
//            IdVProviderManager idVProviderManager = new IdVProviderManagerImpl();
//            IdentityVerificationProvider identityVerificationProvider =
//                    idVProviderManager.addIdVProvider(idvProvider, -1234);
//
//            Assert.assertEquals(identityVerificationProvider.getIdVProviderName(), "ONFIDO");
//        }
//    }
//
//    @Test
//    public void testGetCountOfIdVProviders() {
//
//    }
//
//    @Test
//    public void testDeleteIdVProvider() {
//
//    }
//
//    @Test
//    public void testUpdateIdVProvider() {
//
//    }
//
//    @Test
//    public void testGetIdVProviders() {
//
//    }
//
//    @Test
//    public void testIsIdVProviderExists() {
//
//    }
//
//    @Test
//    public void testGetIdVPByName() {
//
//    }

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

    private void prepareConfigs() {

        IdVProviderDAO idVProviderDAO = new IdVProviderDAOImpl();
        IdVProviderDataHolder.getInstance().setIdVProviderDAOs(Collections.singletonList(idVProviderDAO));
        mockCarbonContextForTenant(SUPER_TENANT_ID, SUPER_TENANT_DOMAIN_NAME);
        mockIdentityTenantUtility();
        idVProviderManager = new IdVProviderManagerImpl();
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
