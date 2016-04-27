package com.amirov.jirareporter.jira;

import com.amirov.jirareporter.Reporter;
import com.amirov.jirareporter.RunnerParamsProvider;
import com.amirov.jirareporter.teamcity.TeamCityXMLParser;
import com.atlassian.jira.rest.client.IssueRestClient;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.Comment;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.Resolution;
import com.atlassian.jira.rest.client.domain.Transition;
import com.atlassian.jira.rest.client.domain.Version;
import com.atlassian.jira.rest.client.domain.input.TransitionInput;
import com.atlassian.jira.rest.client.domain.input.VersionInput;
import com.atlassian.jira.rest.client.domain.input.VersionInputBuilder;
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;
import jetbrains.buildServer.agent.BuildProgressLogger;
import org.joda.time.DateTime;

import java.net.URI;
import java.net.URISyntaxException;

public class JIRAClient {
    private static final NullProgressMonitor pm = new NullProgressMonitor();

    public static JiraRestClient getRestClient() {
        System.setProperty("jsse.enableSNIExtension", RunnerParamsProvider.sslConnectionIsEnabled());
        JerseyJiraRestClientFactory factory = new JerseyJiraRestClientFactory();
        URI jiraServerUri = null;
        try {
            jiraServerUri = new URI(RunnerParamsProvider.getJiraServerUrl());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return factory.createWithBasicHttpAuthentication(jiraServerUri, RunnerParamsProvider.getJiraUser(), RunnerParamsProvider.getJiraPassword());
    }

    public static JiraRestClient getRestClient(BuildProgressLogger myLogger) {
        System.setProperty("jsse.enableSNIExtension", RunnerParamsProvider.sslConnectionIsEnabled());
        JerseyJiraRestClientFactory factory = new JerseyJiraRestClientFactory();
        myLogger.message("JerseyJiraRestClientFactory factory = " + factory);
        URI jiraServerUri = null;
        try {
            jiraServerUri = new URI(RunnerParamsProvider.getJiraServerUrl());
            myLogger.message("jiraServerUri = " + jiraServerUri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            myLogger.exception(e);
        }
        myLogger.message("Jira User = " + RunnerParamsProvider.getJiraUser());
        return factory.createWithBasicHttpAuthentication(jiraServerUri, RunnerParamsProvider.getJiraUser(), RunnerParamsProvider.getJiraPassword());
    }

    public static Issue getIssue() {
        Issue issue = null;
        try {
            String issueKey = Reporter.getIssueKey();
            System.out.println("Reporter.getIssueKey() = " + issueKey);
            issue = getRestClient().getIssueClient().getIssue(issueKey, pm);
            System.out.println("issue = " + issue);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return issue;
    }

    public static Issue getIssue(BuildProgressLogger myLogger) {
        Issue issue = null;
        try {
            String issueKey = Reporter.getIssueKey();
            myLogger.message("Reporter.getIssueKey() = " + issueKey);
            final JiraRestClient restClient = getRestClient(myLogger);
            myLogger.message("restClient = " + restClient);
            final IssueRestClient issueClient = restClient.getIssueClient();
            myLogger.message("issueClient = " + issueClient);
            issue = issueClient.getIssue(issueKey, pm);
            myLogger.message("issue = " + issue);
        } catch (Exception e) {
            e.printStackTrace();
            myLogger.message("ERROR with getting the issue!!!" + e.getMessage());
            myLogger.exception(e);
        }
        return issue;
    }

    public static String getIssueStatus(){
        return getIssue().getStatus().getName();
    }

    private static Iterable<Transition> getTransitions (){
        return getRestClient().getIssueClient().getTransitions(getIssue().getTransitionsUri(), pm);
    }

    private static Transition getTransition(String transitionName){
        return getTransitionByName(getTransitions(), transitionName);
    }

    public static TransitionInput getTransitionInput(String transitionName){
        TeamCityXMLParser parser = new TeamCityXMLParser();
        return new TransitionInput(getTransition(transitionName).getId(), Comment.valueOf(parser.getTestResultText()));
    }

    private static Transition getTransitionByName(Iterable<Transition> transitions, String transitionName) {
        for (Transition transition : transitions) {
            if (transition.getName().equals(transitionName)) {
                return transition;
            }
        }
        return null;
    }

    public static Transition getTransitionByName(String transitionName) {
        Iterable<Transition> transitions = getTransitions();
        return getTransitionByName(transitions, transitionName);
    }

    private static Iterable<Resolution> getResolutions() {
        return getRestClient().getMetadataClient().getResolutions(pm);
    }

    public static Resolution getResolutionByName(String resolutionName) {
        Iterable<Resolution> resolutions = getResolutions();
        for (Resolution resolution : resolutions) {
            if (resolution.getName().equals(resolutionName)) {
                return resolution;
            }
        }
        return null;
    }

    private static Version createVersion(String versionName, String projectKey) {
        //create new version
        VersionInput versionInput = new VersionInputBuilder(projectKey)
                                            .setName(versionName)
                                            .setReleaseDate(new DateTime())
                                            .setReleased(true)
                                            .build();
        return getRestClient().getVersionRestClient().createVersion(versionInput, pm);
    }

    public static Version getVersion(String versionName) {
        String projectKey = getIssue().getProject().getKey();
        Iterable<Version> versions = getRestClient().getProjectClient().getProject(projectKey, pm).getVersions();
        for (Version version : versions) {
            if (version.getName().equals(versionName)) {
                return version;
            }
        }
        return createVersion(versionName, projectKey);

    }
}
