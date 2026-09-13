package land.temmi.rollercoaster.dialogue;

import com.badlogic.gdx.utils.Array;
import land.temmi.rollercoaster.event.Condition;

/** One answer a player can pick. A null target ends the dialogue; otherwise it jumps to another
 * node in the same dialogue. */
public final class DialogueResponse {
    public final String textId;
    public final String targetNodeId;
    public final Array<Condition> conditions;

    public DialogueResponse(String textId, String targetNodeId, Array<Condition> conditions) {
        if (textId == null || textId.trim().isEmpty()) throw new IllegalArgumentException("Response text ID is required");
        this.textId = textId;
        this.targetNodeId = targetNodeId;
        this.conditions = conditions == null ? new Array<>() : conditions;
    }
}
