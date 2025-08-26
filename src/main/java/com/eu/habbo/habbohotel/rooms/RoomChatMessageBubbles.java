package com.eu.habbo.habbohotel.rooms;

public enum RoomChatMessageBubbles {
    NORMAL(0, "", true, true),
    ALERT(1, "", true, true),
    BOT(2, "", true, true),
    RED(3, "", true, true),
    BLUE(4, "", true, true),
    YELLOW(5, "", true, true),
    GREEN(6, "", true, true),
    BLACK(7, "", true, true),
    FORTUNE_TELLER(8, "", false, false),
    ZOMBIE_ARM(9, "", true, false),
    SKELETON(10, "", true, false),
    LIGHT_BLUE(11, "", true, true),
    PINK(12, "", true, true),
    PURPLE(13, "", true, true),
    DARK_YEWLLOW(14, "", true, true),
    DARK_BLUE(15, "", true, true),
    HEARTS(16, "", true, true),
    ROSES(17, "", true, true),
    UNUSED(18, "", true, true), //?
    PIG(19, "", true, true),
    DOG(20, "", true, true),
    BLAZE_IT(21, "", true, true),
    DRAGON(22, "", true, true),
    STAFF(23, "", false, true),
    BATS(24, "", true, false),
    MESSENGER(25, "", true, false),
    STEAMPUNK(26, "", true, false),
    THUNDER(27, "", true, true),
    PARROT(28, "", false, false),
    PIRATE(29, "", false, false),
    BOT_GUIDE(30, "", true, true),
    BOT_RENTABLE(31, "", true, true),
    SCARY_THING(32, "", true, false),
    FRANK(33, "", true, false),
    WIRED(34, "", false, true),
    GOAT(35, "", true, false),
    SANTA(36, "", true, false),
    AMBASSADOR(37, "acc_ambassador", false, true),
    RADIO(38, "", true, false),
    PENCIL(39, "", true, false),
    STAR(40, "", true, false),
    DICE(41, "", true, false),
    BUILDER(42, "", true, false),
    COLLABORATOR(43, "acc_ambassador", false, false),
    GUIDE(44, "acc_ambassador", false, false),
    SYSTEM(45, "", true, false),
    STICKER(46, "", true, false),
    AUDIO(47, "", true, false),
    DARK1(48, "", true, true),
    DARK2(49, "", true, true),
    CUSTOM1(50, "", true, true),
    CUSTOM2(51, "", true, true),
    CUSTOM3(52, "", true, true),
    CUSTOM4(53, "", true, true),
    CUSTOM5(54, "", true, true),
    CUSTOM6(55, "", true, true),
    CUSTOM7(56, "", true, true),
    CUSTOM8(57, "", true, true),
    CUSTOM9(58, "", true, true),
    CUSTOM10(59, "", true, true),
    CUSTOM11(60, "", true, true),
    CUSTOM12(61, "", true, true),
    CUSTOM13(62, "", true, true),
    CUSTOM14(63, "", true, true),
    CUSTOM15(64, "", true, true),
    CUSTOM16(65, "", true, true),
    CUSTOM17(66, "", true, true),
    CUSTOM18(67, "", true, true),
    CUSTOM19(68, "", true, true),
    CUSTOM20(69, "", true, true),
    CUSTOM21(70, "", true, true),
    CUSTOM22(71, "", true, true),
    CUSTOM23(72, "", true, true),
    CUSTOM24(73, "", true, true),
    CUSTOM25(74, "", true, true),
    CUSTOM26(75, "", true, true),
    CUSTOM27(76, "", true, true),
    CUSTOM28(77, "", true, true),
    CUSTOM29(78, "", true, true),
    CUSTOM30(79, "", true, true),
    CUSTOM31(80, "", true, true),
    CUSTOM32(81, "", true, true),
    CUSTOM33(82, "", true, true),
    CUSTOM34(83, "", true, true),
    CUSTOM35(84, "", true, true),
    CUSTOM36(85, "", true, true),
    CUSTOM37(86, "", true, true),
    CUSTOM38(87, "", true, true),
    CUSTOM39(88, "", true, true),
    CUSTOM40(89, "", true, true),
    CUSTOM41(90, "", true, true),
    CUSTOM42(91, "", true, true),
    CUSTOM43(92, "", true, true),
    CUSTOM44(93, "", true, true),
    CUSTOM45(94, "", true, true),
    CUSTOM46(95, "", true, true),
    CUSTOM47(96, "", true, true),
    CUSTOM48(97, "", true, true),
    CUSTOM49(98, "", true, true),
    CUSTOM50(99, "", true, true),
    CUSTOM51(100, "", true, true),
    CUSTOM52(101, "", true, true),
    CUSTOM53(102, "", true, true),
    CUSTOM54(103, "", true, true);

    private final int type;
    private final String permission;
    private final boolean overridable;
    private final boolean triggersTalkingFurniture;

    RoomChatMessageBubbles(int type, String permission, boolean overridable, boolean triggersTalkingFurniture) {
        this.type = type;
        this.permission = permission;
        this.overridable = overridable;
        this.triggersTalkingFurniture = triggersTalkingFurniture;
    }

    public static RoomChatMessageBubbles getBubble(int bubbleId) {
        try {
            return values()[bubbleId];
        } catch (Exception e) {
            return NORMAL;
        }
    }

    public int getType() {
        return this.type;
    }

    public String getPermission() {
        return this.permission;
    }

    public boolean isOverridable() {
        return this.overridable;
    }

    public boolean triggersTalkingFurniture() {
        return this.triggersTalkingFurniture;
    }
}
