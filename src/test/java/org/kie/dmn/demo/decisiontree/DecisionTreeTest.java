/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.dmn.demo.decisiontree;

import org.junit.Test;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.core.api.DMNFactory;
import org.kie.dmn.core.util.DMNRuntimeUtil;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class DecisionTreeTest {

    @Test
    public void testDecisionContext() {

        DMNRuntime runtime = DMNRuntimeUtil.createRuntime("Conclusie_Dakkapel.dmn", this.getClass());
        DMNModel model = runtime.getModel("http://www.trisotech.com/dmn/definitions/_d523bd18-c840-4a7d-bffb-41cb1c83062a",
                "Conclusie Dakkapel");

        assertNotNull(model);

        DMNContext context = DMNFactory.newContext();
        context.set("Zijkant", true);
        context.set("Achterkant", true);
        context.set("Dakpannen rondom", true);
        context.set("Plat dak", true);

        DMNResult result = runtime.evaluateAll(model, context);
        assertThat( DMNRuntimeUtil.formatMessages( result.getMessages() ), result.hasErrors(), is( false ) );
        assertFalse(result.getDecisionResults().isEmpty());

    }

    @Test
    public void testPartialDecision() {

        DMNRuntime runtime = DMNRuntimeUtil.createRuntime("Conclusie_Dakkapel.dmn", this.getClass());
        DMNModel model = runtime.getModel("http://www.trisotech.com/dmn/definitions/_d523bd18-c840-4a7d-bffb-41cb1c83062a",
                "Conclusie Dakkapel");

        assertNotNull(model);

        DMNContext context = DMNFactory.newContext();

        context.set("Zijkant", true);
        context.set("Achterkant", true);

        DMNResult result = runtime.evaluateDecisionByName(model, "Plaatsing correct", context);
        assertThat(DMNRuntimeUtil.formatMessages( result.getMessages() ), result.hasErrors(), is( false ));
        assertEquals(result.getDecisionResultByName("Plaatsing correct").getResult(), true);

    }

    @Test
    public void testDecisionsOnly() {
        DMNRuntime runtime = DMNRuntimeUtil.createRuntime("Conclusie_Dakkapel.dmn", this.getClass());
        DMNModel model = runtime.getModel("http://www.trisotech.com/dmn/definitions/_d523bd18-c840-4a7d-bffb-41cb1c83062a",
                "Conclusie Dakkapel");

        assertNotNull(model);

        DMNContext context = DMNFactory.newContext();

        context.set("Plaatsing correct", true);
        context.set("Toepassing correct", true);

        DMNResult result = runtime.evaluateDecisionByName(model, "Conclusie Dakkappel", context);

        assertThat(DMNRuntimeUtil.formatMessages( result.getMessages() ), result.hasErrors(), is( false ));
        assertEquals(result.getDecisionResultByName("Conclusie Dakkappel").getResult(), "Vergunningvrij");

    }

}
