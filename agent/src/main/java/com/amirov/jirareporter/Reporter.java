package com.amirov.jirareporter;

import com.amirov.jirareporter.jira.JIRAClient;
import com.amirov.jirareporter.teamcity.IBuildInfo;
import com.atlassian.jira.rest.client.domain.Issue;
import jetbrains.buildServer.agent.BuildProgressLogger;

import java.net.URISyntaxException;
import java.util.Collection;

public class Reporter
{
    private final RunnerParamsProvider _prmsProvider;
    private final BuildProgressLogger _logger;

    public Reporter(RunnerParamsProvider prmsProvider)
    {
        _prmsProvider = prmsProvider;
        _logger = prmsProvider.getLogger();
    }

    public void report(Collection<String> issueIds, IBuildInfo buildInfo) throws URISyntaxException
    {
        String comment = buildInfo.buildCommentText();
        _logger.message("Ready to report to JIRA. Message: " + comment);

        JIRAClient jira = new JIRAClient(_prmsProvider);
        for (String issueId : issueIds)
        {
            _logger.message("Loading issue " + issueId);
            final Issue issue = jira.getIssue(issueId);
            if (_prmsProvider.isCommentingEnabled())
            {
                _logger.message("Adding comment...");
                jira.addComment(issue, comment);
            }

            if (_prmsProvider.isLinkToBuildPageEnabled())
            {
                _logger.message("Making link to TeamCity build page...");
                String title = buildInfo.getBuildName() + " " + buildInfo.getBuildNumber();
                jira.makeLink(issue, buildInfo.getWebUrl() + "&tab=artifacts", title);
            }
        }
        _logger.message("Reporting completed!");
    }

    /*
    private static final ObjectMapper mapper = new ObjectMapper();
    public void progressIssue() {
        NullProgressMonitor pm = new NullProgressMonitor();
        if(RunnerParamsProvider.isProgressIssueEnabled() == null){}
        else if(RunnerParamsProvider.isProgressIssueEnabled().equals("true")){
            String transitionName = JIRAConfig.prepareJiraWorkflow(parser.getBuildStatus()).get(getIssueStatus());
            if (transitionName != null) {
                //Get Transition
                Transition transition = getTransitionByName(transitionName);

                //Create New Field Input Updates
                Resolution fixedResolution = getResolutionByName("Fixed");
                Map resMap = Collections.EMPTY_MAP;
                try {
                    String resolutionString = mapper.writeValueAsString(fixedResolution);
                    resMap = mapper.readValue(resolutionString, HashMap.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ComplexIssueInputFieldValue complexIssueInputFieldValue = new ComplexIssueInputFieldValue(resMap);

                //Get Jira Version
                Map<String, Object> versionMap = new HashMap<>();
                Version version = getVersion(parser.getReleasedPomVersionString());
                versionMap.put("id", String.valueOf(version.getId()));
                versionMap.put("name", version.getName());
                ComplexIssueInputFieldValue versionComplexIssueInputFieldValue = new ComplexIssueInputFieldValue(versionMap);

                List<ComplexIssueInputFieldValue> fixVersionsComplex = new ArrayList<>();
                fixVersionsComplex.add(versionComplexIssueInputFieldValue);

                Collection<FieldInput> fieldInputs = Arrays.asList(new FieldInput("resolution", complexIssueInputFieldValue), new FieldInput("fixVersions", fixVersionsComplex));
                //Create final transition input to ship across the wire.
                final TransitionInput resolvedTransitionInput = new TransitionInput(transition.getId(), fieldInputs, Comment.valueOf("This issue was released and closed via TeamCity Plugin."));
                //SHIP IT!!!
                getRestClient().getIssueClient().transition(getIssue().getTransitionsUri(), resolvedTransitionInput, pm);

                _logger.message(issueKey + " has been transitioned to Closed with resolution Fixed and Fix Version of " + version.getName());
            }
        }
    }*/
}
