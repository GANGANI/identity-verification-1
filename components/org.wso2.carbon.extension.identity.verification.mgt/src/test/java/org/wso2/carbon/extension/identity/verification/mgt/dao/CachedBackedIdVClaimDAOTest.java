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

package org.wso2.carbon.extension.identity.verification.mgt.dao;

import org.json.JSONObject;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.extension.identity.verification.mgt.cache.IdVClaimByIdCache;
import org.wso2.carbon.extension.identity.verification.mgt.cache.IdVClaimByIdCacheKey;
import org.wso2.carbon.extension.identity.verification.mgt.cache.IdVClaimCacheEntry;
import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationException;
import org.wso2.carbon.extension.identity.verification.mgt.internal.IdentityVerificationDataHolder;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdVClaim;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest({IdVClaimByIdCache.class})
public class CachedBackedIdVClaimDAOTest {

    private IdentityVerificationClaimDAO identityVerificationClaimDAO;
    private IdVClaimByIdCache idVClaimByIdCache;
    private IdentityVerificationClaimDAO identityVerificationClaimDAOImpl;
    private IdVClaimByIdCacheKey idVClaimByIdCacheKey;
    private IdVClaimCacheEntry idVClaimCacheEntry;

    @BeforeMethod
    public void setUp() {

        identityVerificationClaimDAOImpl = mock(IdentityVerificationClaimDAOImpl.class);

        mockStatic(IdVClaimByIdCache.class);
        idVClaimByIdCache = mock(IdVClaimByIdCache.class);
        when(IdVClaimByIdCache.getInstance()).thenReturn(idVClaimByIdCache);

        identityVerificationClaimDAO = new CachedBackedIdVClaimDAO(identityVerificationClaimDAOImpl);
        IdentityVerificationDataHolder.getInstance().
                setIdVClaimDAOs(Collections.singletonList(identityVerificationClaimDAO));

        idVClaimByIdCacheKey = mock(IdVClaimByIdCacheKey.class);
        idVClaimCacheEntry = mock(IdVClaimCacheEntry.class);
    }

    @Test
    public void testAddIdVClaimList() throws Exception {

        PowerMockito.whenNew(IdVClaimByIdCacheKey.class).withAnyArguments().thenReturn(idVClaimByIdCacheKey);
        PowerMockito.whenNew(IdVClaimCacheEntry.class).withAnyArguments().thenReturn(idVClaimCacheEntry);
        PowerMockito.doNothing().when(identityVerificationClaimDAOImpl).addIdVClaimList(anyList(), anyInt());
        identityVerificationClaimDAO.addIdVClaimList(Arrays.asList(getIdVClaims()), -1234);
    }

    @Test
    public void testUpdateIdVClaim() throws Exception {

        PowerMockito.whenNew(IdVClaimByIdCacheKey.class).withAnyArguments().thenReturn(idVClaimByIdCacheKey);
        PowerMockito.whenNew(IdVClaimCacheEntry.class).withAnyArguments().thenReturn(idVClaimCacheEntry);
        PowerMockito.doNothing().when(identityVerificationClaimDAOImpl).updateIdVClaim(any(IdVClaim.class), anyInt());
        identityVerificationClaimDAO.updateIdVClaim(getIdVClaim(), -1234);
    }

    @Test
    public void testGetIDVClaim() throws Exception {

        IdVClaim idVClaim = getIdVClaim();
        PowerMockito.whenNew(IdVClaimByIdCacheKey.class).withAnyArguments().thenReturn(idVClaimByIdCacheKey);
        when(idVClaimByIdCache.getValueFromCache(any(IdVClaimByIdCacheKey.class), anyInt())).
                thenReturn(idVClaimCacheEntry);
        when(idVClaimCacheEntry.getIdVClaim()).thenReturn(idVClaim);
        IdVClaim identityVerificationClaim = identityVerificationClaimDAO.
                getIDVClaim("715558cb-d9c1-4a23-af09-3d95284d8e2b",
                "575a3d28-c6fb-46c8-bf63-45530448ca17", -1234);
        Assert.assertEquals(identityVerificationClaim.getClaimUri(), idVClaim.getClaimUri());
    }

    @Test
    public void testGetIDVClaims() throws Exception {

        Mockito.when(identityVerificationClaimDAOImpl.getIDVClaims(anyString(), anyInt())).
                thenReturn(getIdVClaims());
        IdVClaim[] idVClaims = identityVerificationClaimDAO.
                getIDVClaims("715558cb-d9c1-4a23-af09-3d95284d8e2b", -1234);
        Assert.assertEquals(idVClaims.length, 1);
    }

    @Test
    public void testDeleteIdVClaim() throws Exception {

        IdVClaim idVClaim = getIdVClaim();
        PowerMockito.whenNew(IdVClaimByIdCacheKey.class).withAnyArguments().thenReturn(idVClaimByIdCacheKey);
        when(idVClaimByIdCache.getValueFromCache(any(IdVClaimByIdCacheKey.class), anyInt())).
                thenReturn(idVClaimCacheEntry);
        when(idVClaimCacheEntry.getIdVClaim()).thenReturn(idVClaim);
        identityVerificationClaimDAO.
                deleteIdVClaim("715558cb-d9c1-4a23-af09-3d95284d8e2b",
                        "575a3d28-c6fb-46c8-bf63-45530448ca17", -1234);
    }

    @Test
    public void testIsIdVClaimDataExist() throws Exception {

        Mockito.when(identityVerificationClaimDAOImpl.isIdVClaimDataExist(anyString(), anyString(), anyString(),
                        anyInt())).thenReturn(true);
        boolean isIdVClaimDataExist = identityVerificationClaimDAO.
                isIdVClaimDataExist("715558cb-d9c1-4a23-af09-3d95284d8e2b",
                        "575a3d28-c6fb-46c8-bf63-45530448ca17",
                        "575a3d28-c6fb-46c8-bf63-45530448ca17", -1234);
        Assert.assertTrue(isIdVClaimDataExist);
    }

    @Test
    public void testIsIdVClaimExist() throws IdentityVerificationException {

        Mockito.when(identityVerificationClaimDAOImpl.isIdVClaimExist(anyString(), anyInt())).thenReturn(true);
        boolean isIdVClaimExist = identityVerificationClaimDAO.
                isIdVClaimExist("715558cb-d9c1-4a23-af09-3d95284d8e2b", -1234);
        Assert.assertTrue(isIdVClaimExist);
    }

    private IdVClaim[] getIdVClaims() {

        IdVClaim[] idVClaims = new IdVClaim[1];
        idVClaims[0] = getIdVClaim();
        return idVClaims;
    }

    private static IdVClaim getIdVClaim() {

        IdVClaim idVClaim = new IdVClaim();
        idVClaim.setUserId("715558cb-d9c1-4a23-af09-3d95284d8e2b");
        idVClaim.setUuid("575a3d28-c6fb-46c8-bf63-45530448ca17");
        idVClaim.setClaimUri("http://wso2.org/claims/dob");
        idVClaim.setStatus(true);
        idVClaim.setIdVPId("1c7ce08b-2ebc-4b9e-a107-3b129c019954");
        idVClaim.setMetadata(new JSONObject("{\n" +
                "      \"source\": \"evidentID\",\n" +
                "      \"trackingId\": \"123e4567-e89b-12d3-a456-556642440000\"\n" +
                "    }"));
        return idVClaim;
    }

    @Test
    public void testGetPriority() {

        int priority = identityVerificationClaimDAO.getPriority();
        Assert.assertEquals(priority, 2);
    }
}