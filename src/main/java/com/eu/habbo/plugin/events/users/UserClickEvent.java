package com.eu.habbo.plugin.events.users;

import com.eu.habbo.habbohotel.users.Habbo;

public class UserClickEvent extends UserEvent {
    public Habbo ownHabbo;

    public UserClickEvent(Habbo ownHabbo, Habbo target) {
        super(target);
        this.ownHabbo = ownHabbo;
    }
}
