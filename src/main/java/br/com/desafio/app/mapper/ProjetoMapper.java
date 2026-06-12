package br.com.desafio.app.mapper;

import br.com.desafio.app.domain.ClassificacaoRisco;
import br.com.desafio.app.domain.Membro;
import br.com.desafio.app.domain.Projeto;
import br.com.desafio.app.dto.MembroResumoDTO;
import br.com.desafio.app.dto.ProjetoResponseDTO;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class ProjetoMapper {

	public ProjetoResponseDTO toResponse(Projeto projeto, ClassificacaoRisco risco) {
		return new ProjetoResponseDTO(
				projeto.getId(),
				projeto.getNome(),
				projeto.getDataInicio(),
				projeto.getPrevisaoTermino(),
				projeto.getDataRealTermino(),
				projeto.getOrcamentoTotal(),
				projeto.getDescricao(),
				toResumo(projeto.getGerenteResponsavel()),
				projeto.getStatusAtual(),
				risco,
				projeto.getMembros().stream()
						.sorted(Comparator.comparing(Membro::getId))
						.map(this::toResumo)
						.toList()
		);
	}

	public MembroResumoDTO toResumo(Membro membro) {
		return new MembroResumoDTO(membro.getId(), membro.getNome(), membro.getAtribuicao());
	}
}
