package br.com.desafio.app.dto;

import br.com.desafio.app.domain.StatusProjeto;

import java.math.BigDecimal;

public record StatusResumoDTO(StatusProjeto status, long quantidadeProjetos, BigDecimal totalOrcado) {
}
