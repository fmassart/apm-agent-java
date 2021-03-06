/*-
 * #%L
 * Elastic APM Java agent
 * %%
 * Copyright (C) 2018 - 2020 Elastic and contributors
 * %%
 * Licensed to Elasticsearch B.V. under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch B.V. licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * #L%
 */
package co.elastic.apm.agent.bci;

import co.elastic.apm.agent.AbstractInstrumentationTest;
import net.bytebuddy.dynamic.loading.ClassInjector;
import org.junit.jupiter.api.Test;
import org.stagemonitor.util.IOUtils;

import java.io.InputStream;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class IndyBootstrapTest extends AbstractInstrumentationTest {

    @Test
    void testSetJavaBaseModule() throws Throwable {
        Module javaBaseModule = Class.class.getModule();
        assertThat(IndyBootstrapTest.class.getModule()).isNotEqualTo(javaBaseModule);

        // In order to test this functionality, IndyBootstrapDispatcherModuleSetter needs to be loaded from the Boot CL.
        // We don't mind loading it with the test's class loader as well only to get it's class file
        InputStream classFileAsStream = IndyBootstrapDispatcherModuleSetter.class.getResourceAsStream("IndyBootstrapDispatcherModuleSetter.class");
        byte[] bootstrapClass = IOUtils.readToBytes(classFileAsStream);
        ClassInjector.UsingUnsafe.ofBootLoader().injectRaw(Collections.singletonMap(IndyBootstrapDispatcherModuleSetter.class.getName(), bootstrapClass));

        IndyBootstrap.setJavaBaseModule(IndyBootstrapTest.class);
        assertThat(IndyBootstrapTest.class.getModule()).isEqualTo(javaBaseModule);
    }
}
