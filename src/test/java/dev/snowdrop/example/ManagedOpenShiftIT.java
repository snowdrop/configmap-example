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

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.dekorate.testing.annotation.Inject;
import io.dekorate.testing.openshift.annotation.OpenshiftIntegrationTest;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.OpenShiftClient;

@DisabledIfSystemProperty(named = "unmanaged-test", matches = "true")
@OpenshiftIntegrationTest
public class ManagedOpenShiftIT extends AbstractOpenShiftIT {
    @Inject
    KubernetesClient kubernetesClient;

    String baseUrl;

    @BeforeEach
    public void setup() throws MalformedURLException {
        // TODO: In Dekorate 1.7, we can inject Routes directly, so we won't need to do this:
        Route route = kubernetesClient.adapt(OpenShiftClient.class).routes().withName("configmap").get();
        String protocol = route.getSpec().getTls() == null ? "http" : "https";
        int port = "http".equals(protocol) ? 80 : 443;
        URL url = new URL(protocol, route.getSpec().getHost(), port, route.getSpec().getPath());
        baseUrl = url.toString();
    }

    @Override
    protected String baseURL() {
        return baseUrl;
    }

    @Override
    protected KubernetesClient kubernetesClient() {
        return kubernetesClient;
    }
}
