package io.syndesis.qe.templates;

import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.ServiceSpecBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IrcTemplate {
    private static final String LABEL_NAME = "app";
    private static final String SERVER_APP_NAME = "irc-server";
    public static final String CONTROLLER_APP_NAME = "irc-controller";

    private static final int IRC_PORT = 6667;
    private static final int CONTROLLER_PORT = 8080;

    public static void deploy() {
        if (!TestUtils.isDcDeployed("irc-server")) {
            deployIrcServer();
        }

        if (!TestUtils.isDcDeployed("irc-controller")) {
            deployIrcController();
        }

        addAccounts();
    }

    private static void deployIrcServer() {
        List<ContainerPort> ports = new LinkedList<>();
        ports.add(new ContainerPortBuilder()
            .withName(SERVER_APP_NAME)
            .withContainerPort(IRC_PORT)
            .withProtocol("TCP").build());

        OpenShiftUtils.getInstance().deploymentConfigs().createOrReplaceWithNew()
            .editOrNewMetadata()
            .withName(SERVER_APP_NAME)
            .addToLabels(LABEL_NAME, SERVER_APP_NAME)
            .endMetadata()

            .editOrNewSpec()
            .addToSelector(LABEL_NAME, SERVER_APP_NAME)
            .withReplicas(1)
            .editOrNewTemplate()
            .editOrNewMetadata()
            .addToLabels(LABEL_NAME, SERVER_APP_NAME)
            .endMetadata()
            .editOrNewSpec()
            .addNewContainer().withName(SERVER_APP_NAME).withImage("syndesisqe/irc:latest").addAllToPorts(ports)

            .endContainer()
            .endSpec()
            .endTemplate()
            .addNewTrigger()
            .withType("ConfigChange")
            .endTrigger()
            .endSpec()
            .done();

        ServiceSpecBuilder serviceSpecBuilder = new ServiceSpecBuilder().addToSelector(LABEL_NAME, SERVER_APP_NAME);

        serviceSpecBuilder.addToPorts(new ServicePortBuilder()
            .withName(SERVER_APP_NAME)
            .withPort(IRC_PORT)
            .withTargetPort(new IntOrString(IRC_PORT))
            .build());

        OpenShiftUtils.getInstance().services().createOrReplaceWithNew()
            .editOrNewMetadata()
            .withName(SERVER_APP_NAME)
            .addToLabels(LABEL_NAME, SERVER_APP_NAME)
            .endMetadata()
            .editOrNewSpecLike(serviceSpecBuilder.build())
            .endSpec()
            .done();

        try {
            OpenShiftWaitUtils.waitFor(OpenShiftWaitUtils.areExactlyNPodsReady(LABEL_NAME, SERVER_APP_NAME, 1));
            Thread.sleep(20 * 1000);
        } catch (InterruptedException | TimeoutException e) {
            log.error("Wait for {} deployment failed ", SERVER_APP_NAME, e);
        }
    }

    private static void deployIrcController() {
        List<ContainerPort> ports = new LinkedList<>();
        ports.add(new ContainerPortBuilder()
            .withName(CONTROLLER_APP_NAME)
            .withContainerPort(CONTROLLER_PORT)
            .withProtocol("TCP").build());

        List<EnvVar> envVars = new ArrayList<>();
        envVars.add(new EnvVar("HOST", SERVER_APP_NAME, null));

        OpenShiftUtils.getInstance().deploymentConfigs().createOrReplaceWithNew()
            .editOrNewMetadata()
            .withName(CONTROLLER_APP_NAME)
            .addToLabels(LABEL_NAME, CONTROLLER_APP_NAME)
            .endMetadata()

            .editOrNewSpec()
            .addToSelector(LABEL_NAME, CONTROLLER_APP_NAME)
            .withReplicas(1)
            .editOrNewTemplate()
            .editOrNewMetadata()
            .addToLabels(LABEL_NAME, CONTROLLER_APP_NAME)
            .endMetadata()
            .editOrNewSpec()
            .addNewContainer().withName(CONTROLLER_APP_NAME).withImage("syndesisqe/irc-controller:latest").addAllToPorts(ports).addAllToEnv(envVars)

            .endContainer()
            .endSpec()
            .endTemplate()
            .addNewTrigger()
            .withType("ConfigChange")
            .endTrigger()
            .endSpec()
            .done();

        ServiceSpecBuilder serviceSpecBuilder = new ServiceSpecBuilder().addToSelector(LABEL_NAME, CONTROLLER_APP_NAME);

        serviceSpecBuilder.addToPorts(new ServicePortBuilder()
            .withName(CONTROLLER_APP_NAME)
            .withPort(CONTROLLER_PORT)
            .withTargetPort(new IntOrString(CONTROLLER_PORT))
            .build());

        OpenShiftUtils.getInstance().services().createOrReplaceWithNew()
            .editOrNewMetadata()
            .withName(CONTROLLER_APP_NAME)
            .addToLabels(LABEL_NAME, CONTROLLER_APP_NAME)
            .endMetadata()
            .editOrNewSpecLike(serviceSpecBuilder.build())
            .endSpec()
            .done();

        OpenShiftUtils.getInstance().routes().createOrReplaceWithNew()
            .withNewMetadata()
            .withName(CONTROLLER_APP_NAME)
            .endMetadata()
            .withNewSpec()
            .withPath("/")
            .withWildcardPolicy("None")
            .withNewTls()
            .withTermination("edge")
            .withInsecureEdgeTerminationPolicy("Allow")
            .endTls()
            .withNewTo()
            .withKind("Service").withName(CONTROLLER_APP_NAME)
            .endTo()
            .endSpec()
            .done();

        try {
            OpenShiftWaitUtils.waitFor(OpenShiftWaitUtils.areExactlyNPodsReady(LABEL_NAME, CONTROLLER_APP_NAME, 1));
            Thread.sleep(20 * 1000);
        } catch (InterruptedException | TimeoutException e) {
            log.error("Wait for {} deployment failed ", CONTROLLER_APP_NAME, e);
        }
    }

    private static void addAccounts() {
        Account irc = new Account();
        Map<String, String> params = new HashMap<>();
        params.put("hostname", "irc-server");
        params.put("port", "6667");
        irc.setService("irc");
        irc.setProperties(params);
        AccountsDirectory.getInstance().getAccounts().put("irc", irc);
    }
}
