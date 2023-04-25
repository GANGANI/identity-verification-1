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

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.extension.identity.verification.provider.dao.IdVProviderDAO;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtClientException;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtException;
import org.wso2.carbon.extension.identity.verification.provider.internal.IdVProviderDataHolder;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVConfigProperty;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.addTestIdVProvider;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.idVProviderId;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.idVProviderName;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.tenantId;

/**
 * Test class for IdVProviderManagerImpl.
 */
@PrepareForTest({IdVProviderDataHolder.class, IdVProvider.class})
public class IdVProviderManagerImplTest extends PowerMockTestCase {

    private IdVProviderManager idVProviderManager;
    private IdVProviderDataHolder idVProviderDataHolder;
    private IdVProviderDAO idVProviderDAO;

    @BeforeMethod
    public void setUp() {

        idVProviderDAO = mock(IdVProviderDAO.class);
        idVProviderDataHolder = mock(IdVProviderDataHolder.class);

        mockIdVProviderDAO();
        idVProviderManager = new IdVProviderManagerImpl();
    }

    @Test
    public void testGetIdVProvider() throws IdVProviderMgtException {

        IdVProvider idvProvider = addTestIdVProvider();
        doReturn(idvProvider).when(idVProviderDAO).getIdVProvider(anyString(), anyInt());
        IdVProvider identityVerificationProvider =
                idVProviderManager.getIdVProvider(idVProviderId, tenantId);
        Assert.assertEquals(identityVerificationProvider.getIdVProviderUuid(), idVProviderId);
    }

    @Test(expectedExceptions = IdVProviderMgtClientException.class)
    public void testGetIdVProviderEmptyIdVProviderID() {

        try {
            idVProviderManager.getIdVProvider(null, 1);
        } catch (IdVProviderMgtException e) {
            Assert.assertEquals(e.getErrorCode(), "IdVProvider ID cannot be empty.");
        }
    }

    @DataProvider(name = "addIdVProvider")
    public Object[][] addIdVProvider() {

        IdVProvider idvProvider = addTestIdVProvider1();
        return new Object[][]{
                {idvProvider, idvProvider},
                {null, null}};
    }

//    @Test(dataProvider = "addIdVProvider")
//    public void testAddIdVProvider(IdVProvider idvProvider, IdVProvider expectedIdVProvider) throws Exception {
//
//        doNothing().when(idVProviderDAO).addIdVProvider(any(IdVProvider.class), anyInt());
//        IdVProvider identityVerificationProvider =
//                idVProviderManager.addIdVProvider(idvProvider, -1234);
//        Assert.assertEquals(identityVerificationProvider.getIdVProviderName(),
//                expectedIdVProvider.getIdVProviderName());
//    }

    @Test
    public void testGetCountOfIdVProviders() throws IdVProviderMgtException {

        doReturn(5).when(idVProviderDAO).getCountOfIdVProviders(anyInt());
        int countOfIdVProviders = idVProviderManager.getCountOfIdVProviders(tenantId);
        Assert.assertEquals(countOfIdVProviders, 5);
    }

    @Test
    public void testDeleteIdVProvider() throws IdVProviderMgtException {

        doNothing().when(idVProviderDAO).deleteIdVProvider(anyString(), anyInt());
        idVProviderManager.deleteIdVProvider(idVProviderId, 1);

    }

    @Test
    public void testUpdateIdVProvider() throws IdVProviderMgtException {

        IdVProvider idvProvider = addTestIdVProvider();
        doNothing().when(idVProviderDAO).updateIdVProvider(any(IdVProvider.class), any(IdVProvider.class), anyInt());
        IdVProvider idvProviderList = idVProviderManager.updateIdVProvider(idvProvider, idvProvider, 1);
        Assert.assertEquals(idvProviderList.getIdVProviderDescription(),
                "ONFIDO identity verification provider");
    }

    @DataProvider(name = "getIdVProvidersData")
    public Object[][] createValidParameters() {

        return new Object[][]{
                {2, 0, 1},
                {null, 0, 1},
                {105, 0, 1},
                {2, null, 1}};
    }

    @Test(dataProvider = "getIdVProvidersData")
    public void testGetIdVProviders(Integer limit, Integer offset, int expected)
            throws IdVProviderMgtException {

        List<IdVProvider> idvProviders = new ArrayList<>();
        idvProviders.add(addTestIdVProvider());
        doReturn(idvProviders).when(idVProviderDAO).getIdVProviders(anyInt(), anyInt(), anyInt());
        List<IdVProvider> idvProviderList = idVProviderManager.getIdVProviders(limit, offset, 1);
        Assert.assertEquals(idvProviderList.size(), expected);
    }

    @DataProvider(name = "getIdVProvidersDataWithInvalidInputs")
    public Object[][] createInvalidValidParameters() {

        return new Object[][]{
                {-1, 0},
                {2, -1}};
    }

    @Test(dataProvider = "getIdVProvidersDataWithInvalidInputs",
            expectedExceptions = IdVProviderMgtClientException.class)
    public void testGetIdVProvidersInvalidInputs(Integer limit, Integer offset)
            throws IdVProviderMgtException {

        List<IdVProvider> idvProviders = new ArrayList<>();
        idvProviders.add(addTestIdVProvider());
        doReturn(idvProviders).when(idVProviderDAO).getIdVProviders(anyInt(), anyInt(), anyInt());
        idVProviderManager.getIdVProviders(limit, offset, 1);
    }

    @Test
    public void testIsIdVProviderExists() throws IdVProviderMgtException {

        doReturn(true).when(idVProviderDAO).isIdVProviderExists(anyString(), anyInt());
        boolean idVProviderExists = idVProviderManager.isIdVProviderExists(idVProviderId, tenantId);
        Assert.assertTrue(idVProviderExists);
    }

    @Test
    public void testGetIdVPByName() throws IdVProviderMgtException {

        IdVProvider idvProvider = addTestIdVProvider();
        doReturn(idvProvider).when(idVProviderDAO).getIdVProviderByName(anyString(), anyInt());
        IdVProvider identityVerificationProvider =
                idVProviderManager.getIdVProviderByName(idVProviderName, 1);
        Assert.assertNotNull(identityVerificationProvider);
        Assert.assertEquals(identityVerificationProvider.getIdVProviderName(), idVProviderName);
    }

    private void mockIdVProviderDAO() {

        PowerMockito.mockStatic(IdVProviderDataHolder.class);
        when(IdVProviderDataHolder.getInstance()).thenReturn(idVProviderDataHolder);
        when(idVProviderDataHolder.getIdVProviderDAOs()).thenReturn(Collections.singletonList(idVProviderDAO));
    }

    public static IdVProvider addTestIdVProvider1() {

        IdVProvider idVProvider = new IdVProvider();
        idVProvider.setIdVProviderUUID(idVProviderId);
        idVProvider.setIdVProviderName(idVProviderName);
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
