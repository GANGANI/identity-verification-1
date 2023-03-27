package org.wso2.carbon.extension.identity.verification.mgt.cache;

/**
 * Cache key for {@link org.wso2.carbon.extension.identity.verification.mgt.model.IdVClaim}
 * caches by its id.
 */
public class IdVClaimByIdCacheKey extends IdVClaimCacheKey {

    public IdVClaimByIdCacheKey(String idVClaimId) {

        super(idVClaimId);
    }
}
