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

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.extension.identity.verification.provider.IdVPSecretProcessor;
import org.wso2.carbon.extension.identity.verification.provider.internal.IdVProviderDataHolder;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVConfigProperty;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.secret.mgt.core.SecretManagerImpl;
import org.wso2.carbon.identity.secret.mgt.core.SecretResolveManagerImpl;
import org.wso2.carbon.identity.secret.mgt.core.model.ResolvedSecret;
import org.wso2.carbon.identity.secret.mgt.core.model.Secret;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.addTestIdVProvider;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.idVProviderId;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.idVProviderName;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.tenantId;

/**
 * Test class for IdVProviderDAOImpl.
 */
@PrepareForTest({IdentityDatabaseUtil.class, IdVPSecretProcessor.class})
public class IdVProviderDAOImplTest {

    private IdVProviderDAO idVProviderDAO;
    private SecretManagerImpl secretManager;
    private static final Map<String, BasicDataSource> dataSourceMap = new HashMap<>();
    private static final String DB_NAME = "test";

    @BeforeClass
    public void init() throws Exception {

        initiateH2Database(DB_NAME, getFilePath());
    }

    @BeforeMethod
    public void setUp() {

        idVProviderDAO = new IdVProviderDAOImpl();
        IdVProviderDataHolder.getInstance().
                setIdVProviderDAOs(Collections.singletonList(idVProviderDAO));

        mockStatic(IdentityDatabaseUtil.class);
        secretManager = mock(SecretManagerImpl.class);
    }

    @AfterClass
    public void tearDown() throws Exception {

        closeH2Database();
    }

    @Test(priority = 1)
    public void testAddIdVProvider() throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            IdVProvider identityVerificationProvider = addTestIdVProvider();

            IdVPSecretProcessor idVPSecretProcessor = mock(IdVPSecretProcessor.class);
            SecretManagerImpl secretManager = mock(SecretManagerImpl.class);
            SecretResolveManagerImpl secretResolveManager = mock(SecretResolveManagerImpl.class);
            whenNew(IdVPSecretProcessor.class).withNoArguments().thenReturn(idVPSecretProcessor);
            whenNew(SecretManagerImpl.class).withNoArguments().thenReturn(secretManager);
            whenNew(SecretResolveManagerImpl.class).withNoArguments().thenReturn(secretResolveManager);
            doReturn(false).when(secretManager).isSecretExist(anyString(), anyString());
            Secret secret = new Secret();
            doReturn(secret).when(secretManager).addSecret(anyString(), any(Secret.class));
            SecretType secretType = new SecretType();
            secretType.setId("1234");
            doReturn(secretType).when(secretManager).getSecretType(anyString());
            idVProviderDAO.addIdVProvider(identityVerificationProvider, -1234);
            Assert.assertEquals(identityVerificationProvider.getIdVProviderName(), idVProviderName);
        }
    }

    @Test(priority = 2)
    public void testGetIdVProvider() throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));

            IdVPSecretProcessor idVPSecretProcessor = mock(IdVPSecretProcessor.class);

            SecretResolveManagerImpl secretResolveManager = mock(SecretResolveManagerImpl.class);
            whenNew(IdVPSecretProcessor.class).withNoArguments().thenReturn(idVPSecretProcessor);
            whenNew(SecretManagerImpl.class).withNoArguments().thenReturn(secretManager);
            whenNew(SecretResolveManagerImpl.class).withNoArguments().thenReturn(secretResolveManager);
            doReturn(true).when(secretManager).isSecretExist(anyString(), anyString());
            Secret secret = new Secret();
            doReturn(secret).when(secretManager).addSecret(anyString(), any(Secret.class));
            SecretType secretType = new SecretType();
            secretType.setId("1234");
            doReturn(secretType).when(secretManager).getSecretType(anyString());

            ResolvedSecret resolvedSecret = new ResolvedSecret();
            resolvedSecret.setResolvedSecretValue("1234-5678-91234-654246");
            doReturn(resolvedSecret).when(secretResolveManager).
                    getResolvedSecret(anyString(), anyString());

            IdVProvider idVProvider =
                    idVProviderDAO.getIdVProvider(idVProviderId, tenantId);
            Assert.assertEquals(idVProvider.getIdVProviderName(), idVProviderName);
        }
    }

    @Test(priority = 3)
    public void testGetIdVProviders() throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));

            IdVPSecretProcessor idVPSecretProcessor = mock(IdVPSecretProcessor.class);

            SecretResolveManagerImpl secretResolveManager = mock(SecretResolveManagerImpl.class);
            whenNew(IdVPSecretProcessor.class).withNoArguments().thenReturn(idVPSecretProcessor);
            whenNew(SecretManagerImpl.class).withNoArguments().thenReturn(secretManager);
            whenNew(SecretResolveManagerImpl.class).withNoArguments().thenReturn(secretResolveManager);
            doReturn(true).when(secretManager).isSecretExist(anyString(), anyString());
            Secret secret = new Secret();
            doReturn(secret).when(secretManager).addSecret(anyString(), any(Secret.class));
            SecretType secretType = new SecretType();
            secretType.setId("1234");
            doReturn(secretType).when(secretManager).getSecretType(anyString());

            ResolvedSecret resolvedSecret = new ResolvedSecret();
            resolvedSecret.setResolvedSecretValue("1234-5678-91234-654246");
            doReturn(resolvedSecret).when(secretResolveManager).
                    getResolvedSecret(anyString(), anyString());

            List<IdVProvider> identityVerificationProviders =
                    idVProviderDAO.getIdVProviders(2, 0, -1234);
            Assert.assertEquals(identityVerificationProviders.size(), 1);
        }
    }

    @Test(priority = 4)
    public void testGetIdVPByName() throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));

            IdVPSecretProcessor idVPSecretProcessor = mock(IdVPSecretProcessor.class);

            SecretResolveManagerImpl secretResolveManager = mock(SecretResolveManagerImpl.class);
            whenNew(IdVPSecretProcessor.class).withNoArguments().thenReturn(idVPSecretProcessor);
            whenNew(SecretManagerImpl.class).withNoArguments().thenReturn(secretManager);
            whenNew(SecretResolveManagerImpl.class).withNoArguments().thenReturn(secretResolveManager);
            doReturn(true).when(secretManager).isSecretExist(anyString(), anyString());
            Secret secret = new Secret();
            doReturn(secret).when(secretManager).addSecret(anyString(), any(Secret.class));
            SecretType secretType = new SecretType();
            secretType.setId("1234");
            doReturn(secretType).when(secretManager).getSecretType(anyString());

            ResolvedSecret resolvedSecret = new ResolvedSecret();
            resolvedSecret.setResolvedSecretValue("1234-5678-91234-654246");
            doReturn(resolvedSecret).when(secretResolveManager).
                    getResolvedSecret(anyString(), anyString());

            IdVProvider identityVerificationProvider =
                    idVProviderDAO.getIdVProviderByName(idVProviderName,  -1234);
            Assert.assertEquals(identityVerificationProvider.getIdVProviderUuid(),
                    "1c7ce08b-2ebc-4b9e-a107-3b129c019954");
        }
    }

    @Test(priority = 5)
    public void testGetCountOfIdVProviders() throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));

            IdVPSecretProcessor idVPSecretProcessor = mock(IdVPSecretProcessor.class);

            SecretResolveManagerImpl secretResolveManager = mock(SecretResolveManagerImpl.class);
            whenNew(IdVPSecretProcessor.class).withNoArguments().thenReturn(idVPSecretProcessor);
            whenNew(SecretManagerImpl.class).withNoArguments().thenReturn(secretManager);
            whenNew(SecretResolveManagerImpl.class).withNoArguments().thenReturn(secretResolveManager);
            doReturn(true).when(secretManager).isSecretExist(anyString(), anyString());
            Secret secret = new Secret();
            doReturn(secret).when(secretManager).addSecret(anyString(), any(Secret.class));
            SecretType secretType = new SecretType();
            secretType.setId("1234");
            doReturn(secretType).when(secretManager).getSecretType(anyString());

            ResolvedSecret resolvedSecret = new ResolvedSecret();
            resolvedSecret.setResolvedSecretValue("1234-5678-91234-654246");
            doReturn(resolvedSecret).when(secretResolveManager).
                    getResolvedSecret(anyString(), anyString());

            int countOfIdVProviders = idVProviderDAO.getCountOfIdVProviders(tenantId);
            Assert.assertEquals(countOfIdVProviders, 1);
        }
    }

    @Test(priority = 6)
    public void testIsIdVProviderExists() throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));

            IdVPSecretProcessor idVPSecretProcessor = mock(IdVPSecretProcessor.class);

            SecretResolveManagerImpl secretResolveManager = mock(SecretResolveManagerImpl.class);
            whenNew(IdVPSecretProcessor.class).withNoArguments().thenReturn(idVPSecretProcessor);
            whenNew(SecretManagerImpl.class).withNoArguments().thenReturn(secretManager);
            whenNew(SecretResolveManagerImpl.class).withNoArguments().thenReturn(secretResolveManager);
            doReturn(true).when(secretManager).isSecretExist(anyString(), anyString());
            Secret secret = new Secret();
            doReturn(secret).when(secretManager).addSecret(anyString(), any(Secret.class));
            SecretType secretType = new SecretType();
            secretType.setId("1234");
            doReturn(secretType).when(secretManager).getSecretType(anyString());

            ResolvedSecret resolvedSecret = new ResolvedSecret();
            resolvedSecret.setResolvedSecretValue("1234-5678-91234-654246");
            doReturn(resolvedSecret).when(secretResolveManager).
                    getResolvedSecret(anyString(), anyString());

            boolean isIdVProviderExists = idVProviderDAO.isIdVProviderExists(idVProviderId, tenantId);
            Assert.assertTrue(isIdVProviderExists);
        }
    }

    @Test(priority = 7)
    public void testUpdateIdVProviderExists() throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            IdVProvider idVProvider = getOldIdVProvider();
            IdVProvider updatedIdVProvider = addTestIdVProvider();

            IdVPSecretProcessor idVPSecretProcessor = mock(IdVPSecretProcessor.class);
            SecretManagerImpl secretManager = mock(SecretManagerImpl.class);
            SecretResolveManagerImpl secretResolveManager = mock(SecretResolveManagerImpl.class);
            whenNew(IdVPSecretProcessor.class).withNoArguments().thenReturn(idVPSecretProcessor);
            whenNew(SecretManagerImpl.class).withNoArguments().thenReturn(secretManager);
            whenNew(SecretResolveManagerImpl.class).withNoArguments().thenReturn(secretResolveManager);
            doReturn(false).when(secretManager).isSecretExist(anyString(), anyString());
            Secret secret = new Secret();
            doReturn(secret).when(secretManager).addSecret(anyString(), any(Secret.class));
            SecretType secretType = new SecretType();
            secretType.setId("1234");
            doReturn(secretType).when(secretManager).getSecretType(anyString());
            idVProviderDAO.updateIdVProvider(idVProvider, updatedIdVProvider, tenantId);
        }
    }

    @Test(priority = 8)
    public void testDeleteIdVProvider() throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));

            IdVPSecretProcessor idVPSecretProcessor = mock(IdVPSecretProcessor.class);

            SecretResolveManagerImpl secretResolveManager = mock(SecretResolveManagerImpl.class);
            whenNew(IdVPSecretProcessor.class).withNoArguments().thenReturn(idVPSecretProcessor);
            whenNew(SecretManagerImpl.class).withNoArguments().thenReturn(secretManager);
            whenNew(SecretResolveManagerImpl.class).withNoArguments().thenReturn(secretResolveManager);
            doReturn(true).when(secretManager).isSecretExist(anyString(), anyString());
            Secret secret = new Secret();
            doReturn(secret).when(secretManager).addSecret(anyString(), any(Secret.class));
            SecretType secretType = new SecretType();
            secretType.setId("1234");
            doReturn(secretType).when(secretManager).getSecretType(anyString());

            ResolvedSecret resolvedSecret = new ResolvedSecret();
            resolvedSecret.setResolvedSecretValue("1234-5678-91234-654246");
            doReturn(resolvedSecret).when(secretResolveManager).
                    getResolvedSecret(anyString(), anyString());

            idVProviderDAO.deleteIdVProvider(idVProviderId, tenantId);
        }
    }

    @Test
    public void testGetPriority() {

        int priority = idVProviderDAO.getPriority();
        Assert.assertEquals(priority, 1);
    }

    public static void closeH2Database() throws Exception {

        BasicDataSource dataSource = dataSourceMap.get(DB_NAME);
        if (dataSource != null) {
            dataSource.close();
        }
    }

    private static String getFilePath() {

        if (StringUtils.isNotBlank("h2.sql")) {
            return Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "dbscripts", "h2.sql")
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

    private IdVProvider getOldIdVProvider() {

        IdVProvider idVProvider = new IdVProvider();
        idVProvider.setId("1");
        idVProvider.setIdVProviderName("ONFIDO");
        idVProvider.setIdVProviderDescription("ONFIDO updated description");

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
}