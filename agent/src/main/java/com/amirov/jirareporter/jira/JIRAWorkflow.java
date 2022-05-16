package com.amirov.jirareporter.jira;

import com.amirov.jirareporter.RunnerParamsProvider;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class JIRAWorkflow
{
    private RunnerParamsProvider prmsProvider;
    public JIRAWorkflow(RunnerParamsProvider prmsProvider) {
        this.prmsProvider = prmsProvider;
    }

    private void processWorkflow(String buildStatus, String propStatus, Map<String, String> workFlowMap)
    {
        if(propStatus.contains(buildStatus))
        {
            String [] progressSteps = propStatus.split(":");
            String progressStep = progressSteps[progressSteps.length-1];
            String [] transitions = progressStep.split(",");
            for(String transition : transitions){
                String [] steps = transition.split("-");
                String key = steps[steps.length-2];
                String value = steps[steps.length-1];
                workFlowMap.put(key, value);
            }
        }
    }

    public Map<String, String> prepareJiraWorkflow(String buildStatus)
    {
        Map<String, String> successWorkflowMap = new HashMap<String, String>();
        Map<String, String> failureWorkflowMap = new HashMap<String, String>();
        String [] statusCont = prmsProvider.getJiraWorkFlow().split(";");
        for(String status : statusCont){
            processWorkflow("SUCCESS", status, successWorkflowMap);
            processWorkflow("FAILURE", status, failureWorkflowMap);
        }
        if(buildStatus.equals("SUCCESS")){
            return successWorkflowMap;
        }
        else {
            return failureWorkflowMap;
        }
    }

}
