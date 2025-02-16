package org.customBuilder.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.customBuilder.exception.ParserException;

import lombok.Data;

@Data
public class ChildNode {
    private final NodeType type;
    private List<Node> listNode;
    private Map<String, Node> mapNode;

    public static boolean hasIndex(String node) {
        Pattern pattern = Pattern.compile("^(.+)\\[(\\d+)\\]$");
        Matcher matcher = pattern.matcher(node);
        return matcher.find();
    }

    public static String getNodeName(String node) {
        Pattern pattern = Pattern.compile("^(.+)\\[(\\d+)\\]$");
        Matcher matcher = pattern.matcher(node);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return node;
    }

    public static int getIndex(String node) {
        Pattern pattern = Pattern.compile("^(.+)\\[(\\d+)\\]$");
        Matcher matcher = pattern.matcher(node);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(2));
        }
        return -1;
    }

    public ChildNode(NodeType type) {
        this.type = type;
        if (type == NodeType.LIST) {
            listNode = new ArrayList<>();
        } else if (type == NodeType.MAP) {
            mapNode = new HashMap<>();
        } else {
            throw new ParserException("未知的节点类型: " + type);
        }
    }

    public boolean hasChild(String node) {
        if (type == NodeType.LIST) {
            try {
                int index = Integer.parseInt(node);
                return index >= 0 && index < listNode.size();
            } catch (NumberFormatException e) {
                throw new ParserException("节点名称不是数字: " + node);
            }
        }
        if (type == NodeType.MAP) {
            // 正则匹配^.*?\[\d+\]$，判断是否需要获取数组下标
            if (hasIndex(node)) {
                String nodeName = getNodeName(node);
                int index = getIndex(node);
                if (mapNode.containsKey(nodeName)) {
                    return mapNode.get(nodeName).hasChild(String.valueOf(index));
                }
                return false;
            }
            return mapNode.containsKey(node);
        }
        throw new ParserException("未知的节点类型: " + type);
    }

    public void addChild(String node) {
        if (type == NodeType.LIST) {
            try {
                int index = Integer.parseInt(node);
                if (index < 0) {
                    throw new ParserException("节点名称不能为负数: " + node);
                }
                while (listNode.size() <= index) {
                    listNode.add(new Node());
                }
            } catch (NumberFormatException e) {
                throw new ParserException("节点名称不是数字: " + node);
            }
        } else if (type == NodeType.MAP) {
            // 正则匹配^.*?\[\d+\]$，判断是否需要获取数组下标
            if (hasIndex(node)) {
                String nodeName = getNodeName(node);
                int index = getIndex(node);
                if (!mapNode.containsKey(nodeName)) {
                    mapNode.put(nodeName, new Node());
                }
                mapNode.get(nodeName).addChild(String.valueOf(index));
            } else {
                if (!mapNode.containsKey(node)) {
                    mapNode.put(node, new Node());
                }
            }
        } else {
            throw new ParserException("未知的节点类型: " + type);
        }
    }

    public Node getChild(String node) {
        if (!hasChild(node)) {
            throw new ParserException("节点不存在: " + node);
        }
        if (type == NodeType.LIST) {
            return listNode.get(Integer.parseInt(node));
        }
        if (type == NodeType.MAP) {
            // 正则匹配^.*?\[\d+\]$，判断是否需要获取数组下标
            if (hasIndex(node)) {
                String nodeName = getNodeName(node);
                int index = getIndex(node);
                return mapNode.get(nodeName).getChild(String.valueOf(index));
            }
            return mapNode.get(node);
        }
        throw new ParserException("未知的节点类型: " + type);
    }
}
