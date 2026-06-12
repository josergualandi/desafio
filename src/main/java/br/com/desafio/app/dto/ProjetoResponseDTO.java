package br.com.desafio.app.dto;

import br.com.desafio.app.domain.ClassificacaoRisco;
import br.com.desafio.app.domain.StatusProjeto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ProjetoResponseDTO(
	Long id,
	String nome,
	LocalDate dataInicio,
	LocalDate previsaoTermino,
	LocalDate dataRealTermino,
	BigDecimal orcamentoTotal,
	String descricao,
	MembroResumoDTO gerenteResponsavel,
	StatusProjeto statusAtual,
	ClassificacaoRisco classificacaoRisco,
	List<MembroResumoDTO> membros
) {
}
