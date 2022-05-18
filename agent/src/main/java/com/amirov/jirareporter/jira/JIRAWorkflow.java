package com.amirov.jirareporter.jira;

import com.amirov.jirareporter.RunnerParamsProvider;
import com.amirov.jirareporter.jira.exceptions.JIRAProcessWorkflowException;
import com.amirov.jirareporter.jira.exceptions.JIRATransitionParamsSetException;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class JIRAWorkflow
{
    private RunnerParamsProvider prmsProvider;
    public JIRAWorkflow(RunnerParamsProvider prmsProvider) {
        this.prmsProvider = prmsProvider;
    }

    private HashMap<String, JIRATransitionParams> processWorkflow(String buildStatus, String propStatus ) throws JIRATransitionParamsSetException, JIRAProcessWorkflowException {

        HashMap<String, JIRATransitionParams> workFlowMap = new HashMap<String, JIRATransitionParams>();

        String [] progressSteps = propStatus.split(":");
        String progressStep = progressSteps[progressSteps.length-1];
        String [] transitions = progressStep.split(",");
        for(String transition : transitions){
            String [] steps = transition.split("-");

            JIRATransitionParams transitionParams = new JIRATransitionParams()
                    .setStatusName(steps[0])
                    .setTransitionName(steps[1]);
            for(int i=2;i<steps.length;++i) {
                String [] param = steps[i].split("[\\[\\]]");
                transitionParams.set(param[0] + "Name",param[1]);
            }
            if(workFlowMap.containsKey(transitionParams.getStatusName())) {
                throw new JIRAProcessWorkflowException("You can not define the transition from " + transitionParams.getStatusName() + " twice.");
            }
            workFlowMap.put(transitionParams.getStatusName(), transitionParams);
        }
        return workFlowMap;
    }

    public Map<String, JIRATransitionParams> prepareJiraWorkflow(String buildStatus) throws JIRATransitionParamsSetException, JIRAProcessWorkflowException {
        String [] transitionsOnStatusBuild = prmsProvider.getJiraWorkFlow().split(";");
        for(String transition : transitionsOnStatusBuild){
            if(transition.contains(buildStatus)) {
                return processWorkflow(buildStatus,transition);
            }
        }
        return null;
    }

}
