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
    private List<InputDataNode> inputNodes = new ArrayList<>();
    private List<DecisionNode> decisionNodes = new ArrayList<>();
    int numberOfQuestion = 0;

    @BeforeClass
    public static void init() {
        KieServices ks = KieServices.Factory.get();
        KieContainer kieContainer = KieHelper.getKieContainer(
                ks.newReleaseId("org.kie.dmn.demo", "decision-tree", "1.0"),
                ks.getResources().newClassPathResource("DynamicQuestioning.dmn", DecisionTreeTest.class));

        runtime = kieContainer.newKieSession().getKieRuntime(DMNRuntime.class);

        model = runtime.getModel("http://toepasbare-regels.omgevingswet.overheid.nl/00000001821699180000",
                "Brandveilig gebruik Dynamisch");
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
        assertTrue(result.getMessages().isEmpty());
        assertEquals("Vergunningvrij", result.getDecisionResultByName("Conclusie Dakkappel").getResult());

    }

    @Test
    public void testPartialDecision() {


        DMNContext context = DMNFactory.newContext();

        //scenario for Conclusie Dakkappel
//        context.set("Zijkant", false);
//        context.set("Achterkant", null);
//        context.set("Dakpannen rondom", null);
//        context.set("Dak helling", true);
//        context.set("Met dakleer", null);
        //context.set("Plat dak", null);

        context.set("Aanvraag wordt door melder zelf ingediend grondslag", false);
        context.set("Adres van de melder input", "address");
        context.set("Ligging van het bouwwerk input", "anything");
        context.set("Periode of tijdvakken beoogd gebruik input", null);
        context.set("Plattegrondtekening grondslag", true);
        context.set("Begindatum bouwerk input", null);
        context.set("Vaste waarde input", true);
        context.set("Voor tijdelijk of seizoensgebonden gebruik van een bouwwerk grondslag", false);
        context.set("In werkingsgebied HDSR grondslag", false);
        context.set("Hoogte van de hoogste vloer boven het maaiveld grondslag", null);
        context.set("Blusinstallatie met watermist grondslag", null);
        context.set("Naam van de melder input", "asdfsdadfd");
        context.set("Naam van de gemachtigde input", "naam");
        context.set("Adres van de gemachtigde input", "address");


        //model.getInputs().forEach(inputDataNode -> System.out.println(inputDataNode.getName()));


        DMNResult result = runtime.evaluateAll(model, context);
        DMNContext resultContext = result.getContext();
        String topLevelDecisionName = "Brandveilig gebruik";
        Boolean finalResult = (Boolean) resultContext.get(topLevelDecisionName);
        System.out.println(finalResult);
        //result.getMessages().forEach(System.out::println);
        if (finalResult == null) {
            getRequiredQuestions(topLevelDecisionName, resultContext);
        }

        System.out.println("Number of questions needed to be answered :" + numberOfQuestion);

    }

    private void getRequiredQuestions(String topLevelDecisionName, DMNContext resultContext) {
        DecisionNode decisionNodeConclusie = model.getDecisionByName(topLevelDecisionName);
        Decision decisionConclusie = decisionNodeConclusie.getDecision();
        if (decisionConclusie.getName().equals(topLevelDecisionName)) {
            decisionConclusie.getInformationRequirement().forEach(informationRequirement -> {
                getQuestionsFromDecisionOrInput(informationRequirement, resultContext);
            });
        }
    }

    private void getQuestionsFromDecisionOrInput(InformationRequirement informationRequirement, DMNContext resultContext) {
        if (informationRequirement.getRequiredDecision() != null) {
            DecisionNode decisionNode = model.getDecisionById(informationRequirement.getRequiredDecision().getHref().replace("#", ""));
            Object decisoonResult = resultContext.get(decisionNode.getName());
            if (decisoonResult == null) {
                Decision subDecision = decisionNode.getDecision();
                subDecision.getInformationRequirement().forEach(informationRequirement1 -> {
                    getQuestionsFromDecisionOrInput(informationRequirement1, resultContext);
                });
            }
        }
        if (informationRequirement.getRequiredInput() != null) {
            String requiredInput = informationRequirement.getRequiredInput() != null ? informationRequirement.getRequiredInput().getHref().replace("#", "") : null;
            InputDataNode inputDataNode = model.getInputById(requiredInput);
            numberOfQuestion++;
            System.out.println(numberOfQuestion+". This question needs to be  answered : " + inputDataNode.getName());
        }
    }

    @Test
    public void testEvaluateDecisionWithSubDecisionAndInputs() {
        DMNContext context = DMNFactory.newContext();
        context.set("Plaatsing correct", true);
        context.set("Dakpannen rondom", true);
        context.set("Plat dak", true);

        DMNResult result = runtime.evaluateAll(model, context);
        assertTrue(result.getMessages().isEmpty());
        assertEquals("Vergunningvrij", result.getDecisionResultByName("Conclusie Dakkappel").getResult());
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
        for (InformationRequirement infoReq : infoReqs) {
            DMNNode node = getInfoReq(infoReq);
            if (node instanceof DecisionNode) {
                decisions.add((DecisionNode) node);
            } else {
                inputData.add((InputDataNode) node);
            }
        }
        assertEquals(decisions.size(), 2);
        assertTrue(inputData.isEmpty());
    }

    private DMNNode getInfoReq(InformationRequirement infoReq) {
        DMNNode node = null;
        if (infoReq.getRequiredInput() != null) {
            node = model.getInputById(infoReq.getRequiredInput().getHref().substring(1));
        } else if (infoReq.getRequiredDecision() != null) {
            node = model.getDecisionById(infoReq.getRequiredDecision().getHref().substring(1));
        }
        return node;
    }

}
