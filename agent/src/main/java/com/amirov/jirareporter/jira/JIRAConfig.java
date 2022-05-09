package com.amirov.jirareporter.jira;

import com.amirov.jirareporter.RunnerParamsProvider;
import java.util.HashMap;
import java.util.Map;


public class JIRAConfig
{
    /**
     * @Deprecated Use JIRAWorkflow:processWorkflow instead.
     */
    @Deprecated
    private static void processWorkflow(String buildStatus, String propStatus, Map<String, String> workFlowMap)
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
    /**
     * @Deprecated Use JIRAWorkflow:prepareJiraWorkflow instead.
     */
    @Deprecated
    public static Map<String, String> prepareJiraWorkflow(String buildStatus)
    {
        return null;
    }

}
