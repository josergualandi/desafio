package br.com.desafio.app.dto;

import br.com.desafio.app.domain.StatusProjeto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public record ProjetoRequestDTO(
	@NotBlank(message = "Nome é obrigatório")
	String nome,

	@NotNull(message = "Data de início é obrigatória")
	LocalDate dataInicio,

	@NotNull(message = "Previsão de término é obrigatória")
	LocalDate previsaoTermino,

	LocalDate dataRealTermino,

	@NotNull(message = "Orçamento total é obrigatório")
	@DecimalMin(value = "0.0", inclusive = true, message = "Orçamento deve ser maior ou igual a zero")
	BigDecimal orcamentoTotal,

	@NotBlank(message = "Descrição é obrigatória")
	String descricao,

	@NotNull(message = "Gerente responsável é obrigatório")
	Long gerenteResponsavelId,

	StatusProjeto statusAtual,

	@NotEmpty(message = "Informe ao menos um membro")
	@Size(min = 1, max = 10, message = "Projeto deve ter entre 1 e 10 membros")
	Set<Long> membrosIds
) {
}
