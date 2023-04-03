package org.wso2.carbon.extension.identity.verification.provider.dao;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.extension.identity.verification.provider.IdVPSecretProcessor;
import org.wso2.carbon.extension.identity.verification.provider.internal.IdVProviderDataHolder;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVConfigProperty;
import org.wso2.carbon.extension.identity.verification.provider.model.IdentityVerificationProvider;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
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
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;

@PrepareForTest({PrivilegedCarbonContext.class, IdentityDatabaseUtil.class, IdentityUtil.class,
        IdentityTenantUtil.class, IdVPSecretProcessor.class})
public class IdVProviderDAOImplTest {

    private IdVProviderDAO idVProviderDAO;
    private static Map<String, BasicDataSource> dataSourceMap = new HashMap<>();
    private static final String DB_NAME = "test";

    @BeforeMethod
    public void setUp() throws Exception {

        idVProviderDAO = new IdVProviderDAOImpl();
        IdVProviderDataHolder.getInstance().
                setIdVProviderDAOs(Collections.singletonList(idVProviderDAO));
//        initiateH2Database(DB_NAME, getFilePath("h2.sql"));
        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());
        prepareConfigs();
    }

//    @AfterMethod
//    public void tearDown() throws Exception {
//
//        closeH2Database();
//    }


    @Test(priority = 1)
    public void testAddIdVProvider() throws Exception {

        initiateH2Database(DB_NAME, getFilePath("h2.sql"));
        mockStatic(IdentityDatabaseUtil.class);
        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            IdentityVerificationProvider identityVerificationProvider = addTestIdVProvider();

            IdVPSecretProcessor idVPSecretProcessor = mock(IdVPSecretProcessor.class);
            SecretManagerImpl secretManager = mock(SecretManagerImpl.class);
            SecretResolveManagerImpl secretResolveManager = mock(SecretResolveManagerImpl.class);
            PowerMockito.whenNew(IdVPSecretProcessor.class).withNoArguments().thenReturn(idVPSecretProcessor);
            PowerMockito.whenNew(SecretManagerImpl.class).withNoArguments().thenReturn(secretManager);
            PowerMockito.whenNew(SecretResolveManagerImpl.class).withNoArguments().thenReturn(secretResolveManager);
            PowerMockito.doReturn(false).when(secretManager).isSecretExist(anyString(), anyString());
            Secret secret = new Secret();
            PowerMockito.doReturn(secret).when(secretManager).addSecret(anyString(), any(Secret.class));
            SecretType secretType = new SecretType();
            secretType.setId("1234");
            PowerMockito.doReturn(secretType).when(secretManager).getSecretType(anyString());
            idVProviderDAO.addIdVProvider(identityVerificationProvider, -1234);
            Assert.assertEquals(identityVerificationProvider.getIdVProviderName(), "ONFIDO");
        }
    }

    @Test(priority = 2)
    public void testGetIdVProvider() throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        SecretManagerImpl secretManager = mock(SecretManagerImpl.class);

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));

            IdVPSecretProcessor idVPSecretProcessor = mock(IdVPSecretProcessor.class);

            SecretResolveManagerImpl secretResolveManager = mock(SecretResolveManagerImpl.class);
            PowerMockito.whenNew(IdVPSecretProcessor.class).withNoArguments().thenReturn(idVPSecretProcessor);
            PowerMockito.whenNew(SecretManagerImpl.class).withNoArguments().thenReturn(secretManager);
            PowerMockito.whenNew(SecretResolveManagerImpl.class).withNoArguments().thenReturn(secretResolveManager);
            PowerMockito.doReturn(true).when(secretManager).isSecretExist(anyString(), anyString());
            Secret secret = new Secret();
            PowerMockito.doReturn(secret).when(secretManager).addSecret(anyString(), any(Secret.class));
            SecretType secretType = new SecretType();
            secretType.setId("1234");
            PowerMockito.doReturn(secretType).when(secretManager).getSecretType(anyString());

            ResolvedSecret resolvedSecret = new ResolvedSecret();
            resolvedSecret.setResolvedSecretValue("1234-5678-91234-654246");
            PowerMockito.doReturn(resolvedSecret).when(secretResolveManager).
                    getResolvedSecret(anyString(), anyString());

            IdentityVerificationProvider idVProvider =
                    idVProviderDAO.getIdVProvider("1c7ce08b-2ebc-4b9e-a107-3b129c019954", -1234);
            Assert.assertEquals(idVProvider.getIdVProviderName(), "ONFIDO");
        }
    }

    @Test(priority = 3)
    public void testGetIdVProviders() throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        SecretManagerImpl secretManager = mock(SecretManagerImpl.class);

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));

            IdVPSecretProcessor idVPSecretProcessor = mock(IdVPSecretProcessor.class);

            SecretResolveManagerImpl secretResolveManager = mock(SecretResolveManagerImpl.class);
            PowerMockito.whenNew(IdVPSecretProcessor.class).withNoArguments().thenReturn(idVPSecretProcessor);
            PowerMockito.whenNew(SecretManagerImpl.class).withNoArguments().thenReturn(secretManager);
            PowerMockito.whenNew(SecretResolveManagerImpl.class).withNoArguments().thenReturn(secretResolveManager);
            PowerMockito.doReturn(true).when(secretManager).isSecretExist(anyString(), anyString());
            Secret secret = new Secret();
            PowerMockito.doReturn(secret).when(secretManager).addSecret(anyString(), any(Secret.class));
            SecretType secretType = new SecretType();
            secretType.setId("1234");
            PowerMockito.doReturn(secretType).when(secretManager).getSecretType(anyString());

            ResolvedSecret resolvedSecret = new ResolvedSecret();
            resolvedSecret.setResolvedSecretValue("1234-5678-91234-654246");
            PowerMockito.doReturn(resolvedSecret).when(secretResolveManager).
                    getResolvedSecret(anyString(), anyString());

            List<IdentityVerificationProvider> identityVerificationProviders =
                    idVProviderDAO.getIdVProviders(2, 0, -1234);
            Assert.assertEquals(identityVerificationProviders.size(), 1);
        }
    }

    @Test(priority = 4)
    public void testGetIdVPByName() throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        SecretManagerImpl secretManager = mock(SecretManagerImpl.class);

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));

            IdVPSecretProcessor idVPSecretProcessor = mock(IdVPSecretProcessor.class);

            SecretResolveManagerImpl secretResolveManager = mock(SecretResolveManagerImpl.class);
            PowerMockito.whenNew(IdVPSecretProcessor.class).withNoArguments().thenReturn(idVPSecretProcessor);
            PowerMockito.whenNew(SecretManagerImpl.class).withNoArguments().thenReturn(secretManager);
            PowerMockito.whenNew(SecretResolveManagerImpl.class).withNoArguments().thenReturn(secretResolveManager);
            PowerMockito.doReturn(true).when(secretManager).isSecretExist(anyString(), anyString());
            Secret secret = new Secret();
            PowerMockito.doReturn(secret).when(secretManager).addSecret(anyString(), any(Secret.class));
            SecretType secretType = new SecretType();
            secretType.setId("1234");
            PowerMockito.doReturn(secretType).when(secretManager).getSecretType(anyString());

            ResolvedSecret resolvedSecret = new ResolvedSecret();
            resolvedSecret.setResolvedSecretValue("1234-5678-91234-654246");
            PowerMockito.doReturn(resolvedSecret).when(secretResolveManager).
                    getResolvedSecret(anyString(), anyString());

            IdentityVerificationProvider identityVerificationProvider =
                    idVProviderDAO.getIdVPByName("ONFIDO",  -1234);
            Assert.assertEquals(identityVerificationProvider.getIdVProviderUuid(),
                    "1c7ce08b-2ebc-4b9e-a107-3b129c019954");
        }
    }

    @Test(priority = 5)
    public void testGetCountOfIdVProviders() throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        SecretManagerImpl secretManager = mock(SecretManagerImpl.class);

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));

            IdVPSecretProcessor idVPSecretProcessor = mock(IdVPSecretProcessor.class);

            SecretResolveManagerImpl secretResolveManager = mock(SecretResolveManagerImpl.class);
            PowerMockito.whenNew(IdVPSecretProcessor.class).withNoArguments().thenReturn(idVPSecretProcessor);
            PowerMockito.whenNew(SecretManagerImpl.class).withNoArguments().thenReturn(secretManager);
            PowerMockito.whenNew(SecretResolveManagerImpl.class).withNoArguments().thenReturn(secretResolveManager);
            PowerMockito.doReturn(true).when(secretManager).isSecretExist(anyString(), anyString());
            Secret secret = new Secret();
            PowerMockito.doReturn(secret).when(secretManager).addSecret(anyString(), any(Secret.class));
            SecretType secretType = new SecretType();
            secretType.setId("1234");
            PowerMockito.doReturn(secretType).when(secretManager).getSecretType(anyString());

            ResolvedSecret resolvedSecret = new ResolvedSecret();
            resolvedSecret.setResolvedSecretValue("1234-5678-91234-654246");
            PowerMockito.doReturn(resolvedSecret).when(secretResolveManager).
                    getResolvedSecret(anyString(), anyString());

            int countOfIdVProviders = idVProviderDAO.getCountOfIdVProviders(-1234);
            Assert.assertEquals(countOfIdVProviders, 1);
        }
    }

    @Test(priority = 6)
    public void testIsIdVProviderExists() throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        SecretManagerImpl secretManager = mock(SecretManagerImpl.class);

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));

            IdVPSecretProcessor idVPSecretProcessor = mock(IdVPSecretProcessor.class);

            SecretResolveManagerImpl secretResolveManager = mock(SecretResolveManagerImpl.class);
            PowerMockito.whenNew(IdVPSecretProcessor.class).withNoArguments().thenReturn(idVPSecretProcessor);
            PowerMockito.whenNew(SecretManagerImpl.class).withNoArguments().thenReturn(secretManager);
            PowerMockito.whenNew(SecretResolveManagerImpl.class).withNoArguments().thenReturn(secretResolveManager);
            PowerMockito.doReturn(true).when(secretManager).isSecretExist(anyString(), anyString());
            Secret secret = new Secret();
            PowerMockito.doReturn(secret).when(secretManager).addSecret(anyString(), any(Secret.class));
            SecretType secretType = new SecretType();
            secretType.setId("1234");
            PowerMockito.doReturn(secretType).when(secretManager).getSecretType(anyString());

            ResolvedSecret resolvedSecret = new ResolvedSecret();
            resolvedSecret.setResolvedSecretValue("1234-5678-91234-654246");
            PowerMockito.doReturn(resolvedSecret).when(secretResolveManager).
                    getResolvedSecret(anyString(), anyString());

            boolean isIdVProviderExists =
                    idVProviderDAO.isIdVProviderExists("1c7ce08b-2ebc-4b9e-a107-3b129c019954", -1234);
            Assert.assertTrue(isIdVProviderExists);
        }
    }

    @Test(priority = 7)
    public void testUpdateIdVProviderExists() throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            IdentityVerificationProvider idVProvider = getOldIdVProvider();
            IdentityVerificationProvider updatedIdVProvider = addTestIdVProvider();

            IdVPSecretProcessor idVPSecretProcessor = mock(IdVPSecretProcessor.class);
            SecretManagerImpl secretManager = mock(SecretManagerImpl.class);
            SecretResolveManagerImpl secretResolveManager = mock(SecretResolveManagerImpl.class);
            PowerMockito.whenNew(IdVPSecretProcessor.class).withNoArguments().thenReturn(idVPSecretProcessor);
            PowerMockito.whenNew(SecretManagerImpl.class).withNoArguments().thenReturn(secretManager);
            PowerMockito.whenNew(SecretResolveManagerImpl.class).withNoArguments().thenReturn(secretResolveManager);
            PowerMockito.doReturn(false).when(secretManager).isSecretExist(anyString(), anyString());
            Secret secret = new Secret();
            PowerMockito.doReturn(secret).when(secretManager).addSecret(anyString(), any(Secret.class));
            SecretType secretType = new SecretType();
            secretType.setId("1234");
            PowerMockito.doReturn(secretType).when(secretManager).getSecretType(anyString());
            idVProviderDAO.updateIdVProvider(idVProvider, updatedIdVProvider, -1234);
//            Assert.assertEquals(identityVerificationProvider.getIdVProviderName(), "ONFIDO");
        }
    }

    @Test(priority = 8)
    public void testDeleteIdVProvider() throws Exception {

        mockStatic(IdentityDatabaseUtil.class);
        SecretManagerImpl secretManager = mock(SecretManagerImpl.class);
        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));

            IdVPSecretProcessor idVPSecretProcessor = mock(IdVPSecretProcessor.class);

            SecretResolveManagerImpl secretResolveManager = mock(SecretResolveManagerImpl.class);
            PowerMockito.whenNew(IdVPSecretProcessor.class).withNoArguments().thenReturn(idVPSecretProcessor);
            PowerMockito.whenNew(SecretManagerImpl.class).withNoArguments().thenReturn(secretManager);
            PowerMockito.whenNew(SecretResolveManagerImpl.class).withNoArguments().thenReturn(secretResolveManager);
            PowerMockito.doReturn(true).when(secretManager).isSecretExist(anyString(), anyString());
            Secret secret = new Secret();
            PowerMockito.doReturn(secret).when(secretManager).addSecret(anyString(), any(Secret.class));
            SecretType secretType = new SecretType();
            secretType.setId("1234");
            PowerMockito.doReturn(secretType).when(secretManager).getSecretType(anyString());

            ResolvedSecret resolvedSecret = new ResolvedSecret();
            resolvedSecret.setResolvedSecretValue("1234-5678-91234-654246");
            PowerMockito.doReturn(resolvedSecret).when(secretResolveManager).
                    getResolvedSecret(anyString(), anyString());

            idVProviderDAO.deleteIdVProvider("1c7ce08b-2ebc-4b9e-a107-3b129c019954", -1234);
//            Assert.assertNotNull(countOfIdVProviders, 1);
        }
        closeH2Database();
    }

    @Test
    public void testGetPriority() {

        int priority = idVProviderDAO.getPriority();
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

    private IdentityVerificationProvider getOldIdVProvider() {

        IdentityVerificationProvider idVProvider = new IdentityVerificationProvider();
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