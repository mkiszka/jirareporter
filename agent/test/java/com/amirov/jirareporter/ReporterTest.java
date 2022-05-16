package com.amirov.jirareporter;

import com.amirov.jirareporter.jira.JIRAClient;
import com.amirov.jirareporter.jira.JIRAWorkflow;
import com.amirov.jirareporter.teamcity.TeamCityXMLParser;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.Resolution;
import com.atlassian.jira.rest.client.domain.Transition;
import java.net.URI;
import jetbrains.buildServer.agent.BuildProgressLogger;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ReporterTest {

    static Stream<Arguments> generateData_TransitionIssue() {


        return Stream.of(
                Arguments.of("SUCCESS:In Progress-Done,ToDo-In Progress;\nFAILURE:In Progress-Reopen Issue,In Testing-Reopen Issue,Closed-Reopen Issue;","SUCCESS",3),
                Arguments.of("SUCCESS:In Progress-Done,ToDo-In Progress;\nFAILURE:In Progress-Reopen Issue,In Testing-Reopen Issue,Closed-Reopen Issue;","FAILURE",3),
                Arguments.of("SUCCESS:In Progress-Done,ToDo-In Progress;\nFAILURE:In Testing-Reopen Issue,Closed-Reopen Issue;","FAILURE",0)
        );
    }

    @ExtendWith(MockitoExtension.class)
    @ParameterizedTest
    @MethodSource("generateData_TransitionIssue")
    @MockitoSettings(strictness = Strictness.LENIENT)
    void TransitionIssue(String prmsProvider_getJiraWorkflow,String buildStatus, int jiraClient_tranistion_wantedNumberOfInvokation) {
        //Arrange
        RunnerParamsProvider prmsProvider = Mockito.mock(RunnerParamsProvider.class);
        JIRAClient jiraClient = Mockito.mock(JIRAClient.class);
        TeamCityXMLParser teamCityXMLParser = Mockito.mock(TeamCityXMLParser.class);
        Issue issue = Mockito.mock(Issue.class);
        Transition transition = Mockito.mock(Transition.class);

        BuildProgressLogger buildProgressLogger = Mockito.mock(BuildProgressLogger.class);
        Mockito.when(prmsProvider.getLogger()).thenReturn(buildProgressLogger);
        Mockito.when(prmsProvider.isTransitionIssueEnabled()).thenReturn(true);
        Mockito.when(prmsProvider.getJiraWorkFlow()).thenAnswer(i ->  prmsProvider_getJiraWorkflow);

        Mockito.when(issue.getKey()).thenReturn("TEST-321","TEST-318","KOM-2246");

        Resolution resolution = null;
        try {
            resolution = new Resolution( new URI("https://jira.domain/rest/api/2/resolution/6"),"Done","The task is done.");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            assertTrue(false,"There should be no exception.");

        }

        Mockito.when(jiraClient.getIssue(Mockito.any())).thenReturn(issue);
        Mockito.when(jiraClient.getIssueStatus(Mockito.anyString())).thenReturn("In Progress");
        Mockito.when(jiraClient.getTransitionByName(Mockito.anyString(),Mockito.any())).thenReturn(transition);
        Mockito.when(jiraClient.getResolutionByName(Mockito.anyString())).thenReturn(resolution);

        Mockito.when(teamCityXMLParser.getBuildNumber()).thenReturn("25");
        Mockito.when(teamCityXMLParser.getBuildStatus()).thenReturn(buildStatus);



        JIRAWorkflow jiraWorkflow = new JIRAWorkflow(prmsProvider);

        Collection<String> issueIds = Arrays.asList(new String[] {"TEST-321","TEST-318","KOM-2246"});

        //Act
        try {
            Reporter reporter = new Reporter(prmsProvider, jiraClient, jiraWorkflow, teamCityXMLParser);
            reporter.transitionIssue(issueIds);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            assertTrue(false,"There should be no exception.");
        }
        //Assert
        Mockito.verify(jiraClient,Mockito.times(jiraClient_tranistion_wantedNumberOfInvokation)).transition(Mockito.any(),Mockito.any());
    }

}