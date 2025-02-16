package org.customBuilder.factory;

public enum BuildType {
    BUILTIN,
    LUA,
    RESOURCE,
    TEMPLATE,
    REFERENCE,
    CLONE;

    public static BuildType fromString(String type) {
        switch (type) {
            case "BUILTIN":
                return BUILTIN;
            case "LUA":
                return LUA;
            case "RESOURCE":
                return RESOURCE;
            case "TEMPLATE":
                return TEMPLATE;
            case "REFERENCE":
                return REFERENCE;
            case "CLONE":
                return CLONE;
            default:
                throw new IllegalArgumentException("Unknown build type: " + type);
        }
    }
}
