package com.amirov.jirareporter.jira;

import com.atlassian.jira.rest.client.internal.json.JsonObjectParser;
import com.atlassian.jira.rest.client.internal.json.JsonParseUtil;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.net.URI;

public class RemoteLinkJsonParser implements JsonObjectParser<RemoteLink>
{
    @Override
    public RemoteLink parse(JSONObject json) throws JSONException
    {
        final URI selfUri = JsonParseUtil.getSelfUri(json);
        final Long id = JsonParseUtil.getOptionalLong(json, "id");

        return new RemoteLink(selfUri, id);
    }
}
