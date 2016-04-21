
package com.amirov.jirareporter;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.BuildRunnerContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;


public class JIRABuildProcess implements BuildProcess{
    private final AgentRunningBuild myBuild;
    private final BuildRunnerContext myContext;
    private BuildProgressLogger logger;


    public JIRABuildProcess(@NotNull AgentRunningBuild build, @NotNull BuildRunnerContext context){
        myBuild = build;
        myContext = context;
    }

    @Override
    public void start() throws RunBuildException {
        Map<String, String> runnerParams = new HashMap<String, String>(myContext.getRunnerParameters());
        for(Map.Entry<String, String> entry : runnerParams.entrySet()){
            RunnerParamsProvider.setProperty(entry.getKey(), entry.getValue());
        }
        RunnerParamsProvider.setProperty("build.type.id", myContext.getBuild().getBuildTypeId());
        logger = myBuild.getBuildLogger();
        Reporter reporter = new Reporter(logger);
        String issueId = RunnerParamsProvider.getIssueId();
        if(runnerParams.get("enableIssueProgressing") == null){
            RunnerParamsProvider.setProperty("enableIssueProgressing", "false");
        }
        if(runnerParams.get("enableSSLConnection") == null){
            RunnerParamsProvider.setProperty("enableSSLConnection", "false");
        }
        if(runnerParams.get("enableTemplateComment") == null){
            RunnerParamsProvider.setProperty("enableTemplateComment", "false");
        }
        RunnerParamsProvider.setProperty("buildName", myContext.getBuild().getBuildTypeName());
        if(issueId == null || issueId.isEmpty()){
            logger.message("No Issue was found in the change log for this build.");
        } else {
            if(issueId.contains(",")){
                for(String issue : issueId.split(",")){
                    reporter.report(issue);
                    reporter.progressIssue();
                }
            } else {
                reporter.report(issueId);
                reporter.progressIssue();
            }
        }
    }

    @Override
    public boolean isInterrupted() {
        return false;
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public void interrupt() {
    }

    @NotNull
    @Override
    public BuildFinishedStatus waitFor() throws RunBuildException {
        return BuildFinishedStatus.FINISHED_SUCCESS;
    }
}
