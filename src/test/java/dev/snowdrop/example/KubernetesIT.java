/*
 * Copyright 2021 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.snowdrop.example;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.dekorate.testing.annotation.Inject;
import io.dekorate.testing.annotation.KubernetesIntegrationTest;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.LocalPortForward;

@Disabled("It needs Dekorate profiles to configure. See https://github.com/dekorateio/dekorate/pull/824")
@KubernetesIntegrationTest
public class KubernetesIT {

    private static final String GREETING_PATH = "api/greeting";

    @Inject
    KubernetesClient kubernetesClient;

    LocalPortForward appPort;

    @BeforeEach
    public void setup() {
        appPort = kubernetesClient.pods().withName("configmap").portForward(8080);
    }

    @AfterEach
    public void tearDown() throws IOException {
        if (appPort != null) {
            appPort.close();
        }
    }

    @Test
    public void testConfigMap() {
        // Endpoint should say Hello World at the beginning as ConfigMap must have been loaded before running the test
        given().get(baseURL() + GREETING_PATH)
                .then()
                .statusCode(200)
                .body("content", is("Hello World from a ConfigMap!"));
    }

    private String baseURL() {
        return "http://localhost:" + appPort.getLocalPort() + "/";
    }
}
