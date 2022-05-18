package com.amirov.jirareporter.jira.exceptions;

public class JIRAProcessWorkflowException extends Exception {
    public JIRAProcessWorkflowException(String s, Exception e)  {
        super(s,e);
    }

    public JIRAProcessWorkflowException(String message) {
        super(message);
    }
}
