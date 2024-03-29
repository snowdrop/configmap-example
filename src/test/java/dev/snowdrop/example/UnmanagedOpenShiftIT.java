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

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.dekorate.testing.annotation.Inject;
import io.dekorate.testing.openshift.annotation.OpenshiftIntegrationTest;
import io.fabric8.kubernetes.client.KubernetesClient;

@EnabledIfSystemProperty(named = "unmanaged-test", matches = "true")
@OpenshiftIntegrationTest(deployEnabled = false, buildEnabled = false, pushEnabled = false)
public class UnmanagedOpenShiftIT extends AbstractIT {
    @Inject
    KubernetesClient kubernetesClient;

    @Override
    protected KubernetesClient kubernetesClient() {
        return kubernetesClient;
    }
}
