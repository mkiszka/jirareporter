package com.amirov.jirareporter;

import jetbrains.buildServer.agent.BuildProgressLogger;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class RunnerParamsProvider
{
    private static final String CONFIG_FILE = "params.properties";
    private final Properties _props = new Properties();
    private final BuildProgressLogger _logger;

    public RunnerParamsProvider(BuildProgressLogger logger)
    {
        _logger = logger;
    }

    public RunnerParamsProvider(Map<String, String> runnerParams, BuildProgressLogger logger)
    {
        this(logger);
        for (Map.Entry<String, String> entry : runnerParams.entrySet())
        {
            set(entry.getKey(), entry.getValue());
        }
    }

    public void logProperties() { _logger.message("RunnerParams: " + _props.toString()); }

    public void dumpProperties()
    {
        try
        {
            _props.store(new FileOutputStream(CONFIG_FILE), "set parameters");
        }
        catch (IOException e)
        {
            _logger.warning("Unable to dump properties to " + CONFIG_FILE + ": " + e.getMessage());
        }
    }

    public BuildProgressLogger getLogger() { return _logger; }

    public String get(String key)
    {
        return _props.getProperty(key);
    }

    public void set(String key, String value)
    {
        _props.setProperty(key, value);
    }

    public String getJiraServerUrl() { return normalizeUrl(_props.getProperty("jiraServerUrl")); }

    public String getJiraUser() { return _props.getProperty("jiraUser"); }

    public String getJiraPassword() { return _props.getProperty("jiraPassword"); }

    public boolean getJiraWindowsAuth() { return Boolean.parseBoolean(_props.getProperty("jiraWindowsAuth")); }

    public boolean isSslConnectionEnabled() { return Boolean.parseBoolean(_props.getProperty("enableSSLConnection")); }

    public String getBuildTypeId() { return _props.getProperty("build.type.id"); }

    public String getJiraWorkFlow() { return _props.getProperty("jiraWorkflow"); }

    public String getTCServerUrl() { return normalizeUrl(_props.getProperty("tcServerUrl")); }

    public String getTCUser() { return _props.getProperty("tcUser"); }

    public String getTCPassword() { return _props.getProperty("tcPassword"); }

    public boolean getTCWindowsAuth() { return Boolean.parseBoolean(_props.getProperty("tcWindowsAuth")); }

    public boolean isProgressIssueEnabled() { return Boolean.parseBoolean(_props.getProperty("enableIssueProgressing")); }

    public String getBuildName() { return _props.getProperty("buildName"); }

    public String getTemplateComment() { return _props.getProperty("templateComment"); }

    public boolean isCommentingEnabled() {  return Boolean.parseBoolean(_props.getProperty("enableCommenting")); }

    public boolean isCommentTemplateEnabled() {  return Boolean.parseBoolean(_props.getProperty("enableTemplateComment")); }

    public boolean isLinkToBuildPageEnabled() {  return Boolean.parseBoolean(_props.getProperty("enableLinkToBuildPage")); }

    public boolean isAnyFeatureEnabled() {
        return isCommentingEnabled() || isLinkToBuildPageEnabled() || isProgressIssueEnabled();
    }

    private String normalizeUrl(String url)
    {
        if (url.endsWith("/") || url.endsWith("\\"))
            return url.substring(0, url.length() - 1);
        return url;
    }
}
