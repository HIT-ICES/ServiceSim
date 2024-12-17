package org.customBuilder;

import org.customBuilder.exception.ParserException;
import org.customBuilder.simulation.Basic;
import org.customBuilder.simulation.Providers;

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
public class Simulation {
    private Basic basic;
    private Providers providers;

    public Simulation(JSONObject config, String path) {
        if (config == null) {
            throw new ParserException("config parse error, please check: " + path);
        }
        if (!config.containsKey("basic")) {
            throw new ParserException("config parse error, please check: " + (path + ".basic"));
        }
        if (!config.containsKey("providers")) {
            throw new ParserException("config parse error, please check: " + (path + ".providers"));
        }
        this.basic = new Basic(config.getJSONObject("basic"), path + ".basic");
        this.providers = new Providers(config.getJSONArray("providers"), path + ".providers");
    }
}
