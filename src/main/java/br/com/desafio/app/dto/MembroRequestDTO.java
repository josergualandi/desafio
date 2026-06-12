package br.com.desafio.app.dto;

import br.com.desafio.app.domain.AtribuicaoMembro;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MembroRequestDTO(
        @NotBlank(message = "Nome é obrigatório")
        String nome,
        @NotNull(message = "Atribuição é obrigatória")
        AtribuicaoMembro atribuicao
) {
}
