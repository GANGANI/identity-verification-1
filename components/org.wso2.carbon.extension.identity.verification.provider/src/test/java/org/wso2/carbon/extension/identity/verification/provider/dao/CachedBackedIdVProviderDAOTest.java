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

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.extension.identity.verification.provider.cache.IdVProviderByIdCache;
import org.wso2.carbon.extension.identity.verification.provider.cache.IdVProviderByIdCacheKey;
import org.wso2.carbon.extension.identity.verification.provider.cache.IdVProviderByNameCache;
import org.wso2.carbon.extension.identity.verification.provider.cache.IdVProviderByNameCacheKey;
import org.wso2.carbon.extension.identity.verification.provider.cache.IdVProviderCacheEntry;
import org.wso2.carbon.extension.identity.verification.provider.internal.IdVProviderDataHolder;
import org.wso2.carbon.extension.identity.verification.provider.model.IdentityVerificationProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.addTestIdVProvider;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.idVProviderId;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.idVProviderName;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.tenantId;

@PrepareForTest({IdVProviderByIdCache.class, IdVProviderByNameCache.class})
public class CachedBackedIdVProviderDAOTest {

    private IdVProviderDAO idVProviderDAO;
    private IdVProviderByNameCache idVProviderByNameCache;
    private IdVProviderByIdCache idVProviderByIdCache;
    private IdVProviderDAO idVProviderDAOImpl;
    private IdVProviderByIdCacheKey idVProviderByIdCacheKey;
    private IdVProviderCacheEntry idVProviderCacheEntry;
    private IdVProviderByNameCacheKey idVProviderByNameCacheKey;

    @Test
    public void testGetPriority() {

        int priority = idVProviderDAO.getPriority();
        Assert.assertEquals(priority, 2);
    }

    @BeforeMethod
    public void setUp() {

        idVProviderDAOImpl = mock(IdVProviderDAOImpl.class);

        mockStatic(IdVProviderByIdCache.class);
        idVProviderByIdCache = mock(IdVProviderByIdCache.class);
        when(IdVProviderByIdCache.getInstance()).thenReturn(idVProviderByIdCache);

        mockStatic(IdVProviderByNameCache.class);
        idVProviderByNameCache = mock(IdVProviderByNameCache.class);
        when(IdVProviderByNameCache.getInstance()).thenReturn(idVProviderByNameCache);

        idVProviderDAO = new CachedBackedIdVProviderDAO(idVProviderDAOImpl);
        IdVProviderDataHolder.getInstance().
                setIdVProviderDAOs(Collections.singletonList(idVProviderDAO));

        idVProviderByIdCacheKey = mock(IdVProviderByIdCacheKey.class);
        idVProviderCacheEntry = mock(IdVProviderCacheEntry.class);
        idVProviderByNameCacheKey = mock(IdVProviderByNameCacheKey.class);
    }

    @Test
    public void testGetIdVProvider() throws Exception {

        IdentityVerificationProvider identityVerificationProvider = addTestIdVProvider();
        whenNew(IdVProviderByIdCacheKey.class).withAnyArguments().thenReturn(idVProviderByIdCacheKey);
        when(idVProviderByIdCache.getValueFromCache(any(IdVProviderByIdCacheKey.class), anyInt())).
                thenReturn(idVProviderCacheEntry);
        when(idVProviderCacheEntry.getIdentityVerificationProvider()).thenReturn(identityVerificationProvider);
        IdentityVerificationProvider idVProvider =
                idVProviderDAO.getIdVProvider(idVProviderId, tenantId);
        Assert.assertEquals(idVProvider.getIdVProviderName(), idVProviderName);
    }

    @Test
    public void testIsIdVProviderExists() throws Exception {

        IdentityVerificationProvider identityVerificationProvider = addTestIdVProvider();
        whenNew(IdVProviderByIdCacheKey.class).withAnyArguments().thenReturn(idVProviderByIdCacheKey);
        when(idVProviderByIdCache.getValueFromCache(any(IdVProviderByIdCacheKey.class), anyInt())).
                thenReturn(idVProviderCacheEntry);
        when(idVProviderCacheEntry.getIdentityVerificationProvider()).thenReturn(identityVerificationProvider);
        boolean isIdVProviderExists=
                idVProviderDAO.isIdVProviderExists(idVProviderId, tenantId);
        assertTrue(isIdVProviderExists);
    }

    @Test
    public void testAddIdVProvider() throws Exception {

        IdentityVerificationProvider identityVerificationProvider = addTestIdVProvider();
        doNothing().when(idVProviderDAOImpl).
                addIdVProvider(any(IdentityVerificationProvider.class), anyInt());

        whenNew(IdVProviderByIdCacheKey.class).withAnyArguments().thenReturn(idVProviderByIdCacheKey);
        whenNew(IdVProviderCacheEntry.class).withAnyArguments().thenReturn(idVProviderCacheEntry);


        idVProviderDAO.addIdVProvider(identityVerificationProvider, tenantId);
    }

    @Test
    public void testUpdateIdVProvider() throws Exception {

        IdentityVerificationProvider identityVerificationProvider = addTestIdVProvider();
        doNothing().when(idVProviderDAOImpl).updateIdVProvider(any(IdentityVerificationProvider.class),
                any(IdentityVerificationProvider.class), anyInt());

        whenNew(IdVProviderByIdCacheKey.class).withAnyArguments().thenReturn(idVProviderByIdCacheKey);

        doNothing().when(idVProviderByIdCache).
                clearCacheEntry(any(IdVProviderByIdCacheKey.class), anyInt());

        idVProviderDAO.updateIdVProvider(identityVerificationProvider, identityVerificationProvider, tenantId);
    }

    @Test
    public void testGetIdVProviders() throws Exception {

        List<IdentityVerificationProvider> idvProviders = new ArrayList<>();
        idvProviders.add(addTestIdVProvider());
        when(idVProviderDAOImpl.getIdVProviders(anyInt(), anyInt(), anyInt())).thenReturn(idvProviders);
        List<IdentityVerificationProvider> identityVerificationProviders =
                idVProviderDAO.getIdVProviders(2, 0, tenantId);
        Assert.assertEquals(identityVerificationProviders, idvProviders);
    }

    @Test
    public void testGetCountOfIdVProviders() throws Exception {

        when(idVProviderDAOImpl.getCountOfIdVProviders(anyInt())).thenReturn(1);
        int countOfIdVProviders = idVProviderDAO.getCountOfIdVProviders(tenantId);
        Assert.assertEquals(countOfIdVProviders, 1);
    }

    @Test
    public void testGetIdVPByName() throws Exception {

        IdentityVerificationProvider identityVerificationProvider = addTestIdVProvider();
        whenNew(IdVProviderByNameCacheKey.class).withAnyArguments().thenReturn(idVProviderByNameCacheKey);
        when(idVProviderByNameCache.getValueFromCache(any(IdVProviderByNameCacheKey.class), anyInt())).
                thenReturn(idVProviderCacheEntry);
        when(idVProviderCacheEntry.getIdentityVerificationProvider()).thenReturn(identityVerificationProvider);
        IdentityVerificationProvider idVProvider =
                idVProviderDAO.getIdVPByName(idVProviderName, tenantId);
        Assert.assertEquals(idVProvider.getIdVProviderName(), idVProviderName);
    }

    @Test
    public void testDeleteIdVProvider() throws Exception {

        IdentityVerificationProvider identityVerificationProvider = addTestIdVProvider();
        whenNew(IdVProviderByIdCacheKey.class).withAnyArguments().thenReturn(idVProviderByIdCacheKey);
        when(idVProviderByIdCache.getValueFromCache(any(IdVProviderByIdCacheKey.class), anyInt())).
                thenReturn(idVProviderCacheEntry);
        when(idVProviderCacheEntry.getIdentityVerificationProvider()).thenReturn(identityVerificationProvider);
        idVProviderDAO.deleteIdVProvider(idVProviderId, tenantId);
    }
}