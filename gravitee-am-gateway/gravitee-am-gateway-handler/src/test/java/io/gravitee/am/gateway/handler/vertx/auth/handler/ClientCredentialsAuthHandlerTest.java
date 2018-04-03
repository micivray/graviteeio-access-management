/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.am.gateway.handler.vertx.auth.handler;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.shiro.ShiroAuth;
import io.vertx.ext.auth.shiro.ShiroAuthRealmType;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.ext.web.handler.AuthHandlerTestBase;
import org.junit.Test;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class ClientCredentialsAuthHandlerTest extends AuthHandlerTestBase {

    @Test
    public void shouldLoginSuccess() throws Exception {
        Handler<RoutingContext> handler = rc -> {
            assertNotNull(rc.user());
            assertEquals("my-client", rc.user().principal().getString("username"));
            rc.response().end();
        };

        JsonObject authConfig = new JsonObject().put("properties_path", "classpath:client/clientusers.properties");
        AuthProvider authProvider = ShiroAuth.create(vertx, ShiroAuthRealmType.PROPERTIES, authConfig);

        router.route("/token/*")
                .handler(ClientCredentialsAuthHandler.create(authProvider))
                .handler(handler);

        testRequest(HttpMethod.GET, "/token/", 401, "Unauthorized");

        // Now try again with credentials
        testRequest(HttpMethod.GET, "/token?client_id=my-client&client_secret=my-secret", resp -> {
            String wwwAuth = resp.headers().get("WWW-Authenticate");
            assertNull(wwwAuth);
        }, 200, "OK", null);
    }

    @Override
    protected AuthHandler createAuthHandler(AuthProvider authProvider) {
        return ClientCredentialsAuthHandler.create(authProvider);
    }
}