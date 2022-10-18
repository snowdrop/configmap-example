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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ScalableResource;

public abstract class AbstractIT {

    protected static final String CONFIG_MAP_NAME = "app-config";
    protected static final String GREETING_NAME = System.getProperty("app.name", "configmap");
    protected static final String GREETING_PATH = "api/greeting";

    @Test
    public void testConfigMapLifecycle() {
        // Endpoint should say Hello at the beginning as ConfigMap must have been loaded before running the test
        verifyEndpoint("Hello");

        // Verify the name parameter is properly replaced in the greetings sentence.
        given().param("name", "John")
                .when()
                .get(baseURL() + GREETING_PATH)
                .then()
                .statusCode(200)
                .body("content", is("Hello John from a ConfigMap!"));

        // Verify the app is updated when the config map changes
        updateConfigMap();
        rolloutChanges();
        waitForApp();
        verifyEndpoint("Bonjour");

        // Verify the app is updated when the config map is deleted
        deleteConfigMap();
        rolloutChanges();
        await().atMost(5, TimeUnit.MINUTES)
                .catchUncaughtExceptions()
                .untilAsserted(() -> given().get(baseURL() + GREETING_PATH)
                        .then().statusCode(500));
    }

    protected abstract String baseURL();
    protected abstract KubernetesClient kubernetesClient();
    protected abstract ScalableResource<?> deployment();

    private void verifyEndpoint(final String greeting) {
        given().get(baseURL() + GREETING_PATH)
                .then()
                .statusCode(200)
                .body("content", is(String.format("%s World from a ConfigMap!", greeting)));
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

    private void rolloutChanges() {
        scale(0);
        scale(1);
    }

    private void scale(int replicas) {
        ScalableResource<?> deployment = deployment();
        assertNotNull(deployment, "Deployment with name '" + GREETING_NAME + "' not found!");
        deployment.scale(replicas);

        await().atMost(5, TimeUnit.MINUTES)
                .until(() -> {
                    ScalableResource<?> updatedDeployment = deployment();
                    return updatedDeployment != null && updatedDeployment.scale().getStatus().getReplicas() == replicas;
                });
    }

    private void waitForApp() {
        await().atMost(5, TimeUnit.MINUTES)
                .ignoreExceptions()
                .untilAsserted(
                        () -> given()
                                .get(baseURL() + GREETING_PATH)
                                .then().statusCode(200)
                );
    }

}
