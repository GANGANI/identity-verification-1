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
