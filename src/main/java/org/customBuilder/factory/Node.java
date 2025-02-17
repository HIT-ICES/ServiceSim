package org.customBuilder.factory;

import javax.management.ObjectName;

import org.customBuilder.exception.ParserException;

import com.alibaba.fastjson.JSONObject;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Node {
    private Object instance;
    private Object profile;
    private ChildNode childNode;

    public boolean hasChild(String node) {
        if (childNode == null) {
            return false;
        }
        return childNode.hasChild(node);
    }

    @SuppressWarnings("unused")
    public void addChild(String node) {
        if (childNode == null) {
            try {
                int index = Integer.parseInt(node);
                childNode = new ChildNode(NodeType.LIST);
            } catch (NumberFormatException e) {
                childNode = new ChildNode(NodeType.MAP);
            }
        }
        childNode.addChild(node);
    }

    public Node getChild(String node) {
        if (childNode == null) {
            throw new ParserException("节点不存在: " + node);
        }
        return childNode.getChild(node);
    }
}
