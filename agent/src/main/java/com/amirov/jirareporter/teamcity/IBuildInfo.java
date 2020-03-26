package com.amirov.jirareporter.teamcity;

public interface IBuildInfo
{
    String getBuildId();
    String getBuildStatus();
    String getBuildNumber ();
    String getBuildName();
    String getBuildHref();
    String getWebUrl();

    String buildCommentText();
}
