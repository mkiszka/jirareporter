package com.amirov.jirareporter;

import com.amirov.jirareporter.jira.JIRAConfig;
import com.amirov.jirareporter.teamcity.TeamCityXMLParser;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.Comment;
import com.atlassian.jira.rest.client.domain.Resolution;
import com.atlassian.jira.rest.client.domain.Transition;
import com.atlassian.jira.rest.client.domain.Version;
import com.atlassian.jira.rest.client.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.domain.input.FieldInput;
import com.atlassian.jira.rest.client.domain.input.TransitionInput;
import com.fasterxml.jackson.databind.ObjectMapper;
import jetbrains.buildServer.agent.BuildProgressLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.amirov.jirareporter.jira.JIRAClient.*;

public class Reporter {
    private static String issueKey;
    private BuildProgressLogger myLogger;
    private TeamCityXMLParser parser = new TeamCityXMLParser();
    private static final ObjectMapper mapper = new ObjectMapper();


    public Reporter(BuildProgressLogger logger){
        myLogger = logger;
    }

    public void report(String issueKeyString){
        issueKey = issueKeyString;
        myLogger.message("\nISSUE: " + issueKeyString
                + "\nTitle: " + getIssue().getSummary()
                + "\nDescription: " + getIssue().getDescription());
        NullProgressMonitor pm = new NullProgressMonitor();
        getRestClient().getIssueClient().addComment(pm, getIssue().getCommentsUri(), Comment.valueOf(parser.getTestResultText()));
    }

    public void progressIssue() {
        NullProgressMonitor pm = new NullProgressMonitor();
        if(RunnerParamsProvider.progressIssueIsEnable() == null){}
        else if(RunnerParamsProvider.progressIssueIsEnable().equals("true")){
            String transitionName = JIRAConfig.prepareJiraWorkflow(parser.getStatusBuild()).get(getIssueStatus());
            if (transitionName != null) {
                //Get Transition
                Transition transition = getTransitionByName(transitionName);

                //Create New Field Input Updates
                Resolution fixedResolution = getResolutionByName("Fixed");
                Map resMap = Collections.EMPTY_MAP;
                try {
                    String resolutionString = mapper.writeValueAsString(fixedResolution);
                    resMap = mapper.readValue(resolutionString, HashMap.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ComplexIssueInputFieldValue complexIssueInputFieldValue = new ComplexIssueInputFieldValue(resMap);

                //Get Jira Version
                Map<String, Object> versionMap = new HashMap<>();
                Version version = getVersion(parser.getReleasedPomVersionString());
                versionMap.put("id", String.valueOf(version.getId()));
                versionMap.put("name", version.getName());
                ComplexIssueInputFieldValue versionComplexIssueInputFieldValue = new ComplexIssueInputFieldValue(versionMap);

                List<ComplexIssueInputFieldValue> fixVersionsComplex = new ArrayList<>();
                fixVersionsComplex.add(versionComplexIssueInputFieldValue);

                Collection<FieldInput> fieldInputs = Arrays.asList(new FieldInput("resolution", complexIssueInputFieldValue), new FieldInput("fixVersions", fixVersionsComplex));
                //Create final transition input to ship across the wire.
                final TransitionInput resolvedTransitionInput = new TransitionInput(transition.getId(), fieldInputs, Comment.valueOf("This issue was released and closed via TeamCity Plugin."));
                //SHIP IT!!!
                getRestClient().getIssueClient().transition(getIssue().getTransitionsUri(), resolvedTransitionInput, pm);

                myLogger.message(issueKey + " has been transitioned to Closed with resolution Fixed and Fix Version of " + version.getName());
            }
        }
    }

    public static String getIssueKey(){
        return issueKey;
    }
}
