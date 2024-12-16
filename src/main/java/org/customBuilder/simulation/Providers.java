package org.customBuilder.simulation;

import java.util.ArrayList;
import java.util.List;

import org.customBuilder.exception.ParserException;
import org.customBuilder.simulation.providers.RootProvider;

import com.alibaba.fastjson.JSONArray;
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
public class Providers {
    private List<RootProvider> providers;

    public Providers(JSONArray providers, String path) {
        if (providers == null || providers.isEmpty()) {
            throw new ParserException("Providers not found: " + path);
        }
        this.providers = new ArrayList<>();
        int size = providers.size();
        for (int i = 0; i < size; i++) {
            JSONObject provider = providers.getJSONObject(i);
            if (provider == null) {
                throw new ParserException("Provider not found: " + path + "[" + i + "]");
            }
            this.providers.add(new RootProvider(provider, path + "[" + i + "]"));
        }
    }
}
