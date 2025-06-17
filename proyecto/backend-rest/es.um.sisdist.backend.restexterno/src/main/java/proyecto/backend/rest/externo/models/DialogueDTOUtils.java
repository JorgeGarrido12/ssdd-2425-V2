package proyecto.backend.rest.externo.models;

import es.um.sisdist.backend.dao.models.Dialogue;
import es.um.sisdist.backend.dao.models.DialogueEstados;
import es.um.sisdist.backend.dao.models.Prompt;

import java.util.List;

public class DialogueDTOUtils {

    public static DialogueDTO toDTO(Dialogue d) {
        if (d == null) return null;

        return new DialogueDTO(
            d.getDialogueId(),
            d.getStatus(),
            PromptDTOUtils.toDTOList(d.getDialogue()),
            d.getNextUrl(),
            d.getEndUrl()
        );
    }

    public static Dialogue fromDTO(DialogueDTO dto) {
        if (dto == null) return null;

        Dialogue d = new Dialogue();
        d.setDialogueId(dto.getDialogueId());
        d.setStatus(dto.getStatus() != null ? dto.getStatus() : DialogueEstados.READY);

        List<Prompt> prompts = PromptDTOUtils.fromDTOList(dto.getDialogue());
        d.setDialogue(prompts);

        d.setNextUrl(dto.getNextUrl());
        d.setEndUrl(dto.getEndUrl());

        return d;
    }
}
