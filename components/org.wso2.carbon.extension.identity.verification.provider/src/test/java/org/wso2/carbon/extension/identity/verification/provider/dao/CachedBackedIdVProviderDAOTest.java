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
import org.wso2.carbon.extension.identity.verification.provider.cache.IdVProviderByIdCache;
import org.wso2.carbon.extension.identity.verification.provider.cache.IdVProviderByIdCacheKey;
import org.wso2.carbon.extension.identity.verification.provider.cache.IdVProviderByNameCache;
import org.wso2.carbon.extension.identity.verification.provider.cache.IdVProviderByNameCacheKey;
import org.wso2.carbon.extension.identity.verification.provider.cache.IdVProviderCacheEntry;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtException;
import org.wso2.carbon.extension.identity.verification.provider.internal.IdVProviderDataHolder;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVConfigProperty;
import org.wso2.carbon.extension.identity.verification.provider.model.IdentityVerificationProvider;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.*;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;

@PrepareForTest({PrivilegedCarbonContext.class, IdentityTenantUtil.class, IdVProviderByIdCache.class,
        IdVProviderByNameCache.class})
public class CachedBackedIdVProviderDAOTest {

    private IdVProviderDAO idVProviderDAO;
    private IdVProviderByNameCache idVProviderByNameCache;
    private IdVProviderByIdCache idVProviderByIdCache;
    private IdVProviderDAO idVProviderDAOImpl;
    private static Map<String, BasicDataSource> dataSourceMap = new HashMap<>();
    private static final String DB_NAME = "test";

    @Test
    public void testGetPriority() {

        int priority = idVProviderDAO.getPriority();
        Assert.assertEquals(priority, 2);
    }

    @BeforeMethod
    public void setUp() throws Exception {

        idVProviderDAOImpl = new IdVProviderDAOImpl();

        mockStatic(IdVProviderByIdCache.class);
        idVProviderByIdCache = mock(IdVProviderByIdCache.class);
        when(IdVProviderByIdCache.getInstance()).thenReturn(idVProviderByIdCache);

        mockStatic(IdVProviderByNameCache.class);
        idVProviderByNameCache = mock(IdVProviderByNameCache.class);
        when(IdVProviderByNameCache.getInstance()).thenReturn(idVProviderByNameCache);

        idVProviderDAO = new CachedBackedIdVProviderDAO(idVProviderDAOImpl);
        IdVProviderDataHolder.getInstance().
                setIdVProviderDAOs(Collections.singletonList(idVProviderDAO));
//        initiateH2Database(DB_NAME, getFilePath("h2.sql"));
        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());
        prepareConfigs();
    }

    @Test
    public void testGetIdVProvider() throws Exception {

        IdentityVerificationProvider identityVerificationProvider = addTestIdVProvider();
        IdVProviderByIdCacheKey idVProviderByIdCacheKey = mock(IdVProviderByIdCacheKey.class);
        IdVProviderCacheEntry idVProviderCacheEntry = mock(IdVProviderCacheEntry.class);
        PowerMockito.whenNew(IdVProviderByIdCacheKey.class).withAnyArguments().thenReturn(idVProviderByIdCacheKey);
        when(idVProviderByIdCache.getValueFromCache(any(IdVProviderByIdCacheKey.class), anyInt())).
                thenReturn(idVProviderCacheEntry);
        when(idVProviderCacheEntry.getIdentityVerificationProvider()).thenReturn(identityVerificationProvider);
        IdentityVerificationProvider idVProvider =
                idVProviderDAO.getIdVProvider("1c7ce08b-2ebc-4b9e-a107-3b129c019954", -1234);
        Assert.assertEquals(idVProvider.getIdVProviderName(), "ONFIDO");
    }

    @Test
    public void testIsIdVProviderExists() throws Exception {

        IdentityVerificationProvider identityVerificationProvider = addTestIdVProvider();
        IdVProviderByIdCacheKey idVProviderByIdCacheKey = mock(IdVProviderByIdCacheKey.class);
        IdVProviderCacheEntry idVProviderCacheEntry = mock(IdVProviderCacheEntry.class);
        PowerMockito.whenNew(IdVProviderByIdCacheKey.class).withAnyArguments().thenReturn(idVProviderByIdCacheKey);
        when(idVProviderByIdCache.getValueFromCache(any(IdVProviderByIdCacheKey.class), anyInt())).
                thenReturn(idVProviderCacheEntry);
        when(idVProviderCacheEntry.getIdentityVerificationProvider()).thenReturn(identityVerificationProvider);
        boolean isIdVProviderExists=
                idVProviderDAO.isIdVProviderExists("1c7ce08b-2ebc-4b9e-a107-3b129c019954", -1234);
        assertTrue(isIdVProviderExists);
    }

//
//    @Test
//    public void testAddIdVProvider() {
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
//    public void testGetCountOfIdVProviders() {
//
//    }
//
    @Test
    public void testGetIdVPByName() throws Exception {

        IdentityVerificationProvider identityVerificationProvider = addTestIdVProvider();
        IdVProviderByNameCacheKey idVProviderByNameCacheKey = mock(IdVProviderByNameCacheKey.class);
        IdVProviderCacheEntry idVProviderCacheEntry = mock(IdVProviderCacheEntry.class);
        PowerMockito.whenNew(IdVProviderByNameCacheKey.class).withAnyArguments().thenReturn(idVProviderByNameCacheKey);
        when(idVProviderByNameCache.getValueFromCache(any(IdVProviderByNameCacheKey.class), anyInt())).
                thenReturn(idVProviderCacheEntry);
        when(idVProviderCacheEntry.getIdentityVerificationProvider()).thenReturn(identityVerificationProvider);
        IdentityVerificationProvider idVProvider =
                idVProviderDAO.getIdVPByName("ONFIDO", -1234);
        Assert.assertEquals(idVProvider.getIdVProviderName(), "ONFIDO");
    }
//
//    @Test
//    public void testDeleteIdVProvider() {
//
//    }

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
}