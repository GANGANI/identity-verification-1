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

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.extension.identity.verification.provider.dao.IdVProviderManagementDAO;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtException;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVConfigProperty;
import org.wso2.carbon.extension.identity.verification.provider.model.IdentityVerificationProvider;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

/**
 * Test class for IdVProviderManagerImpl.
 */
public class IdVProviderManagerImplTest {

    private IdVProviderManagerImpl idVProviderManager;
    private IdVProviderManagementDAO idVProviderManagementDAO;

    @BeforeMethod
    public void setUp() {

        idVProviderManager = IdVProviderManagerImpl.getInstance();
        idVProviderManagementDAO = new IdVProviderManagementDAO();
    }

    @Test(expectedExceptions = IdVProviderMgtException.class)
    public void testGetIdVProviderWithEmptyIdVPId() throws IdVProviderMgtException {

        String idVProviderId = "";
        int tenantId = 1;
        doThrow(new IdVProviderMgtException("Error: Empty IdVP Id")).when(idVProviderManagementDAO)
                .getIdVProvider(idVProviderId, tenantId);
        idVProviderManager.getIdVProvider(idVProviderId, tenantId);
    }

    @Test
    public void testGetIdVProvider() throws IdVProviderMgtException {

        String idVProviderId = "1c7ce08b-2ebc-4b9e-a107-3b129c019954";
        int tenantId = 1;
        IdentityVerificationProvider idVProvider = createIdVProvider();
        when(idVProviderManagementDAO.getIdVProvider(idVProviderId, tenantId)).thenReturn(idVProvider);
        IdentityVerificationProvider result = idVProviderManager.getIdVProvider(idVProviderId, tenantId);
        Assert.assertEquals(result.getId(), idVProviderId);
    }

    @Test
    public void testAddIdVProvider() {

    }

    @Test
    public void testGetCountOfIdVProviders() {

    }

    @Test
    public void testDeleteIdVProvider() {

    }

    @Test
    public void testUpdateIdVProvider() {

    }

    @Test
    public void testGetIdVProviders() {

    }

    @Test
    public void testIsIdVProviderExists() {

    }

    @Test
    public void testGetIdVPByName() {

    }

    private IdentityVerificationProvider createIdVProvider() {

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