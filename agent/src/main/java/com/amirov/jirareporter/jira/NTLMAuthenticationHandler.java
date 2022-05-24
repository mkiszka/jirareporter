package com.amirov.jirareporter.jira;

import com.amirov.jirareporter.RunnerParamsProvider;
import com.atlassian.httpclient.api.Request;

import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import jetbrains.buildServer.agent.BuildProgressLogger;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NTLMAuthenticationHandler implements AuthenticationHandler {

    private final String BACKSLASH = "\\\\";
    private final String _domain;
    private final String _username;
    private final String _password;
    private final BuildProgressLogger _logger;
    private String _hostname;

    public NTLMAuthenticationHandler(RunnerParamsProvider prmsProvider)
    {
        _logger = prmsProvider.getLogger();
        String username = prmsProvider.getJiraUser();

        String[] parts = username.split(BACKSLASH);
        if (parts.length > 1)
        {
            _domain =  parts[0];
            _username =  parts[1];
        }
        else { _username = username; _domain = null; }
        this._password = prmsProvider.getJiraPassword();

        try
        {
            InetAddress addr = InetAddress.getLocalHost();
            this._hostname = addr.getHostName();
        }
        catch (UnknownHostException ex)
        {
            this._hostname = System.getProperty("system.agent.name");
            _logger.warning("Hostname can not be resolved: " + ex.getMessage() + ". System property 'system.agent.name' was used. Actual hostname: " + _hostname);
        }
    }

    @Override
    public void configure(Request.Builder builder) {
        throw new NotImplementedException();
    }
}
