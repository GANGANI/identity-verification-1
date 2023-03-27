package org.wso2.carbon.extension.identity.verification.mgt.cache;

import org.wso2.carbon.extension.identity.verification.mgt.model.IdVClaim;
import org.wso2.carbon.identity.core.cache.CacheEntry;

/**
 * Represents a cache entry for {@link IdVClaim}.
 */
public class IdVClaimCacheEntry extends CacheEntry {

    private IdVClaim idVClaim;

    public IdVClaimCacheEntry(IdVClaim idVClaim) {

        this.idVClaim = idVClaim;
    }

    public IdVClaim getIdVClaim() {

        return idVClaim;
    }

    public void setIdVClaim(IdVClaim idVClaim) {

        this.idVClaim = idVClaim;
    }
}
