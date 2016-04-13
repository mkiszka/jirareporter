package com.amirov.jirareporter;

import com.amirov.jirareporter.jira.JIRAConfig;
import com.amirov.jirareporter.teamcity.TeamCityXMLParser;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.Comment;
import com.atlassian.jira.rest.client.domain.Field;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.Resolution;
import com.atlassian.jira.rest.client.domain.Transition;
import com.atlassian.jira.rest.client.domain.Version;
import com.atlassian.jira.rest.client.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.domain.input.FieldInput;
import com.atlassian.jira.rest.client.domain.input.TransitionInput;
import com.atlassian.jira.rest.client.domain.input.VersionInput;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jetbrains.buildServer.agent.BuildProgressLogger;
import org.joda.time.DateTime;

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
    private static String issueId;
    private BuildProgressLogger myLogger;
    private TeamCityXMLParser parser = new TeamCityXMLParser();
    private static final ObjectMapper mapper = new ObjectMapper();


    public Reporter(BuildProgressLogger logger){
        myLogger = logger;
    }

    public void report(String issue){
        issueId = issue;
        myLogger.message("\nISSUE: " + issue
                + "\nTitle: " + getIssue().getSummary()
                + "\nDescription: " + getIssue().getDescription());
        NullProgressMonitor pm = new NullProgressMonitor();
        getRestClient().getIssueClient().addComment(pm, getIssue().getCommentsUri(), Comment.valueOf(parser.getTestResultText()));
    }

    public void progressIssue(){
        String releasedVersion = parser.getReleasedPomVersionString();
        NullProgressMonitor pm = new NullProgressMonitor();
        if(RunnerParamsProvider.progressIssueIsEnable() == null){}
        else if(RunnerParamsProvider.progressIssueIsEnable().equals("true")){
            String transitionName = JIRAConfig.prepareJiraWorkflow(parser.getStatusBuild()).get(getIssueStatus());
            if (transitionName != null) {
                System.out.println("transitionName = " + transitionName);
                //create new version
                Issue issue = getIssue();
                DateTime dateTime = new DateTime();
                VersionInput versionInput = new VersionInput(issue.getProject().getKey(), releasedVersion, null, dateTime, false, true);
                Version version = getRestClient().getVersionRestClient().createVersion(versionInput, pm);

                System.out.println("********* ISSUE FIELDS ************");
                for (Field field : issue.getFields()) {
                    System.out.println(field.toString());
                }
                System.out.println("********* END ISSUE FIELDS ************");

                //Get Transition
                Transition transition = getTransitionByName(transitionName);
                System.out.println("Got the transition!!! " + transition.toString());
                System.out.println("Transition id = " + transition.getId());

                System.out.println("********* TRANSITION FIELDS ************");
                for (Transition.Field field : transition.getFields()) {
                    System.out.println(field.toString());
                }
                System.out.println("********* END TRANSITION FIELDS ************");


                System.out.println("********* RESOLUTIONS ************");
                for (Resolution resolution : getRestClient().getMetadataClient().getResolutions(pm)) {
                    System.out.println(resolution.toString());
                }
                System.out.println("********* END RESOLUTIONS ************");

/*

                Field trigger = issue.getFieldByName("Trigger");
                if (trigger != null) {
                    newIssue.setFieldValue(trigger.getId(), trigger);
                }

                IssueField trigger = issue.getFieldByName("Trigger");
                if (trigger != null) {
                    JSONObject triggerJO = (JSONObject) trigger.getValue();
                    newIssue.setFieldValue(trigger.getId(), ComplexIssueInputFieldValue.with("value", triggerJO.get("value")));
                }

*/
                RunnerParamsProvider.printProperties();



                //Create New Field Input Updates
                Resolution fixedResolution = getResolutionByName("Fixed");
                String resolutionString = null;
                Map resMap = Collections.EMPTY_MAP;
                Map versionMap = new HashMap();
                versionMap.put("id", String.valueOf(version.getId()));
                versionMap.put("name", version.getName());
                try {
                    resolutionString = mapper.writeValueAsString(fixedResolution);
                    System.out.println("********* RESOLUTION STRING ************");
                    System.out.println(resolutionString);
                    System.out.println("********* END RESOLUTION STRING ************");
                    resMap = mapper.readValue(resolutionString, HashMap.class);
//                    versionMap = mapper.readValue(mapper.writeValueAsString(version), HashMap.class);

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                List<Map> fixVersions = new ArrayList<Map>();
                fixVersions.add(resMap);
//                JSONObject resolutionJO = new JSONObject(fixedResolution, new String[]{"resolution"});
//                ComplexIssueInputFieldValue complexIssueInputFieldValue = ComplexIssueInputFieldValue.with("resolution", "{\"self\"=\"http://localhost:2990/jira/rest/api/2/resolution/10100\", \"name\"=\"Fixed\", \"description\"=\"A fix for this issue is checked into the tree and tested.\"}");
//                ComplexIssueInputFieldValue complexIssueInputFieldValue = ComplexIssueInputFieldValue.with("resolution", resolutionString);
//                Map map = new HashMap();
//                map.put("name", "Fixed");
                ComplexIssueInputFieldValue complexIssueInputFieldValue = new ComplexIssueInputFieldValue(resMap);
                ComplexIssueInputFieldValue versionComplexIssueInputFieldValue = new ComplexIssueInputFieldValue(versionMap);

                List<ComplexIssueInputFieldValue> fixVersionsComplex = new ArrayList<ComplexIssueInputFieldValue>();
                fixVersionsComplex.add(versionComplexIssueInputFieldValue);

//                Collection<FieldInput> fieldInputs = Arrays.asList(new FieldInput("resolution", resolutionString), new FieldInput("fixVersions", fixVersions));
                Collection<FieldInput> fieldInputs = Arrays.asList(new FieldInput("resolution", complexIssueInputFieldValue), new FieldInput("fixVersions", fixVersionsComplex));
//                Collection<FieldInput> fieldInputs = Arrays.asList(new FieldInput("fixVersions", fixVersions));
                //Create final transition to ship across the wire.
                final TransitionInput resolvedTransitionInput = new TransitionInput(transition.getId(), fieldInputs, Comment.valueOf("This issue was released and closed via TeamCity Plugin."));
                //SHIP IT!!!
                getRestClient().getIssueClient().transition(issue.getTransitionsUri(), resolvedTransitionInput, pm);
            }
        }
    }

    public static String getIssueId(){
        return issueId;
    }
}
