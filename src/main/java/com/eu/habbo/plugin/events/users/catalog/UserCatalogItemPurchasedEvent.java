package com.eu.habbo.plugin.events.users.catalog;

import com.eu.habbo.habbohotel.catalog.CatalogItem;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import gnu.trove.set.hash.THashSet;

import java.util.List;

public class UserCatalogItemPurchasedEvent extends UserCatalogEvent {

    public final THashSet<HabboItem> itemsList;

    public int totalCredits;

    public int totalPixels;

    public int totalPoints;

    public List<String> badges;

    public UserCatalogItemPurchasedEvent(Habbo habbo, CatalogItem catalogItem, THashSet<HabboItem> itemsList, int totalCredits, int totalPixels, int totalPoints, List<String> badges) {
        super(habbo, catalogItem);

        this.itemsList = itemsList;
        this.totalCredits = totalCredits;
        this.totalPixels = totalPixels;
        this.totalPoints = totalPoints;
        this.badges = badges;
    }
}