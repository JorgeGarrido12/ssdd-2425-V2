package es.um.sisdist.models;

import es.um.sisdist.backend.dao.models.Prompt;
import java.util.List;
import java.util.stream.Collectors;

public class PromptDTOUtils
{
    public static PromptDTO toDTO(Prompt prompt)
    {
        if (prompt == null) return null;

        return new PromptDTO(
            prompt.getPrompt(),
            prompt.getAnswer(),
            prompt.getTimestamp()
        );
    }

    public static Prompt fromDTO(PromptDTO dto)
    {
        if (dto == null) return null;

        return new Prompt(
            dto.getPrompt(),
            dto.getAnswer(),
            dto.getTimestamp()
        );
    }

    //Conversion en listas, las necesitaremos cuando tengamos listas de prompts

    public static List<PromptDTO> toDTOList(List<Prompt> prompts)
    {
        return prompts == null ? List.of() :
            prompts.stream().map(PromptDTOUtils::toDTO).collect(Collectors.toList());
    }

    public static List<Prompt> fromDTOList(List<PromptDTO> dtos)
    {
        return dtos == null ? List.of() :
            dtos.stream().map(PromptDTOUtils::fromDTO).collect(Collectors.toList());
    }
}
