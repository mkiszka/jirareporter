package com.amirov.jirareporter;

import com.amirov.jirareporter.jira.JIRAClient;
import com.amirov.jirareporter.jira.JIRATransitionParams;
import com.amirov.jirareporter.jira.JIRAWorkflow;
import com.amirov.jirareporter.jira.exceptions.JIRAProcessWorkflowException;
import com.amirov.jirareporter.jira.exceptions.JIRATransitionParamsSetException;
import com.amirov.jirareporter.teamcity.IBuildInfo;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.RestClientException;
import com.atlassian.jira.rest.client.domain.*;
import com.atlassian.jira.rest.client.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.domain.input.FieldInput;
import com.atlassian.jira.rest.client.domain.input.TransitionInput;
import com.fasterxml.jackson.databind.ObjectMapper;
import jetbrains.buildServer.agent.BuildProgressLogger;
import name.kiszka.jirareporter.jira.JIRAProcessExceptions;

import java.net.URISyntaxException;
import java.util.*;

public class Reporter
{
    private final RunnerParamsProvider _prmsProvider;
    private final BuildProgressLogger _logger;
    private final JIRAWorkflow _jiraWorkflow;
    private final JIRAClient _jiraClient;
    private final IBuildInfo _buildInfo;
    private static final ObjectMapper mapper = new ObjectMapper();
    private JIRAProcessExceptions _jiraProcessExceptions;

    public Reporter(RunnerParamsProvider prmsProvider,JIRAClient jiraClient, JIRAWorkflow jiraWorkflow,
                    IBuildInfo buildInfo,JIRAProcessExceptions jiraProcessExceptions) throws URISyntaxException {
        _prmsProvider = prmsProvider;
        _logger = prmsProvider.getLogger();
        _jiraClient = jiraClient;
        _jiraWorkflow = jiraWorkflow;
        _buildInfo = buildInfo;
        _jiraProcessExceptions = jiraProcessExceptions;
    }

    public void report(Collection<String> issueIds)
    {
        String comment = _buildInfo.buildCommentText();
        _logger.message("Ready to report to JIRA. Message: " + comment);

        for (String issueId : issueIds)
        {
            try {
                _logger.message("Loading issue " + issueId);
                final Issue issue = _jiraClient.getIssue(issueId);
                if (_prmsProvider.isCommentingEnabled()) {
                    _logger.message("Adding comment...");
                    _jiraClient.addComment(issue, comment);
                }

                if (_prmsProvider.isLinkToBuildPageEnabled()) {
                    _logger.message("Making link to TeamCity build page...");
                    String title = _buildInfo.getBuildName() + " " + _buildInfo.getBuildNumber();
                    _jiraClient.makeLink(issue, _buildInfo.getWebUrl() + "&tab=artifacts", title);
                }

            } catch (RestClientException e) {
                _jiraProcessExceptions.process(e,issueId);
            }
        }
        _logger.message("Reporting completed!");
    }
    //private static final ObjectMapper mapper = new ObjectMapper();
    public void transitionIssue(Collection<String> issueIds)
    {
        _logger.message("Ready to transition Issues. Build number: " + _buildInfo.getBuildNumber());
        try {
            NullProgressMonitor pm = new NullProgressMonitor();
            for (String issueId : issueIds) {
                if (!_prmsProvider.isTransitionIssueEnabled()) {
                } else {
                    try {
                        _logger.message("Transition " + _jiraClient.getIssue(issueId).getKey() + " has been started");

                        Issue issue = _jiraClient.getIssue(issueId);
                        Map<String, JIRATransitionParams> transitionDefinitions = null;

                        transitionDefinitions = _jiraWorkflow.prepareJiraWorkflow(_buildInfo.getBuildStatus());
                        if(transitionDefinitions != null) {
                            for (String statusName: transitionDefinitions.keySet()  ) {

                                    if (statusName.equals(issue.getStatus().getName())) {
                                        JIRATransitionParams transitionParams = transitionDefinitions.get(statusName);
                                        String transitionName = transitionParams.getTransitionName();
                                        //Get Transition
                                        Transition transition = _jiraClient.getTransitionByName(issueId, transitionName);
                                        Collection<FieldInput> fieldInputs = new ArrayList<FieldInput>();

                                        if (transition == null) {
                                            _logger.message("There is no possibility to transition issue from " + issue.getStatus().getName() + " to " + transitionName);
                                        } else {
                                            //Create New Field Input Updates

                                            String resolutionName = transitionParams.getResolutionName();
                                            if (resolutionName != null &&
                                                    !resolutionName.isEmpty()) {
                                                Resolution resolution = _jiraClient.getResolutionByName(transitionParams.getResolutionName());
                                                Map resMap = Collections.EMPTY_MAP;
                                                try {
                                                    String resolutionString = mapper.writeValueAsString(resolution);
                                                    resMap = mapper.readValue(resolutionString, HashMap.class);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                                ComplexIssueInputFieldValue complexIssueInputFieldValue = new ComplexIssueInputFieldValue(resMap);
                                                fieldInputs.add(new FieldInput("resolution", complexIssueInputFieldValue));
                                            }

                                            //Get Jira Version
                                    /*    Map<String, Object> versionMap = new HashMap<>();
                                        Version version = getVersion(_buildInfo.getReleasedPomVersionString());
                                        versionMap.put("id", String.valueOf(version.getId()));
                                        versionMap.put("name", version.getName());
                                        ComplexIssueInputFieldValue versionComplexIssueInputFieldValue = new ComplexIssueInputFieldValue(versionMap);

                                        List<ComplexIssueInputFieldValue> fixVersionsComplex = new ArrayList<>();
                                        fixVersionsComplex.add(versionComplexIssueInputFieldValue);
                                        fieldInputs.add(new FieldInput("fixVersions", fixVersionsComplex));
                                        */

                                            //Create final transition input to ship across the wire.
                                            final TransitionInput transitionInput = new TransitionInput(transition.getId(), fieldInputs, Comment.valueOf("This issue was released and closed via TeamCity Plugin."));

                                            //SHIP IT!!!
                                            _jiraClient.transition(issueId, transitionInput);

                                            _logger.message(_jiraClient.getIssue(issueId).getKey() + " has been transitioned to " + transition.getName() + ((resolutionName == null || resolutionName.isEmpty()) ? "with no resolution" : " with resolution " + resolutionName) + ".");
                                        }
                                    } else {
                                        _logger.message(_jiraClient.getIssue(issueId).getKey() + " hasn't been transitioned.");
                                    }

                            }
                        }
                    } catch (RestClientException e) {
                        _jiraProcessExceptions.process(e,issueId);
                    }
                }
            }
            _logger.message("Transitions completed!");
        } catch (JIRATransitionParamsSetException e) {
            _jiraProcessExceptions.process(e);

        } catch (JIRAProcessWorkflowException e) {
            _jiraProcessExceptions.process(e);
        }

    }
}
