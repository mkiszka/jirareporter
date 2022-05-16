package com.amirov.jirareporter.jira;

import com.amirov.jirareporter.RunnerParamsProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class JIRAWorkflowTest {

    static Stream<Arguments> generateData_successTransitionIssue() {


        return Stream.of(
                Arguments.of("SUCCESS:In Progress-Done,ToDo-In Progress;\nFAILURE:In Progress-Reopen Issue,In Testing-Reopen Issue,Closed-Reopen Issue;","SUCCESS",new HashMap<String,String>() {{ put("In Progress","Done"); put("ToDo","In Progress"); }}),
                Arguments.of("SUCCESS:In Progress-Done,ToDo-In Progress;\nFAILURE:In Progress-Reopen Issue,In Testing-Reopen Issue,Closed-Reopen Issue;","FAILURE",new HashMap<String,String>() {{ put("In Progress","Reopen Issue"); put("In Testing","Reopen Issue"); put("Closed","Reopen Issue"); }})
        );
    }
    @ParameterizedTest
    @MethodSource("generateData_successTransitionIssue")
    public void PrepareJiraWorkflowTest(String prmsProvider_getJiraWorkflow,String buildStatus,HashMap<String,String> expectedResult) {

        //Arrange
        RunnerParamsProvider prmsProvider = Mockito.mock(RunnerParamsProvider.class);
        Mockito.when(prmsProvider.getJiraWorkFlow()).thenAnswer(i ->  prmsProvider_getJiraWorkflow);

        //Act
        JIRAWorkflow jiraWorkflow = new JIRAWorkflow(prmsProvider);
        Map<String, String> result = jiraWorkflow.prepareJiraWorkflow(buildStatus);


        //Assert
        for (Map.Entry<String,String> entry: expectedResult.entrySet()) {
            assertThat(result, hasEntry(entry.getKey(),entry.getValue()));
        }

    }

}