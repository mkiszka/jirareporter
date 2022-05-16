package com.amirov.jirareporter.jira;

import com.amirov.jirareporter.RunnerParamsProvider;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.Comment;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.Resolution;
import com.atlassian.jira.rest.client.domain.Transition;
import com.atlassian.jira.rest.client.domain.input.TransitionInput;
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;
import com.sun.jersey.client.apache.ApacheHttpClient;


import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;

public class JIRAClient
{
    private static final NullProgressMonitor NPM = new NullProgressMonitor();
    private final JiraRestClient _client;
    private JerseyRemoteLinkRestClient _remoteLinkClient;

    public JIRAClient(RunnerParamsProvider prmsProvider) throws URISyntaxException
    {
        System.setProperty("jsse.enableSNIExtension", Boolean.toString(!prmsProvider.isSslConnectionEnabled()));
        JerseyJiraRestClientFactory factory = new JerseyJiraRestClientFactory();
        URI jiraServerUri = new URI(prmsProvider.getJiraServerUrl());

        if (prmsProvider.getJiraWindowsAuth())
            _client = factory.create(jiraServerUri, new NTLMAuthenticationHandler(prmsProvider));
        else _client = factory.createWithBasicHttpAuthentication(jiraServerUri, prmsProvider.getJiraUser(), prmsProvider.getJiraPassword());

        if (prmsProvider.isLinkToBuildPageEnabled())
        {
            try
            {
                Object o = _client.getIssueClient();
                Class c = o.getClass().getSuperclass();
                Field field1 = c.getDeclaredField("client");
                Field field2 = c.getDeclaredField("baseUri");
                field1.setAccessible(true);
                field2.setAccessible(true);
                _remoteLinkClient = new JerseyRemoteLinkRestClient((URI) field2.get(o), (ApacheHttpClient) field1.get(o));
            }
            catch (Exception ex)
            {
                prmsProvider.getLogger().warning("Unable to initialize RemoteLinkClient. Links won't be created. " + ex.getMessage());
            }
        }
    }

    public Issue getIssue(String issueId)
    {
        return  _client.getIssueClient().getIssue(issueId, NPM);
    }

    public void addComment(Issue issue, String comment)
    {
        _client.getIssueClient().addComment(NPM, issue.getCommentsUri(), Comment.valueOf(comment));
    }

    public void makeLink(Issue issue, String url, String title)
    {
        _remoteLinkClient.makeRemoteLink(issue.getKey(), url, title);
    }


    public String getIssueStatus(String issueId){
        return getIssue(issueId).getStatus().getName();
    }

    private Iterable<Transition> getTransitions (String issueId){
        return _client.getIssueClient().getTransitions(getIssue(issueId).getTransitionsUri(), NPM);
    }
/*
    private Transition getTransition(String transitionName){
        return getTransitionByName(getTransitions(), transitionName);
    }*/
  /*
    public static TransitionInput getTransitionInput(String transitionName){
        TeamCityXMLParser parser = new TeamCityXMLParser();
        return new TransitionInput(getTransition(transitionName).getId(), Comment.valueOf(parser.getTestResultText()));
    }
*/
    private Transition getTransitionByName(Iterable<Transition> transitions, String transitionName) {
        for (Transition transition : transitions) {
            if (transition.getName().equals(transitionName)) {
                return transition;
            }
        }
        return null;
    }

    public Transition getTransitionByName(String issueId, String transitionName) {
        Iterable<Transition> transitions = getTransitions(issueId);
        return getTransitionByName(transitions, transitionName);
    }

    private Iterable<Resolution> getResolutions() {
        return _client.getMetadataClient().getResolutions(NPM);
    }

    public Resolution getResolutionByName(String resolutionName) {
        Iterable<Resolution> resolutions = getResolutions();
        for (Resolution resolution : resolutions) {
            if (resolution.getName().equals(resolutionName)) {
                return resolution;
            }
        }
        return null;
    }
/*
    private static Version createVersion(String versionName, String projectKey) {
        //create new version
        VersionInput versionInput = new VersionInputBuilder(projectKey)
                                            .setName(versionName)
                                            .setReleaseDate(new DateTime())
                                            .setReleased(true)
                                            .build();
        return getRestClient().getVersionRestClient().createVersion(versionInput, NPM);
    }

    public static Version getVersion(String versionName) {
        String projectKey = getIssue().getProject().getKey();
        Iterable<Version> versions = getRestClient().getProjectClient().getProject(projectKey, NPM).getVersions();
        for (Version version : versions) {
            if (version.getName().equals(versionName)) {
                return version;
            }
        }
        return createVersion(versionName, projectKey);
    }
    */

    public void transition(String issueId, TransitionInput transitionInput) {
        _client.getIssueClient().transition(getIssue(issueId),transitionInput,NPM);
    }
}
