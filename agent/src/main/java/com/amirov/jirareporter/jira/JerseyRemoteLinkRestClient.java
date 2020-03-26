package com.amirov.jirareporter.jira;

import com.atlassian.jira.rest.client.RestClientException;
import com.atlassian.jira.rest.client.internal.jersey.AbstractJerseyRestClient;
import com.sun.jersey.client.apache.ApacheHttpClient;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class JerseyRemoteLinkRestClient extends AbstractJerseyRestClient
{
    private final RemoteLinkJsonParser remoteLinkJsonParser = new RemoteLinkJsonParser();

    public JerseyRemoteLinkRestClient(URI baseUri, ApacheHttpClient client)
    {
        super(baseUri, client);
    }

    public void makeRemoteLink(String issueKey, String url, String title)
    {
        try
        {
            final UriBuilder uriBuilder = UriBuilder.fromUri(baseUri);
            uriBuilder.path("issue").path(issueKey).path("remotelink");

            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("url", url);
            jsonObject.put("title", title);

            final JSONObject json = new JSONObject();
            json.put("object", jsonObject);

            this.postAndParse(uriBuilder.build(), json, remoteLinkJsonParser, null);
        }
        catch (Exception ex)
        {
            throw new RestClientException(ex);
        }
    }
}
