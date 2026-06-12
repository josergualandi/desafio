package br.com.desafio.app.dto.response;

import br.com.desafio.app.domain.AtribuicaoMembro;

public record MembroResponseDTO(Long id, String nome, AtribuicaoMembro atribuicao) {
}
