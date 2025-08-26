package com.eu.habbo.messages.outgoing.users;

import com.eu.habbo.habbohotel.users.HabboBadge;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

public class RemoveUserBadgeComposer extends MessageComposer {
    private final HabboBadge badge;

    public RemoveUserBadgeComposer(HabboBadge badge) {
        this.badge = badge;
    }

    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RemoveUserBadgeComposer);
        this.response.appendInt(this.badge.getId());
        this.response.appendString(this.badge.getCode());
        return this.response;
    }
}