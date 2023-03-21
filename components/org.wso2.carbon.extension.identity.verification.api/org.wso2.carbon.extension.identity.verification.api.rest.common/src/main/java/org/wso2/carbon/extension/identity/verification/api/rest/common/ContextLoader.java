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

package org.wso2.carbon.extension.identity.verification.api.rest.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.extension.identity.verification.api.rest.common.error.APIError;
import org.wso2.carbon.extension.identity.verification.api.rest.common.error.ErrorResponse;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;

import javax.ws.rs.core.Response;

import static org.wso2.carbon.extension.identity.verification.api.rest.common.Constants.ErrorMessage.ERROR_RESOLVING_USER;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.Error.UNEXPECTED_SERVER_ERROR;

/**
 * Load information from context.
 */
public class ContextLoader {

    private static final Log log = LogFactory.getLog(ContextLoader.class);

    /**
     * Get tenant domain from Auth user.
     *
     * @return tenant domain.
     */
    public static String getTenantDomainFromAuthUser() {

        String tenantDomain = null;
        if (IdentityUtil.threadLocalProperties.get().get(Constants.AUTH_USER_TENANT_DOMAIN) != null) {
            tenantDomain = (String) IdentityUtil.threadLocalProperties.get().get(Constants.AUTH_USER_TENANT_DOMAIN);
        }
        return tenantDomain;
    }

    /**
     * Retrieves authenticated username from carbon context.
     *
     * @return username of the authenticated user.
     */
    public static String getUsernameFromContext() {

        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
    }

    /**
     * Retrieves authenticated username from carbon context.
     *
     * @return username of the authenticated user.
     */
    public static String getUserIdFromContext() {

        try {
            UserRealm userRealm = CarbonContext.getThreadLocalCarbonContext().getUserRealm();
            AbstractUserStoreManager userStoreManager = (AbstractUserStoreManager) userRealm.getUserStoreManager();

            if (userStoreManager == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Userstore Manager is null");
                }
                throw buildInternalServerError(ERROR_RESOLVING_USER.getMessage(),
                        ERROR_RESOLVING_USER.getDescription());
            }
            return userStoreManager.getUserIDFromUserName(getUsernameFromContext());
        } catch (UserStoreException e) {
            throw buildInternalServerError(ERROR_RESOLVING_USER.getMessage(),
                    ERROR_RESOLVING_USER.getDescription());
        }
    }

    /**
     * Builds APIError to be thrown if the URL building fails.
     *
     * @param e                Exception occurred while building the URL.
     * @param errorDescription Description of the error.
     * @return APIError object which contains the error description.
     */
    private static APIError buildInternalServerError(String errorMessage, String errorDescription) {

        String errorCode = UNEXPECTED_SERVER_ERROR.getCode();

        ErrorResponse errorResponse = new ErrorResponse.Builder().
                withCode(errorCode)
                .withMessage(errorMessage)
                .withDescription(errorDescription)
                .build(log, errorMessage, false);
        Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
        return new APIError(status, errorResponse);
    }
}
