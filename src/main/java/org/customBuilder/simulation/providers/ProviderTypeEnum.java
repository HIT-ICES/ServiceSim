package org.customBuilder.simulation.providers;

import org.customBuilder.exception.ParserException;

public enum ProviderTypeEnum {
    TEMPLATE, SCRIPT, BUILTIN, RESOURCE, REFRENCE, CLONE, INLINE;

    public static ProviderTypeEnum fromString(String text) {
        for (ProviderTypeEnum b : ProviderTypeEnum.values()) {
            if (b.toString().equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new ParserException("Provider type not found: " + text);
    }
}
