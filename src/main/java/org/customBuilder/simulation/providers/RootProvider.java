package org.customBuilder.simulation.providers;

import org.customBuilder.exception.ParserException;
import org.infrastructureProvider.DeviceProviderInterface;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class RootProvider {
    private String name;
    private ProviderClassEnum _class;
    private Provider provider;

    public RootProvider(JSONObject provider, String path) {
        if (provider == null || provider.isEmpty()) {
            throw new ParserException("Provider not found: " + path);
        }
        if (!provider.containsKey("name")) {
            throw new ParserException("Provider name not found: " + path);
        }
        if (!provider.containsKey("class")) {
            throw new ParserException("Provider class not found: " + path);
        }
        if (!provider.containsKey("provider")) {
            throw new ParserException("Provider provider not found: " + path);
        }
        this.name = provider.getString("name");
        this._class = ProviderClassEnum.fromString(provider.getString("class"));
        switch (this._class) {
            case DEVICES -> this.provider = new Provider(provider.getJSONObject("provider"), path + ".provider", DeviceProviderInterface.class);
            default -> throw new ParserException("Provider class: " + this._class + " not found: " + path);
        }
    }
}
