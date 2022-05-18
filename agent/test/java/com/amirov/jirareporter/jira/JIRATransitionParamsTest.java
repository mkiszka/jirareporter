package com.amirov.jirareporter.jira;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class JIRATransitionParamsTest {

    static Stream<Arguments> generateData_compareFields() {
        return  Stream.of(
            Arguments.of(new JIRATransitionParams(),new JIRATransitionParams(),true),
            Arguments.of(new JIRATransitionParams().setStatusName("ToDo").setTransitionName("In Progress"),new JIRATransitionParams().setStatusName("ToDo").setTransitionName("In Progress"),true),
            Arguments.of(new JIRATransitionParams().setStatusName("ToDo").setTransitionName("In Progress").setResolutionName("Done"),new JIRATransitionParams().setStatusName("ToDo").setTransitionName("In Progress").setResolutionName("Done"),true),
            Arguments.of(new JIRATransitionParams().setStatusName("ToDo").setTransitionName("In Progress").setResolutionName("Fixed"),new JIRATransitionParams().setStatusName("ToDo").setTransitionName("In Progress"),false),
            Arguments.of(new JIRATransitionParams().setStatusName("ToDo").setTransitionName(null),new JIRATransitionParams().setStatusName("ToDo").setTransitionName("In Progress"),false),
            Arguments.of(new JIRATransitionParams().setStatusName("ToDo").setTransitionName("In Progress"),new JIRATransitionParams().setStatusName("ToDo").setTransitionName(null),false)
        );
    }

    @ParameterizedTest
    @MethodSource("generateData_compareFields")
    void testCompareFields(JIRATransitionParams param1,JIRATransitionParams param2,boolean expectedResult) {
        assertTrue(param1.compareFields(param2) == expectedResult);
    }

    static Stream<Arguments> generateData_equals() {
        JIRATransitionParams a = new JIRATransitionParams();
        return  Stream.of(
                Arguments.of(a,a,true),
                Arguments.of(new JIRATransitionParams(),new Object(),false),
                Arguments.of(new JIRATransitionParams(),null,false),
                Arguments.of(new JIRATransitionParams(),new JIRATransitionParams(),true),
                Arguments.of(new JIRATransitionParams().setStatusName("ToDo").setTransitionName("In Progress"),new JIRATransitionParams().setStatusName("ToDo").setTransitionName("In Progress"),true),
                Arguments.of(new JIRATransitionParams().setStatusName("ToDo").setTransitionName("In Progress").setResolutionName("Done"),new JIRATransitionParams().setStatusName("ToDo").setTransitionName("In Progress").setResolutionName("Done"),true),
                Arguments.of(new JIRATransitionParams().setStatusName("ToDo").setTransitionName("In Progress").setResolutionName("Fixed"),new JIRATransitionParams().setStatusName("ToDo").setTransitionName("In Progress"),false),
                Arguments.of(new JIRATransitionParams().setStatusName("ToDo").setTransitionName(null),new JIRATransitionParams().setStatusName("ToDo").setTransitionName("In Progress"),false),
                Arguments.of(new JIRATransitionParams().setStatusName("ToDo").setTransitionName("In Progress"),new JIRATransitionParams().setStatusName("ToDo").setTransitionName(null),false)
        );
    }

    @ParameterizedTest
    @MethodSource("generateData_equals")
    void testEquals(JIRATransitionParams param1,Object param2,boolean expectedResult) {
        assertTrue(param1.equals(param2) == expectedResult);
    }
}