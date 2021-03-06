/*
 * Copyright 2015 - 2016 Nebula Bay.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tascape.qa.th.test;

import com.tascape.qa.th.driver.JacocoClient;
import com.tascape.qa.th.driver.TestDriver;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic Jacoco exec data collection case.
 *
 * @author linsong wang
 */
public class JacocoTest extends AbstractTest {
    private static final Logger LOG = LoggerFactory.getLogger(JacocoTest.class);

    public static final TestDriver JACOCO = new TestDriver(JacocoTest.class, JacocoClient.class);

    private final JacocoClient jacocoClient;

    public JacocoTest() {
        this.jacocoClient = super.getEntityDriver(JACOCO);
    }

    @Before
    public void setUp() throws Exception {
        LOG.debug("Run something before test case");
        LOG.debug("Please override");
    }

    @After
    public void tearDown() throws Exception {
        LOG.debug("Run something after test case");
        LOG.debug("Please override");
    }

    @Override
    public String getApplicationUnderTest() {
        LOG.debug("Please override");
        return "testharness";
    }
}
