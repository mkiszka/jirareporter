package com.amirov.jirareporter.jira;

import com.amirov.jirareporter.RunnerParamsProvider;
import com.atlassian.jira.rest.client.AuthenticationHandler;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.Filterable;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import jetbrains.buildServer.agent.BuildProgressLogger;
import org.apache.http.auth.AuthScope;

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
    public void configure(ApacheHttpClientConfig clientConfig) {
        //_logger.message("[NTLMAuthenticationHandler] domain: " + _domain + ", user: " + _username + ", hostname: " + _hostname);
        clientConfig.getState().setCredentials(AuthScope.ANY_REALM, AuthScope.ANY_HOST, AuthScope.ANY_PORT, _username, _password, _domain, _hostname);
        clientConfig.getProperties().put(ApacheHttpClientConfig.PROPERTY_PREEMPTIVE_AUTHENTICATION, true);
    }

    @Override
    public void configure(Filterable filterable, Client client) { }
}
