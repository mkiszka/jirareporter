package com.amirov.jirareporter;

import com.amirov.jirareporter.teamcity.TeamCityXMLParser;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.BuildRunnerContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class JIRABuildProcess implements BuildProcess
{
    private final AgentRunningBuild _myBuild;
    private final BuildRunnerContext _myContext;
    private final BuildProgressLogger _logger;
    private final RunnerParamsProvider _prmsProvider;

    public JIRABuildProcess(@NotNull AgentRunningBuild build, @NotNull BuildRunnerContext context)
    {
        _myBuild = build;
        _myContext = context;
        _logger = _myBuild.getBuildLogger();

        _prmsProvider = new RunnerParamsProvider(_myContext.getRunnerParameters(), _logger);
        _prmsProvider.set("build.type.id", _myContext.getBuild().getBuildTypeId());
        _prmsProvider.set("buildName", _myContext.getBuild().getBuildTypeName());
        if (_myContext.getBuild().isPersonal())
            _prmsProvider.logProperties();
    }

    @Override
    public void start() throws RunBuildException
    {
        if (!_prmsProvider.isAnyFeatureEnabled())
        {
            _logger.warning("All features are disabled. Processing was skipped.");
            return;
        }

        try
        {
            TeamCityXMLParser parser = new TeamCityXMLParser(_prmsProvider);
            Collection<String> issueIds = parser.getIssueKeys();

            if (issueIds == null || issueIds.isEmpty())
                _logger.warning("No Issue was found in the change log for this build.");
            else
            {
                _logger.message("Issue ids: " + String.join(", ", issueIds));

                Reporter reporter = new Reporter(_prmsProvider);
                reporter.report(issueIds, parser);
            }
        }
        catch (Exception ex)
        {
            _prmsProvider.dumpProperties();
            _logger.exception(ex);
            throw new RunBuildException(ex);
        }
    }

    @Override
    public boolean isInterrupted() { return false; }

    @Override
    public boolean isFinished() { return false; }

    @Override
    public void interrupt() { }

    @NotNull
    @Override
    public BuildFinishedStatus waitFor() throws RunBuildException
    {
        return BuildFinishedStatus.FINISHED_SUCCESS;
    }
}
