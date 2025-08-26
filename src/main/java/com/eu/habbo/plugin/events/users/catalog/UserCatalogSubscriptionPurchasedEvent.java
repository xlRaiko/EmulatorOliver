package com.eu.habbo.plugin.events.users.catalog;

import com.eu.habbo.habbohotel.catalog.ClubOffer;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.plugin.Event;

public class UserCatalogSubscriptionPurchasedEvent extends Event {
    public final Habbo habbo;
    public final ClubOffer clubOffer;
    public final int totalCredits;
    public final int totalPixels;
    public final int totalPoints;

    public UserCatalogSubscriptionPurchasedEvent(Habbo habbo, ClubOffer clubOffer, int totalCredits, int totalPixels, int totalPoints) {
        this.habbo = habbo;
        this.clubOffer = clubOffer;
        this.totalCredits = totalCredits;
        this.totalPixels = totalPixels;
        this.totalPoints = totalPoints;
    }
}