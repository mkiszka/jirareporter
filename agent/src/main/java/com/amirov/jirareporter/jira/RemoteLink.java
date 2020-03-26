package com.amirov.jirareporter.jira;

import com.atlassian.jira.rest.client.AddressableEntity;
import com.google.common.base.Objects;

import java.net.URI;

public class RemoteLink implements AddressableEntity
{
    private final URI self;
    private final Long id;

    public RemoteLink(URI self, Long id)
    {
        this.self = self;
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @Override
    public URI getSelf() {
        return self;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("self", self)
                .add("id", id)
                .toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof RemoteLink)
        {
            RemoteLink that = (RemoteLink) obj;
            return Objects.equal(this.self, that.self) && Objects.equal(this.id, that.id);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(self, id);
    }
}
