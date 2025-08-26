package com.eu.habbo.messages.outgoing.users;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

public class UserDataComposer extends MessageComposer {
    private final Habbo habbo;

    public UserDataComposer(Habbo habbo) {
        this.habbo = habbo;
    }

    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.UserDataComposer);

        this.response.appendInt(this.habbo.getHabboInfo().getId());
        this.response.appendString(this.habbo.getHabboInfo().getUsername());
        this.response.appendInt(this.habbo.getHabboInfo().getRank().getId());
        this.response.appendString(this.habbo.getHabboInfo().getLook());
        this.response.appendString(this.habbo.getHabboInfo().getGender().name().toUpperCase());
        this.response.appendString(this.habbo.getHabboInfo().getMotto());
        this.response.appendString(this.habbo.getHabboInfo().getUsername());
        this.response.appendInt(this.habbo.getHabboStats().getBannerId());
        this.response.appendString(this.habbo.getHabboStats().getBannerColor());
        this.response.appendInt(this.habbo.getHabboStats().getOrnamentId());
        this.response.appendInt(this.habbo.getHabboStats().getColorId());
        this.response.appendInt(this.habbo.getHabboStats().getUsernameIconId());
        this.response.appendInt(this.habbo.getHabboStats().getUsernameEffectId());
        this.response.appendInt(this.habbo.getHabboStats().hasActiveVip());
        this.response.appendInt(this.habbo.getHabboStats().respectPointsReceived);
        this.response.appendInt(this.habbo.getHabboStats().getKissesReceived());
        this.response.appendString(this.habbo.getHabboStats().getProfileParameters());
        this.response.appendBoolean(false);
        this.response.appendInt(this.habbo.getHabboStats().respectPointsReceived);
        this.response.appendInt(this.habbo.getHabboStats().respectPointsToGive);
        this.response.appendInt(this.habbo.getHabboStats().petRespectPointsToGive);
        this.response.appendBoolean(false);
        this.response.appendString("01-01-1970 00:00:00");
        this.response.appendBoolean(this.habbo.getHabboStats().allowNameChange);
        this.response.appendBoolean(false);

        return this.response;
    }

    public Habbo getHabbo() {
        return habbo;
    }
}
