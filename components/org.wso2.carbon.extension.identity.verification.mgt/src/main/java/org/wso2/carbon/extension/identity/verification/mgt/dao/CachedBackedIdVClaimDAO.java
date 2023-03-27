package org.wso2.carbon.extension.identity.verification.mgt.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.extension.identity.verification.mgt.cache.IdVClaimByIdCache;
import org.wso2.carbon.extension.identity.verification.mgt.cache.IdVClaimByIdCacheKey;
import org.wso2.carbon.extension.identity.verification.mgt.cache.IdVClaimCacheEntry;
import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationException;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdVClaim;
import org.wso2.carbon.extension.identity.verification.provider.model.IdentityVerificationProvider;

import java.util.List;

public class CachedBackedIdVClaimDAO implements IdentityVerificationClaimDAO {

    private static final Log log = LogFactory.getLog(CachedBackedIdVClaimDAO.class);

    private final IdentityVerificationClaimDAO identityVerificationClaimDAO;
    private final IdVClaimByIdCache idVClaimByIdCache;

    public CachedBackedIdVClaimDAO(IdentityVerificationClaimDAO identityVerificationClaimDAO) {

        this.identityVerificationClaimDAO = identityVerificationClaimDAO;
        this.idVClaimByIdCache = IdVClaimByIdCache.getInstance();
    }

    @Override
    public int getPriority() {

        return 2;
    }

    @Override
    public void addIdVClaimList(List<IdVClaim> idvClaimList, int tenantId) throws IdentityVerificationException {

        identityVerificationClaimDAO.addIdVClaimList(idvClaimList, tenantId);
        addIdVClaimsToCache(idvClaimList, tenantId);
    }

    @Override
    public void updateIdVClaim(IdVClaim idVClaim, int tenantId) throws IdentityVerificationException {

        identityVerificationClaimDAO.updateIdVClaim(idVClaim, tenantId);
        deleteIdVPFromCache(idVClaim, tenantId);
    }

    @Override
    public IdVClaim getIDVClaim(String userId, String idVClaimId, int tenantId) throws IdentityVerificationException {

        IdVClaim idVClaim =
                getIdVClaimFromCacheById(idVClaimId, tenantId);
        if (idVClaim != null) {
            if (log.isDebugEnabled()) {
                String message = String.format("Cache hit for IdVClaim by it's id: %s, Tenant id: "
                        + "%d", idVClaimId, tenantId);
                log.debug(message);
            }
        } else {
            if (log.isDebugEnabled()) {
                String message = String.format("Cache miss for IdVClaim by it's id: %s. Tenant id: " +
                        "%d", idVClaimId, tenantId);
                log.debug(message);
            }
            idVClaim = identityVerificationClaimDAO.getIDVClaim(userId, idVClaimId, tenantId);
            addIdVClaimToCache(idVClaim, tenantId);
        }
        return idVClaim;
    }

    @Override
    public IdVClaim[] getIDVClaims(String userId, int tenantId) throws IdentityVerificationException {

        return identityVerificationClaimDAO.getIDVClaims(userId, tenantId);
    }

    @Override
    public void deleteIdVClaim(String idVClaimId, int tenantId) throws IdentityVerificationException {

        identityVerificationClaimDAO.deleteIdVClaim(idVClaimId, tenantId);
        deleteIdVPFromCacheById(idVClaimId, tenantId);
    }

    @Override
    public boolean isIdVClaimDataExist(String userId, String idvId, String uri, int tenantId)
            throws IdentityVerificationException {

        return identityVerificationClaimDAO.isIdVClaimDataExist(userId, idvId, uri, tenantId);
    }

    @Override
    public boolean isIdVClaimExist(String claimId, int tenantId) throws IdentityVerificationException {

        return identityVerificationClaimDAO.isIdVClaimExist(claimId, tenantId);
    }

    private IdVClaim getIdVClaimFromCacheById(String idvClaimId, int tenantId) {

        IdVClaimByIdCacheKey idVClaimByIdCacheKey = new IdVClaimByIdCacheKey(idvClaimId);
        IdVClaimCacheEntry idVClaimCacheEntry =
                idVClaimByIdCache.getValueFromCache(idVClaimByIdCacheKey, tenantId);
        if (idVClaimCacheEntry != null) {
            if (log.isDebugEnabled()) {
                String message = String.format("Entry found from IdVClaim by id cache. IdVClaim id: %s.",
                        idvClaimId);
                log.debug(message);
            }
            return idVClaimCacheEntry.getIdVClaim();
        }
        return null;
    }

    private void addIdVClaimsToCache(List<IdVClaim> idvClaimList, int tenantId) {

        for (IdVClaim idVClaim : idvClaimList) {
            addIdVClaimToCache(idVClaim, tenantId);
        }
    }

    private void addIdVClaimToCache(IdVClaim idVClaim, int tenantId) {

        if (idVClaim == null) {
            return;
        }
        IdVClaimByIdCacheKey idVClaimByIdCacheKey =
                new IdVClaimByIdCacheKey(idVClaim.getUuid());
        IdVClaimCacheEntry idVClaimCacheEntry = new IdVClaimCacheEntry(idVClaim);
        if (log.isDebugEnabled()) {
            String message = String.format("IdVClaim by id cache %s is created",
                    idVClaim.getUuid());
            log.debug(message);
        }
        idVClaimByIdCache.addToCache(idVClaimByIdCacheKey, idVClaimCacheEntry, tenantId);
    }

    private void deleteIdVPFromCache(IdVClaim idVClaim, int tenantId) {

        if (idVClaim == null) {
            return;
        }
        IdVClaimByIdCacheKey idVClaimByIdCacheKey =
                new IdVClaimByIdCacheKey(idVClaim.getUuid());

        if (log.isDebugEnabled()) {
            String message = String.format("IdVClaim by id cache %s is deleted.",
                    idVClaim.getUuid());
            log.debug(message);
        }

        idVClaimByIdCache.clearCacheEntry(idVClaimByIdCacheKey, tenantId);
    }

    private void deleteIdVPFromCacheById(String idVClaimId, int tenantId) {

        IdVClaim idVClaim = getIdVClaimFromCacheById(idVClaimId, tenantId);
        if (idVClaim == null) {
            return;
        }
        deleteIdVPFromCache(idVClaim, tenantId);
    }
}
