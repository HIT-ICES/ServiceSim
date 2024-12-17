package org.customBuilder.simulation;

import java.util.UUID;

import org.customBuilder.exception.ParserException;

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
public class Basic {
    private UUID id;

    private String name;

    public Basic(JSONObject config, String path) {
        if (!config.containsKey("id")) {
            throw new ParserException("config parse error, please check: " + (path + ".id"));
        }
        this.id = UUID.fromString(config.getString("id"));
        if (!config.containsKey("name")) {
            throw new ParserException("config parse error, please check: " + (path + ".name"));
        }
        this.name = config.getString("name");
    }
}
