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

package org.wso2.carbon.extension.identity.verification.mgt.web;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.client.HttpClients;
import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationException;

import java.io.IOException;

import static java.util.Objects.isNull;

public class IdVHTTPClientManager {

    private static final int HTTP_CONNECTION_TIMEOUT = 3000;
    private static final int HTTP_READ_TIMEOUT = 3000;
    private static final int HTTP_CONNECTION_REQUEST_TIMEOUT = 3000;
    private static final int DEFAULT_MAX_CONNECTIONS = 20;
    private static volatile IdVHTTPClientManager idVHTTPClientManagerInstance;
    private final CloseableHttpClient httpClient;

    /**
     * Creates a client manager.
     *
     * @throws IdentityVerificationException Exception thrown when an error occurred when creating HTTP client.
     */
    private IdVHTTPClientManager() throws IdentityVerificationException {

        PoolingHttpClientConnectionManager connectionManager;
        try {
            connectionManager = createPoolingConnectionManager();
        } catch (IOException e) {
            throw new IdentityVerificationException("Error occurred while creating HTTP client.", e);
        }

        RequestConfig config = createRequestConfig();
        httpClient = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .setConnectionManager(connectionManager).build();
    }

    /**
     * Returns an instance of the HTTPClientManager.
     *
     * @throws IdentityVerificationException Exception thrown when an error occurred when creating HTTP client.
     */
    public static IdVHTTPClientManager getInstance() throws IdentityVerificationException {

        if (idVHTTPClientManagerInstance == null) {
            synchronized (IdVHTTPClientManager.class) {
                if (idVHTTPClientManagerInstance == null) {
                    idVHTTPClientManagerInstance = new IdVHTTPClientManager();
                }
            }
        }
        return idVHTTPClientManagerInstance;
    }

    /**
     * Get HTTP client.
     *
     * @return CloseableHttpClient instance.
     * @throws IdentityVerificationException Exception thrown when an error occurred when getting HTTP client.
     */
    public CloseableHttpClient getHttpClient() throws IdentityVerificationException {

        if (isNull(httpClient)) {
            throw new IdentityVerificationException("Error occurred while getting HTTP client.");
        }
        return httpClient;
    }

    private RequestConfig createRequestConfig() {

        return RequestConfig.custom()
                .setConnectTimeout(HTTP_CONNECTION_TIMEOUT)
                .setConnectionRequestTimeout(HTTP_CONNECTION_REQUEST_TIMEOUT)
                .setSocketTimeout(HTTP_READ_TIMEOUT)
                .setRedirectsEnabled(false)
                .setRelativeRedirectsAllowed(false)
                .build();
    }

    private PoolingHttpClientConnectionManager createPoolingConnectionManager() throws IOException {

        PoolingHttpClientConnectionManager poolingHttpClientConnectionMgr = new PoolingHttpClientConnectionManager();
        // Increase max total connection to 20.
        poolingHttpClientConnectionMgr.setMaxTotal(DEFAULT_MAX_CONNECTIONS);
        // Increase default max connection per route to 20.
        poolingHttpClientConnectionMgr.setDefaultMaxPerRoute(DEFAULT_MAX_CONNECTIONS);
        return poolingHttpClientConnectionMgr;
    }
}
