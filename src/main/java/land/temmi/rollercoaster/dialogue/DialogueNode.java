package land.temmi.rollercoaster.dialogue;

import com.badlogic.gdx.utils.Array;

/** One line (or branch point) of a dialogue. Text and portrait are translation/asset keys - the
 * game resolves them, the engine only carries stable IDs. */
public final class DialogueNode {
    public final String id;
    public final String speakerId;
    public final String textId;
    public final String portrait;
    public final Array<DialogueResponse> responses;

    public DialogueNode(String id, String speakerId, String textId, String portrait, Array<DialogueResponse> responses) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("Node id is required");
        if (speakerId == null || speakerId.trim().isEmpty()) throw new IllegalArgumentException("Speaker id is required: " + id);
        if (textId == null || textId.trim().isEmpty()) throw new IllegalArgumentException("Text id is required: " + id);
        this.id = id;
        this.speakerId = speakerId;
        this.textId = textId;
        this.portrait = portrait;
        this.responses = responses == null ? new Array<>() : responses;
    }
}
