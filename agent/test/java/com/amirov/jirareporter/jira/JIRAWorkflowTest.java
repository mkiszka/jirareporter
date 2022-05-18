package com.amirov.jirareporter.jira;

import com.amirov.jirareporter.RunnerParamsProvider;
import com.amirov.jirareporter.jira.exceptions.JIRAProcessWorkflowException;
import com.amirov.jirareporter.jira.exceptions.JIRATransitionParamsSetException;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matchers;
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


class match extends BaseMatcher<Map<String,JIRATransitionParams>> {

    @Override
    public boolean matches(Object o) {
        return false;
    }

    @Override
    public void describeTo(Description description) {

    }
}
class JIRAWorkflowTest {

    static Stream<Arguments> generateData_successTransitionIssue() {


        return Stream.of(
                Arguments.of("SUCCESS:In Progress-Done,ToDo-In Progress;\nFAILURE:In Progress-Reopen Issue,In Testing-Reopen Issue,Closed-Reopen Issue;","SUCCESS",new HashMap<String,JIRATransitionParams>() {{ put("In Progress",new JIRATransitionParams().setStatusName("In Progress").setTransitionName("Done")); put("ToDo",new JIRATransitionParams().setStatusName("ToDo").setTransitionName("In Progress")); }}),
                Arguments.of("SUCCESS:In Progress-Done,ToDo-In Progress;\nFAILURE:In Progress-Reopen Issue,In Testing-Reopen Issue,Closed-Reopen Issue;","FAILURE",new HashMap<String,JIRATransitionParams>() {{ put("In Progress",new JIRATransitionParams().setStatusName("In Progress").setTransitionName("Reopen Issue")); put("In Testing",new JIRATransitionParams().setStatusName("In Testing").setTransitionName("Reopen Issue")); put("Closed",new JIRATransitionParams().setStatusName("Closed").setTransitionName("Reopen Issue")); }}),
                Arguments.of("SUCCESS:In Progress-Done,ToDo-In Progress;\nFAILURE:In Progress-Reopen Issue,In Testing-Reopen Issue,Closed-Reopen Issue;","UNEXIST_STATUS",null),
                Arguments.of("SUCCESS:In Progress-Done-Resolution[Done],ToDo-In Progress-Resolution[None],Implemented-Done-Resolution[Fixed]-FixVersion[v1.0.9];\nFAILURE:In Progress-Reopen Issue,In Testing-Reopen Issue,Closed-Reopen Issue;","SUCCESS",new HashMap<String,JIRATransitionParams>() {{ put("In Progress",new JIRATransitionParams().setStatusName("In Progress").setTransitionName("Done").setResolutionName("Done")); put("ToDo",new JIRATransitionParams().setStatusName("ToDo").setTransitionName("In Progress")); put("Implemented",new JIRATransitionParams().setStatusName("Implemented").setTransitionName("Done").setResolutionName("Fixed").setFixVersionName("v1.0.9")); }})

                );
    }
    @ParameterizedTest
    @MethodSource("generateData_successTransitionIssue")
    public void TestPrepareJiraWorkflow(String prmsProvider_getJiraWorkflow,String buildStatus,HashMap<String,JIRATransitionParams> expectedResult) {


        //Arrange
        RunnerParamsProvider prmsProvider = Mockito.mock(RunnerParamsProvider.class);
        Mockito.when(prmsProvider.getJiraWorkFlow()).thenAnswer(i ->  prmsProvider_getJiraWorkflow);

        //Act
        JIRAWorkflow jiraWorkflow = new JIRAWorkflow(prmsProvider);
        Map<String, JIRATransitionParams> result = null;
        try {
            result = jiraWorkflow.prepareJiraWorkflow(buildStatus);
        } catch (JIRATransitionParamsSetException | JIRAProcessWorkflowException e) {
            e.printStackTrace();
            assertTrue(false);
        }

        //Assert
        if( expectedResult == null) {
            assertNull(result);
        } else {
            for (Map.Entry<String, JIRATransitionParams> entry : expectedResult.entrySet()) {
               // assertThat(result, new match());
                assertThat(result, hasEntry(entry.getKey(), entry.getValue()));
            }
        }
    }


    static Stream<Arguments> generateData_TestException_PrepareJiraWorkflow() {


        return Stream.of(
                Arguments.of("SUCCESS:In Progress-Done-Resolution[Done],ToDo-In Progress-Resolution[None],ToDo-In Progress-Resolution[Fixed];\nFAILURE:In Progress-Reopen Issue,In Testing-Reopen Issue,Closed-Reopen Issue;","SUCCESS",JIRAProcessWorkflowException.class),
                Arguments.of("SUCCESS:In Progress-Done-Resolution[Done],ToDo-In Progress-Resolution[None];\nFAILURE:In Progress-Reopen Issue,In Testing-Reopen Issue,In Testing-Reject Issue,Closed-Reopen Issue;","FAILURE",JIRAProcessWorkflowException.class),
                Arguments.of("SUCCESS:In Progress-Done-WrongDefinitionTest[Done]\n","SUCCESS",JIRATransitionParamsSetException.class)

        );
    }
    @ParameterizedTest
    @MethodSource("generateData_TestException_PrepareJiraWorkflow")
    public void TestException_PrepareJiraWorkflow(String prmsProvider_getJiraWorkflow,String buildStatus,Class expectedType) {


        //Arrange
        RunnerParamsProvider prmsProvider = Mockito.mock(RunnerParamsProvider.class);
        Mockito.when(prmsProvider.getJiraWorkFlow()).thenAnswer(i ->  prmsProvider_getJiraWorkflow);

        //Act
        JIRAWorkflow jiraWorkflow = new JIRAWorkflow(prmsProvider);
        //Assert
        assertThrows(expectedType, () -> { jiraWorkflow.prepareJiraWorkflow(buildStatus);});

    }
}