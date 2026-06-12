package br.com.desafio.app.dto;

import br.com.desafio.app.domain.AtribuicaoMembro;

public record MembroResumoDTO(Long id, String nome, AtribuicaoMembro atribuicao) {
}
