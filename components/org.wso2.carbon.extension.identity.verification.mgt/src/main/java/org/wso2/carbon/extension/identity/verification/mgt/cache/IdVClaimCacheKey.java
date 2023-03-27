package org.wso2.carbon.extension.identity.verification.mgt.cache;

import org.wso2.carbon.identity.core.cache.CacheKey;

/**
 * Abstract representation of the idVClaim cache key.
 */
public class IdVClaimCacheKey extends CacheKey {

    private final String cacheKey;

    public IdVClaimCacheKey(String cacheKey) {

        this.cacheKey = cacheKey;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        IdVClaimCacheKey that = (IdVClaimCacheKey) o;
        return cacheKey.equals(that.cacheKey);
    }

    @Override
    public int hashCode() {

        int result = super.hashCode();
        result = 31 * result + cacheKey.hashCode();
        return result;
    }
}
