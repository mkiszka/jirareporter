package com.amirov.jirareporter.jira.exceptions;

public class JIRATransitionParamsSetException extends Exception {
    public JIRATransitionParamsSetException(String s, Exception e)  {
        super(s,e);
    }
}
