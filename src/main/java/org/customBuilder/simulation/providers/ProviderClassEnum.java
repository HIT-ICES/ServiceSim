package org.customBuilder.simulation.providers;

import org.customBuilder.exception.ParserException;

public enum ProviderClassEnum {
    DEVICES, SERVICES;

    public static ProviderClassEnum fromString(String text) {
        for (ProviderClassEnum b : ProviderClassEnum.values()) {
            if (b.toString().equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new ParserException("Provider class not found: " + text);
    }
}
