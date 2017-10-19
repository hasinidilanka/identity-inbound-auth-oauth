/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.webfinger.builders;

import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.webfinger.WebFingerConstants;
import org.wso2.carbon.identity.webfinger.WebFingerEndpointException;
import org.wso2.carbon.identity.webfinger.WebFingerRequest;
import org.wso2.carbon.identity.webfinger.internal.WebFingerServiceComponentHolder;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import java.util.Enumeration;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;


import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@PrepareForTest({HttpServletRequest.class, WebFingerServiceComponentHolder.class, RealmService.class,
        TenantManager.class})
public class DefaultWebFingerRequestBuilderTest extends PowerMockTestCase {
    private DefaultWebFingerRequestBuilder defaultWebFingerRequestBuilder;
    private String rel;
    private String resource;
    private String host;
    private String tenant;
    private String path;
    private String scheme;
    private int port;
    private String acctResource;
    private String invalidAcctResource;

    @Mock
    HttpServletRequest request;

    @Mock
    WebFingerServiceComponentHolder webFingerServiceComponentHolder;

    @Mock
    RealmService realmService;

    @Mock
    TenantManager tenantManager;

    @BeforeMethod
    public void setUp() throws Exception {
        defaultWebFingerRequestBuilder = new DefaultWebFingerRequestBuilder();
        rel = "http://openid.net/specs/connect/1.0/issuer";
        resource = "https://oidc.testdomain.wso2.org:9443/.well-known/webfinger";
        host = "oidc.testdomain.wso2.org";
        tenant = "carbon.super";
        path = "/.well-known/webfinger";
        acctResource = "acct:joe@example.com";
        invalidAcctResource = "acct:joe.example.com";
        scheme = "https";
        port = 9443;

        mockStatic(WebFingerServiceComponentHolder.class);
        when(WebFingerServiceComponentHolder.getRealmService()).thenReturn(realmService);
        when(realmService.getTenantManager()).thenReturn(tenantManager);
    }

    @DataProvider(name = "BuildParameters")
    public Object[][] buildScopeString() {
        Vector<String> vector1 = new Vector<>();
        vector1.add("value1");
        vector1.add("value2");
        Enumeration<String> params1 = vector1.elements();

        Vector<String> vector2 = new Vector<>();
        vector2.add("value1");
        Enumeration<String> params2 = vector2.elements();

        Vector<String> vector3 = new Vector<>();
        vector3.add(WebFingerConstants.REL);
        vector3.add("value2");
        Enumeration<String> params3 = vector3.elements();

        Vector<String> vector4 = new Vector<>();
        vector3.add(WebFingerConstants.RESOURCE);
        vector3.add("value2");
        Enumeration<String> params4 = vector4.elements();
        return new Object[][] {
                {params1},
                {params2},
                {params3},
                {params4}
        };
    }
    //Test for exception "Bad webfinger request"
    @Test(dataProvider = "BuildParameters")
    public void testInvalidRequest(Enumeration<String> params) {
        when(request.getParameterNames()).thenReturn(params);
        try {
            defaultWebFingerRequestBuilder.buildRequest(request);
        } catch (WebFingerEndpointException e) {
            assertEquals(e.getErrorCode(), WebFingerConstants.ERROR_CODE_INVALID_REQUEST);
        }
    }


    @Test
    public  void testInvalidResource() throws WebFingerEndpointException {
        Vector<String> vector = new Vector<>();
        vector.add(WebFingerConstants.REL);
        vector.add(WebFingerConstants.RESOURCE);
        Enumeration<String> params = vector.elements();
        when(request.getParameterNames()).thenReturn(params);


        try {
            defaultWebFingerRequestBuilder.buildRequest(request);
        } catch (WebFingerEndpointException e) {
            assertEquals(e.getErrorCode(), WebFingerConstants.ERROR_CODE_INVALID_RESOURCE);
        }
    }


    @Test
    public  void testResourceEmpty() throws WebFingerEndpointException {
        Vector<String> vector = new Vector<>();
        vector.add(WebFingerConstants.REL);
        vector.add(WebFingerConstants.RESOURCE);
        Enumeration<String> params = vector.elements();
        when(request.getParameterNames()).thenReturn(params);
        when(request.getParameter(WebFingerConstants.RESOURCE)).thenReturn("9443/.well-known/webfinger");
        when(request.getParameter(WebFingerConstants.REL)).thenReturn(rel);

        try {
            defaultWebFingerRequestBuilder.buildRequest(request);
        } catch (WebFingerEndpointException e) {
            assertEquals(e.getErrorMessage(), "Scheme of the resource cannot be empty");
        }
    }

    @Test
    public  void testUserInfoInvalid() throws WebFingerEndpointException {
        Vector<String> vector = new Vector<>();
        vector.add(WebFingerConstants.REL);
        vector.add(WebFingerConstants.RESOURCE);
        Enumeration<String> params = vector.elements();
        when(request.getParameterNames()).thenReturn(params);
        when(request.getParameter(WebFingerConstants.RESOURCE)).thenReturn(invalidAcctResource);
        when(request.getParameter(WebFingerConstants.REL)).thenReturn(rel);

        try {
            defaultWebFingerRequestBuilder.buildRequest(request);
        } catch (WebFingerEndpointException e) {
            assertEquals(e.getErrorMessage(), "Invalid host value.");
        }
    }

    @Test
    public  void testUserInfo() throws WebFingerEndpointException, UserStoreException {
        Vector<String> vector = new Vector<>();
        vector.add(WebFingerConstants.REL);
        vector.add(WebFingerConstants.RESOURCE);
        Enumeration<String> params = vector.elements();
        when(request.getParameterNames()).thenReturn(params);
        when(request.getParameter(WebFingerConstants.RESOURCE)).thenReturn(acctResource);
        when(request.getParameter(WebFingerConstants.REL)).thenReturn(rel);
        mockStatic(WebFingerServiceComponentHolder.class);
        when(WebFingerServiceComponentHolder.getRealmService()).thenReturn(realmService);
        when(realmService.getTenantManager()).thenReturn(tenantManager);
        when(tenantManager.getTenantId(any(String.class))).thenReturn(MultitenantConstants.SUPER_TENANT_ID);

        WebFingerRequest webFingerRequest = defaultWebFingerRequestBuilder.buildRequest(request);
        assertNotNull(webFingerRequest);
        assertEquals(webFingerRequest.getResource(), acctResource);
        assertEquals(webFingerRequest.getRel(), rel);
     }

    @Test
    public  void testBuildRequest() throws WebFingerEndpointException, UserStoreException {
        Vector<String> vector = new Vector<>();
        vector.add(WebFingerConstants.REL);
        vector.add(WebFingerConstants.RESOURCE);
        Enumeration<String> params = vector.elements();
        when(request.getParameterNames()).thenReturn(params);
        when(request.getParameter(WebFingerConstants.RESOURCE)).thenReturn(resource);
        when(request.getParameter(WebFingerConstants.REL)).thenReturn(rel);
        when(tenantManager.getTenantId(any(String.class))).thenReturn(MultitenantConstants.SUPER_TENANT_ID);

        WebFingerRequest webFingerRequest = defaultWebFingerRequestBuilder.buildRequest(request);
        assertNotNull(webFingerRequest);
        assertEquals(webFingerRequest.getRel(), rel);
        assertEquals(webFingerRequest.getResource(), resource);
        assertEquals(webFingerRequest.getHost(), host);
        assertEquals(webFingerRequest.getTenant(), tenant);
        assertEquals(webFingerRequest.getPath(), path);
        assertEquals(webFingerRequest.getScheme(), scheme);
        assertEquals(webFingerRequest.getPort(), port);
    }

    @Test
    public void testInvalidTenant() throws Exception {
        when(tenantManager.getTenantId(any(String.class))).thenReturn(-1);

        try {
            DefaultWebFingerRequestBuilder.validateTenant("InvalidtenantDomain");
        } catch (WebFingerEndpointException e) {
            assertEquals(e.getErrorCode(), WebFingerConstants.ERROR_CODE_INVALID_RESOURCE);
        }
    }

    @Test
    public void testTenantException() throws Exception {
        when(tenantManager.getTenantId(any(String.class))).thenThrow(new UserStoreException());

        try {
            DefaultWebFingerRequestBuilder.validateTenant("InvalidtenantDomain");
        } catch (WebFingerEndpointException e) {
            assertEquals(e.getErrorCode(), WebFingerConstants.ERROR_CODE_INVALID_RESOURCE);
        }
    }

}
