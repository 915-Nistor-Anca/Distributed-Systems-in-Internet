package org.example.util;


import org.example.abstractions.AbstractionType;

public class AbstractionIdUtil {
    // fiecare abstractie are un abstractionId, de ex. app. app.beb
    public static final String HUB_ID = "hub";

    public static String getChildAbstractionId(String parentAbstractionId, AbstractionType childAbstractionType) {
        return parentAbstractionId + "." + childAbstractionType.getId();
    } // ex.: (app, BEB) => app.beb

    public static String getParentAbstractionId(String childAbstractionId) {
        return childAbstractionId.substring(0, childAbstractionId.lastIndexOf("."));
    } // ex.: (app.beb.pl) => app.beb

    public static String getNamedAbstractionId(String parentAbstractionId, AbstractionType abstractionType, String name) {
        return getChildAbstractionId(parentAbstractionId, abstractionType) + "[" + name + "]";
    } // ex.: (app.beb, PL, abc-2) => app.beb.pl[abc-2]

    public static String getNamedAncestorAbstractionId(String abstractionId) {
        return abstractionId.substring(0, abstractionId.indexOf("]") + 1);
    } // ex.: (app.beb.pl[abc-2].uc) => app.beb.pl[abc-2]

    public static String getInternalNameFromAbstractionId(String abstractionId) {
        return abstractionId.substring(abstractionId.indexOf("[") + 1, abstractionId.indexOf("]"));
    } //ex.: (app.beb.pl[abc-2]) => abc-2
}
