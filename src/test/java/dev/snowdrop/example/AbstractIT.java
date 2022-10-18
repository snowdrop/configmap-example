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
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.LocalPortForward;
import io.fabric8.kubernetes.client.dsl.FilterWatchListDeletable;
import io.fabric8.kubernetes.client.internal.readiness.Readiness;

public abstract class AbstractIT {

    protected static final String CONFIG_MAP_NAME = "app-config";
    protected static final String GREETING_NAME = System.getProperty("app.name", "configmap");
    protected static final String GREETING_PATH = "api/greeting";

    @Test
    public void testConfigMapLifecycle() throws IOException {
        // Endpoint should say Hello at the beginning as ConfigMap must have been loaded before running the test
        verifyEndpoint("Hello");

        // Verify the name parameter is properly replaced in the greetings sentence.
        try (LocalPortForward appPort = kubernetesClient().services().withName(GREETING_NAME).portForward(8080)) {
            given().param("name", "John")
                    .get("http://localhost:" + appPort.getLocalPort() + "/" + GREETING_PATH)
                    .then()
                    .statusCode(200)
                    .body("content", is("Hello John from a ConfigMap!"));
        }

        // Verify the app is updated when the config map changes
        updateConfigMap();
        stopApplication();
        startApplication();
        verifyEndpoint("Bonjour");

        // Verify the app is updated when the config map is deleted
        deleteConfigMap();
        stopApplication();
        startApplication();
        await().atMost(5, TimeUnit.MINUTES)
                .ignoreExceptions()
                .untilAsserted(() -> {
                            try (LocalPortForward appPort = kubernetesClient().services().withName(GREETING_NAME).portForward(8080)) {
                                String message = given().get("http://localhost:" + appPort.getLocalPort() + "/" + GREETING_PATH + "/message")
                                        .then().extract().asString();
                                assertTrue(StringUtils.isEmpty(message));
                            }
                        }
                );
    }

    protected abstract KubernetesClient kubernetesClient();

    protected void stopApplication() {
        pods().delete();
    }

    private void verifyEndpoint(final String greeting) {
        await().atMost(5, TimeUnit.MINUTES)
                .ignoreExceptions()
                .untilAsserted(() -> {
                        try (LocalPortForward appPort = kubernetesClient().services().withName(GREETING_NAME).portForward(8080)) {
                            given().get("http://localhost:" + appPort.getLocalPort() + "/" + GREETING_PATH)
                                    .then()
                                    .statusCode(200)
                                    .body("content", is(String.format("%s World from a ConfigMap!", greeting)));
                        }
                    }
                );
    }

    private void updateConfigMap() {
        kubernetesClient().configMaps()
                .withName(CONFIG_MAP_NAME)
                .edit(c -> new ConfigMapBuilder(c)
                        .addToData("application.yml", "greeting.message: Bonjour %s from a ConfigMap!")
                        .build());
    }

    private void deleteConfigMap() {
        kubernetesClient().configMaps()
                .withName(CONFIG_MAP_NAME)
                .delete();
    }

    private void startApplication() {
        await().atMost(5, TimeUnit.MINUTES)
                .ignoreExceptions()
                .untilAsserted(
                        () -> assertEquals(1, pods().list().getItems().stream().filter(Readiness::isPodReady).count())
                );
    }

    private FilterWatchListDeletable<Pod, PodList> pods() {
        return kubernetesClient().pods().withLabel("app", "configmap");
    }

}
