/*
 * Copyright 2016-2017 Red Hat, Inc, and individual contributors.
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

package io.openshift.booster;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.internal.readiness.Readiness;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.Route;
import io.openshift.booster.test.OpenShiftTestAssistant;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OpenShiftIT {
    private static final OpenShiftTestAssistant assistant = new OpenShiftTestAssistant();
    private static final String CONFIG_MAP_NAME = "app-config";
    private static final String CONFIG_MAP_FILE = "target/test-classes/test-configmap.yml";
    private static final String GREETING_SERVICE_APP = "spring-boot-configmap-greeting";
    private static String greetingServiceURI;

    @BeforeClass
    public static void prepare() throws Exception {
        final String greetingServiceTemplate = System.getProperty("greetingServiceTemplate");
        if(greetingServiceTemplate == null) {
            throw new IllegalArgumentException("Greeting service template file location hasn't been provided! (Hint: usually in systemPropertyVariables of maven-failsafe-plugin configuration)");
        }
        greetingServiceURI = deployApp(GREETING_SERVICE_APP, greetingServiceTemplate);
        assistant.deploy(CONFIG_MAP_NAME, new File(CONFIG_MAP_FILE));

        await().atMost(5, TimeUnit.MINUTES).until(() -> {
            List<Pod> list = assistant.client().pods().inNamespace(assistant.project()).list().getItems();
            return !list.stream()
                    .filter(pod -> pod.getMetadata().getName().startsWith(GREETING_SERVICE_APP))
                    .filter(pod -> "running".equalsIgnoreCase(pod.getStatus().getPhase())).collect(Collectors.toList()).isEmpty();
        });

        waitForApp();

        // record URI of greeting service so that we can access it later
        greetingServiceURI += "/api/greeting";
    }

    @AfterClass
    public static void cleanup() {
        assistant.cleanup();
    }

    @Test
    public void testAGreetingEndpoint() {
        verifyEndpoint("Hello");
    }

    @Test
    public void testBGreetingEndpointWithNameParameter() {
        given().param("name", "John")
                .when()
                .get(greetingServiceURI)
                .then()
                .statusCode(200)
                .body("content", is("Hello John from a ConfigMap!"));
    }

    @Test
    public void testCConfigMapUpdate() {
        verifyEndpoint("Hello");
        updateConfigMap();
        rolloutChanges();
        waitForApp();
        verifyEndpoint("Bonjour");
    }

    @Test
    public void testDConfigMapNotPresent() {
        verifyEndpoint("Bonjour");
        deleteConfigMap();
        rolloutChanges();
        await().atMost(5, TimeUnit.MINUTES)
                .catchUncaughtExceptions()
                .until(() ->
                        get(greetingServiceURI).then()
                                .statusCode(500)
                );
    }

    private void verifyEndpoint(final String greeting) {
        when().get(greetingServiceURI)
                .then()
                .statusCode(200)
                .body("content", is(String.format("%s World from a ConfigMap!", greeting)));
    }

    private void updateConfigMap() {
        assistant.client()
                .configMaps()
                .withName(CONFIG_MAP_NAME)
                .edit()
                .addToData("application.properties", "greeting.message: Bonjour %s from a ConfigMap!")
                .done();
    }

    private void deleteConfigMap() {
        assistant.client()
                .configMaps()
                .withName(CONFIG_MAP_NAME)
                .delete();
    }

    private void rolloutChanges() {
        scale(0);
        scale(1);
    }

    private void scale(final int replicas) {
        assistant.client()
                .deploymentConfigs()
                .inNamespace(assistant.project())
                .withName(GREETING_SERVICE_APP)
                .scale(replicas);

        await().atMost(5, TimeUnit.MINUTES)
                .until(() -> {
                    // ideally, we'd look at deployment config's status.availableReplicas field,
                    // but that's only available since OpenShift 3.5
                    List<Pod> pods = assistant.client()
                            .pods()
                            .inNamespace(assistant.project())
                            .withLabel("deploymentconfig", GREETING_SERVICE_APP)
                            .list()
                            .getItems();
                    return pods.size() == replicas && pods.stream()
                            .allMatch(Readiness::isPodReady);
                });
    }

    private static void waitForApp() {
        await().atMost(5, TimeUnit.MINUTES)
                .until(() -> {
                    try {
                        final Response response = get(greetingServiceURI);
                        return response.getStatusCode() == 200;
                    } catch (final Exception e) {
                        return false;
                    }
                });
    }

    /**
     * @param name
     * @param templatePath
     * @return the app route
     * @throws IOException
     */
    private static String deployApp(String name, String templatePath) throws IOException {
        String appName;
        List<? extends HasMetadata> entities = assistant.deploy(name, new File(templatePath));

        Optional<String> first = entities.stream().filter(hm -> hm instanceof DeploymentConfig).map(hm -> (DeploymentConfig) hm)
                .map(dc -> dc.getMetadata().getName()).findFirst();
        if (first.isPresent()) {
            appName = first.get();
        } else {
            throw new IllegalStateException("Application deployment config not found");
        }
        Route route = assistant.client().routes().inNamespace(assistant.project()).withName(appName).get();
        assertThat(route).isNotNull();
        return "http://" + route.getSpec().getHost();
    }

}
