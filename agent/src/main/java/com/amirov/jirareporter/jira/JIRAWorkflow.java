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

    private HashMap<String, String> processWorkflow(String buildStatus, String propStatus )
    {

        HashMap<String, String> workFlowMap = new HashMap<String, String>();

        String [] progressSteps = propStatus.split(":");
        String progressStep = progressSteps[progressSteps.length-1];
        String [] transitions = progressStep.split(",");
        for(String transition : transitions){
            String [] steps = transition.split("-");
            String key = steps[steps.length-2];
            String value = steps[steps.length-1];
            workFlowMap.put(key, value);
        }
        return workFlowMap;
    }

    public Map<String, String> prepareJiraWorkflow(String buildStatus)
    {
        String [] transitionsOnStatusBuild = prmsProvider.getJiraWorkFlow().split(";");
        for(String transition : transitionsOnStatusBuild){
            if(transition.contains(buildStatus)) {
                return processWorkflow(buildStatus,transition);
            }
        }
        return null;
    }

}
