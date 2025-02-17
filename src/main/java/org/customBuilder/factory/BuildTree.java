package org.customBuilder.factory;

public class BuildTree {
    private Node root;

    public BuildTree() {
        root = new Node();
    }

    public void set(String context, Object profile, Object instance) {
        String[] nodeList = context.split("\\.");
        Node currentNode = root;

        for (String node : nodeList) {
            if (node.isEmpty()) {
                continue;
            }
            // 判断当前节点是否存在
            if (!currentNode.hasChild(node)) {
                currentNode.addChild(node);
            }
            currentNode = currentNode.getChild(node);
        }

        currentNode.setProfile(profile);
        currentNode.setInstance(instance);
    }

    public Node get(String context) {
        String[] nodeList = context.split("\\.");
        Node currentNode = root;

        for (String node : nodeList) {
            if (node.isEmpty()) {
                continue;
            }
            if (!currentNode.hasChild(node)) {
                return null;
            }
            currentNode = currentNode.getChild(node);
        }

        return currentNode;
    }
}