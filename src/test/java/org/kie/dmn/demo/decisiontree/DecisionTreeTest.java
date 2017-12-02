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

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.api.core.ast.DMNNode;
import org.kie.dmn.api.core.ast.DecisionNode;
import org.kie.dmn.api.core.ast.InputDataNode;
import org.kie.dmn.core.api.DMNFactory;
import org.kie.dmn.core.util.KieHelper;
import org.kie.dmn.model.v1_1.Decision;
import org.kie.dmn.model.v1_1.InformationRequirement;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class DecisionTreeTest {

    private static DMNRuntime runtime;
    private static DMNModel model;

    @BeforeClass
    public static void init() {
        KieServices ks = KieServices.Factory.get();
        KieContainer kieContainer = KieHelper.getKieContainer(
                ks.newReleaseId("org.kie.dmn.demo", "decision-tree", "1.0"),
                ks.getResources().newClassPathResource("Conclusie_Dakkapel.dmn", DecisionTreeTest.class));

        runtime = kieContainer.newKieSession().getKieRuntime(DMNRuntime.class);

        model = runtime.getModel("http://www.trisotech.com/dmn/definitions/_d523bd18-c840-4a7d-bffb-41cb1c83062a",
                "Conclusie Dakkapel");
        assertNotNull(model);
    }

    @Test
    public void testDecisionContext() {

        DMNContext context = DMNFactory.newContext();
        context.set("Zijkant", true);
        context.set("Achterkant", true);
        context.set("Dakpannen rondom", true);
        context.set("Plat dak", true);

        DMNResult result = runtime.evaluateAll(model, context);
        assertFalse(result.getDecisionResults().isEmpty());

    }

    @Test
    public void testPartialDecision() {


        DMNContext context = DMNFactory.newContext();

        context.set("Zijkant", true);
        context.set("Achterkant", true);

        DMNResult result = runtime.evaluateDecisionByName(model, "Plaatsing correct", context);
        assertEquals(result.getDecisionResultByName("Plaatsing correct").getResult(), true);

    }

    @Test
    public void testDecisionsOnly() {

        DMNContext context = DMNFactory.newContext();

        context.set("Plaatsing correct", true);
        context.set("Toepassing correct", true);

        DMNResult result = runtime.evaluateDecisionByName(model, "Conclusie Dakkappel", context);

        assertEquals(result.getDecisionResultByName("Conclusie Dakkappel").getResult(), "Vergunningvrij");

    }

    @Test
    public void testDecisionInfoReqs() {
        DecisionNode decisionNode = model.getDecisionByName("Conclusie Dakkappel");
        List<InformationRequirement> infoReqs = decisionNode.getDecision().getInformationRequirement();
        List<DecisionNode> decisions = new ArrayList<>();
        List<InputDataNode> inputData = new ArrayList<>();
        for(InformationRequirement infoReq : infoReqs) {
            DMNNode node = getInfoReq(infoReq);
            if(node instanceof DecisionNode) {
                decisions.add((DecisionNode) node);
            } else {
                inputData.add((InputDataNode)node);
            }
        }
        assertEquals(decisions.size(), 2);
        assertTrue(inputData.isEmpty());
    }

    private DMNNode getInfoReq(InformationRequirement infoReq) {
        DMNNode node = null;
        if(infoReq.getRequiredInput() != null) {
            node = model.getInputById(infoReq.getRequiredInput().getHref().substring(1));
        } else if (infoReq.getRequiredDecision() != null) {
            node = model.getDecisionById(infoReq.getRequiredDecision().getHref().substring(1));
        }
        return node;
    }

}
