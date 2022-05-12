package com.amirov.jirareporter;

import com.amirov.jirareporter.jira.JIRAClient;
import com.amirov.jirareporter.jira.JIRAWorkflow;
import com.amirov.jirareporter.teamcity.IBuildInfo;
import com.amirov.jirareporter.teamcity.TeamCityXMLParser;
import jetbrains.buildServer.agent.BuildProgressLogger;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class ReporterTest {

    @ExtendWith(MockitoExtension.class)
    @org.junit.jupiter.api.Test
    void transitionIssue(@Mock RunnerParamsProvider prmsProvider, @Mock JIRAClient jiraClient, @Mock JIRAWorkflow jiraWorkflow, @Mock TeamCityXMLParser teamCityXMLParser) {
        //Arrange
        BuildProgressLogger buildProgressLogger = Mockito.mock(BuildProgressLogger.class);
        Mockito.when(prmsProvider.getBuildName()).thenReturn("wrukuluku");
        Mockito.when(prmsProvider.getLogger()).thenReturn(buildProgressLogger);
        Mockito.when(prmsProvider.isProgressIssueEnabled()).thenReturn(true);

        Collection<String> issueIds = Arrays.asList(new String[] {"TEST-321","TEST-318","KOM-2246"});

        //Act
        try {
            Reporter reporter = new Reporter(prmsProvider, jiraClient, jiraWorkflow, teamCityXMLParser);
            reporter.transitionIssue(issueIds);
        } catch (URISyntaxException e) {
            assertTrue(false,"There should be no exception.");
        }
        //Assert
    }
}