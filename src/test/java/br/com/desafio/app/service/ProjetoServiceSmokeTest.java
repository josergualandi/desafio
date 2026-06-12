package br.com.desafio.app.service;

import br.com.desafio.app.domain.AtribuicaoMembro;
import br.com.desafio.app.domain.ClassificacaoRisco;
import br.com.desafio.app.domain.Membro;
import br.com.desafio.app.domain.Projeto;
import br.com.desafio.app.domain.StatusProjeto;
import br.com.desafio.app.dto.ProjetoRequestDTO;
import br.com.desafio.app.exception.RegraNegocioException;
import br.com.desafio.app.mapper.ProjetoMapper;
import br.com.desafio.app.repository.MembroRepository;
import br.com.desafio.app.repository.ProjetoRepository;
import br.com.desafio.app.service.impl.ProjetoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjetoServiceSmokeTest {

	@Mock
	private ProjetoRepository projetoRepository;

	@Mock
	private MembroRepository membroRepository;

	private ProjetoServiceImpl projetoService;

	@BeforeEach
	void setUp() {
		projetoService = new ProjetoServiceImpl(projetoRepository, membroRepository, new ProjetoMapper());
	}

	@Test
	void deveCriarProjetoComRiscoBaixo() {
		Membro gerente = membro(1L, "Gerente A", AtribuicaoMembro.GERENTE);
		Membro funcionario = membro(2L, "Dev A", AtribuicaoMembro.FUNCIONARIO);

		ProjetoRequestDTO dto = new ProjetoRequestDTO(
				"Projeto Alpha",
				LocalDate.of(2026, 1, 1),
				LocalDate.of(2026, 3, 1),
				null,
				new BigDecimal("100000"),
				"Descrição",
				gerente.getId(),
				null,
				Set.of(funcionario.getId())
		);

		when(membroRepository.findById(gerente.getId())).thenReturn(Optional.of(gerente));
		when(membroRepository.findAllById(dto.membrosIds())).thenReturn(List.of(funcionario));
		when(projetoRepository.contarProjetosAtivosDoMembro(eq(funcionario.getId()), anySet(), isNull())).thenReturn(0L);
		when(projetoRepository.save(any(Projeto.class))).thenAnswer(invocation -> {
			Projeto projeto = invocation.getArgument(0);
			projeto.setId(100L);
			return projeto;
		});

		var resposta = projetoService.criar(dto);

		assertEquals(100L, resposta.id());
		assertEquals(StatusProjeto.EM_ANALISE, resposta.statusAtual());
		assertEquals(ClassificacaoRisco.BAIXO, resposta.classificacaoRisco());
		assertEquals(1, resposta.membros().size());
	}

	@Test
	void deveBloquearTransicaoDeStatusComPuloDeEtapa() {
		Membro gerente = membro(1L, "Gerente", AtribuicaoMembro.GERENTE);
		Membro funcionario = membro(2L, "Dev", AtribuicaoMembro.FUNCIONARIO);

		Projeto projetoExistente = projetoBase(10L, gerente, funcionario);
		projetoExistente.setStatusAtual(StatusProjeto.EM_ANALISE);

		ProjetoRequestDTO dto = new ProjetoRequestDTO(
				"Projeto Beta",
				LocalDate.of(2026, 1, 1),
				LocalDate.of(2026, 6, 1),
				null,
				new BigDecimal("200000"),
				"Descrição",
				gerente.getId(),
				StatusProjeto.INICIADO,
				Set.of(funcionario.getId())
		);

		when(projetoRepository.findById(projetoExistente.getId())).thenReturn(Optional.of(projetoExistente));
		when(membroRepository.findById(gerente.getId())).thenReturn(Optional.of(gerente));
		when(membroRepository.findAllById(dto.membrosIds())).thenReturn(List.of(funcionario));
		when(projetoRepository.contarProjetosAtivosDoMembro(eq(funcionario.getId()), anySet(), eq(projetoExistente.getId())))
				.thenReturn(0L);

		assertThrows(RegraNegocioException.class, () -> projetoService.atualizar(projetoExistente.getId(), dto));
		verify(projetoRepository, never()).save(any());
	}

	@Test
	void deveBloquearExclusaoDeProjetoEmAndamento() {
		Projeto projeto = new Projeto();
		projeto.setId(12L);
		projeto.setStatusAtual(StatusProjeto.EM_ANDAMENTO);

		when(projetoRepository.findById(12L)).thenReturn(Optional.of(projeto));

		assertThrows(RegraNegocioException.class, () -> projetoService.excluir(12L));
	}

	@Test
	void devePermitirCancelarProjetoMesmoComFluxoOriginal() {
		assertTrue(StatusProjeto.ANALISE_APROVADA.podeTransicionarPara(StatusProjeto.CANCELADO));
	}

	@Test
	void deveBloquearAssociacaoDeMembroNaoFuncionario() {
		Membro gerente = membro(1L, "Gerente", AtribuicaoMembro.GERENTE);
		Membro membroInvalido = membro(2L, "Outro Gerente", AtribuicaoMembro.GERENTE);

		ProjetoRequestDTO dto = new ProjetoRequestDTO(
				"Projeto Gama",
				LocalDate.of(2026, 1, 1),
				LocalDate.of(2026, 4, 1),
				null,
				new BigDecimal("120000"),
				"Descrição",
				gerente.getId(),
				StatusProjeto.EM_ANALISE,
				Set.of(membroInvalido.getId())
		);

		when(membroRepository.findById(gerente.getId())).thenReturn(Optional.of(gerente));
		when(membroRepository.findAllById(dto.membrosIds())).thenReturn(List.of(membroInvalido));

		assertThrows(RegraNegocioException.class, () -> projetoService.criar(dto));
	}

	@Test
	void deveBloquearMembroComTresProjetosAtivos() {
		Membro gerente = membro(1L, "Gerente", AtribuicaoMembro.GERENTE);
		Membro funcionario = membro(2L, "Dev", AtribuicaoMembro.FUNCIONARIO);

		ProjetoRequestDTO dto = new ProjetoRequestDTO(
				"Projeto Delta",
				LocalDate.of(2026, 1, 1),
				LocalDate.of(2026, 8, 1),
				null,
				new BigDecimal("300000"),
				"Descrição",
				gerente.getId(),
				StatusProjeto.EM_ANALISE,
				Set.of(funcionario.getId())
		);

		when(membroRepository.findById(gerente.getId())).thenReturn(Optional.of(gerente));
		when(membroRepository.findAllById(dto.membrosIds())).thenReturn(List.of(funcionario));
		when(projetoRepository.contarProjetosAtivosDoMembro(eq(funcionario.getId()), anySet(), isNull())).thenReturn(3L);

		assertThrows(RegraNegocioException.class, () -> projetoService.criar(dto));
	}

	@Test
	void deveGerarResumoDoPortfolio() {
		Membro gerente = membro(1L, "Gerente", AtribuicaoMembro.GERENTE);
		Membro funcionario = membro(2L, "Dev", AtribuicaoMembro.FUNCIONARIO);

		Projeto emAnalise = projetoBase(20L, gerente, funcionario);
		emAnalise.setStatusAtual(StatusProjeto.EM_ANALISE);
		emAnalise.setOrcamentoTotal(new BigDecimal("1000"));

		Projeto encerrado = projetoBase(21L, gerente, funcionario);
		encerrado.setStatusAtual(StatusProjeto.ENCERRADO);
		encerrado.setDataRealTermino(LocalDate.of(2026, 1, 11));
		encerrado.setOrcamentoTotal(new BigDecimal("2000"));

		when(projetoRepository.findAll()).thenReturn(List.of(emAnalise, encerrado));
		when(projetoRepository.findAllByStatusAtualAndDataRealTerminoIsNotNull(StatusProjeto.ENCERRADO))
				.thenReturn(List.of(encerrado));
		when(projetoRepository.contarMembrosUnicosAlocados()).thenReturn(1L);

		var resumo = projetoService.gerarResumoPortfolio();

		assertEquals(1L, resumo.totalMembrosUnicosAlocados());
		assertEquals(10.0, resumo.mediaDuracaoProjetosEncerradosDias());

		var statusEmAnalise = resumo.resumoPorStatus().stream()
				.filter(item -> item.status() == StatusProjeto.EM_ANALISE)
				.findFirst()
				.orElseThrow();

		assertEquals(1L, statusEmAnalise.quantidadeProjetos());
		assertEquals(new BigDecimal("1000"), statusEmAnalise.totalOrcado());
	}

	private Membro membro(Long id, String nome, AtribuicaoMembro atribuicao) {
		Membro membro = new Membro();
		membro.setId(id);
		membro.setNome(nome);
		membro.setAtribuicao(atribuicao);
		return membro;
	}

	private Projeto projetoBase(Long id, Membro gerente, Membro funcionario) {
		Projeto projeto = new Projeto();
		projeto.setId(id);
		projeto.setNome("Projeto");
		projeto.setDataInicio(LocalDate.of(2026, 1, 1));
		projeto.setPrevisaoTermino(LocalDate.of(2026, 5, 1));
		projeto.setOrcamentoTotal(new BigDecimal("200000"));
		projeto.setDescricao("Descrição");
		projeto.setGerenteResponsavel(gerente);
		projeto.setMembros(Set.of(funcionario));
		return projeto;
	}
}
