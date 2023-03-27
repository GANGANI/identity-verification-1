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
package org.wso2.carbon.extension.identity.verification.mgt.cache;

import org.wso2.carbon.extension.identity.verification.provider.cache.IdVProviderByIdCache;
import org.wso2.carbon.extension.identity.verification.provider.cache.IdVProviderByIdCacheKey;
import org.wso2.carbon.extension.identity.verification.provider.cache.IdVProviderCacheEntry;
import org.wso2.carbon.identity.core.cache.BaseCache;

public class IdVClaimByIdCache extends BaseCache<IdVClaimCacheKey, IdVClaimCacheEntry> {

    private static final String IDV_CLAIM_CACHE_NAME = "IdVClaimByIdCache";
    private static volatile IdVClaimByIdCache instance;

    private IdVClaimByIdCache() {

        super(IDV_CLAIM_CACHE_NAME);
    }

    public static IdVClaimByIdCache getInstance() {

        if (instance == null) {
            synchronized (IdVClaimByIdCache.class) {
                if (instance == null) {
                    instance = new IdVClaimByIdCache();
                }
            }
        }
        return instance;
    }
}
