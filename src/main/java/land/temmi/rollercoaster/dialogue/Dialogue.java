package land.temmi.rollercoaster.dialogue;

import com.badlogic.gdx.utils.Array;

/** A whole conversation tree: a start node and every node it can reach. Branch targets are
 * validated against this same node set at construction time - unlike a map-to-map transition, a
 * dialogue's nodes are always known together, so there is no load-order hazard. */
public final class Dialogue {
    public final String id;
    public final String startNodeId;
    public final Array<DialogueNode> nodes;

    public Dialogue(String id, String startNodeId, Array<DialogueNode> nodes) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("Dialogue id is required");
        if (startNodeId == null || startNodeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Start node id is required: " + id);
        }
        if (nodes == null || nodes.size == 0) throw new IllegalArgumentException("Dialogue must have at least one node: " + id);
        boolean foundStart = false;
        for (DialogueNode node : nodes) {
            if (node.id.equals(startNodeId)) foundStart = true;
            for (DialogueResponse response : node.responses) {
                if (response.targetNodeId != null && !containsNode(nodes, response.targetNodeId)) {
                    throw new IllegalArgumentException(
                        "Dialogue '" + id + "' response targets unknown node: " + response.targetNodeId);
                }
            }
        }
        if (!foundStart) throw new IllegalArgumentException("Dialogue '" + id + "' start node not found: " + startNodeId);
        this.id = id;
        this.startNodeId = startNodeId;
        this.nodes = nodes;
    }

    public DialogueNode node(String nodeId) {
        for (DialogueNode node : nodes) if (node.id.equals(nodeId)) return node;
        throw new IllegalArgumentException("Unknown dialogue node: " + nodeId);
    }

    private static boolean containsNode(Array<DialogueNode> nodes, String nodeId) {
        for (DialogueNode node : nodes) if (node.id.equals(nodeId)) return true;
        return false;
    }
}
