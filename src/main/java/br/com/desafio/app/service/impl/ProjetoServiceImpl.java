package br.com.desafio.app.service.impl;

import br.com.desafio.app.domain.AtribuicaoMembro;
import br.com.desafio.app.domain.ClassificacaoRisco;
import br.com.desafio.app.domain.Membro;
import br.com.desafio.app.domain.Projeto;
import br.com.desafio.app.domain.StatusProjeto;
import br.com.desafio.app.dto.response.PortfolioResumoDTO;
import br.com.desafio.app.dto.request.ProjetoRequestDTO;
import br.com.desafio.app.dto.response.ProjetoResponseDTO;
import br.com.desafio.app.dto.response.StatusResumoDTO;
import br.com.desafio.app.exception.NotFoundException;
import br.com.desafio.app.exception.RegraNegocioException;
import br.com.desafio.app.mapper.ProjetoMapper;
import br.com.desafio.app.repository.MembroRepository;
import br.com.desafio.app.repository.ProjetoRepository;
import br.com.desafio.app.service.ProjetoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjetoServiceImpl implements ProjetoService {

	private static final Set<StatusProjeto> STATUS_ATIVOS =
			EnumSet.complementOf(EnumSet.of(StatusProjeto.ENCERRADO, StatusProjeto.CANCELADO));

	private static final Set<StatusProjeto> STATUS_BLOQUEIAM_EXCLUSAO =
			EnumSet.of(StatusProjeto.INICIADO, StatusProjeto.EM_ANDAMENTO, StatusProjeto.ENCERRADO);

	private final ProjetoRepository projetoRepository;
	private final MembroRepository membroRepository;
	private final ProjetoMapper projetoMapper;

	@Override
	public ProjetoResponseDTO criar(ProjetoRequestDTO dto) {
		validarDatas(dto);

		Membro gerente = buscarGerente(dto.gerenteResponsavelId());
		Set<Membro> membros = buscarMembros(dto.membrosIds());
		validarMembros(membros, null);

		Projeto projeto = new Projeto();
		preencherProjeto(projeto, dto, gerente, membros, true);

		Projeto salvo = projetoRepository.save(projeto);
		return projetoMapper.toResponse(salvo, calcularRisco(salvo));
	}

	@Override
	public ProjetoResponseDTO buscarPorId(Long id) {
		Projeto projeto = buscarProjeto(id);
		return projetoMapper.toResponse(projeto, calcularRisco(projeto));
	}

	@Override
	public Page<ProjetoResponseDTO> listar(String nome, StatusProjeto status, Pageable pageable) {
		Specification<Projeto> spec = Specification.where(null);

		if (nome != null && !nome.isBlank()) {
			spec = spec.and((root, query, cb) ->
					cb.like(cb.lower(root.get("nome")), "%" + nome.toLowerCase() + "%"));
		}

		if (status != null) {
			spec = spec.and((root, query, cb) -> cb.equal(root.get("statusAtual"), status));
		}

		return projetoRepository.findAll(spec, pageable)
				.map(projeto -> projetoMapper.toResponse(projeto, calcularRisco(projeto)));
	}

	@Override
	public ProjetoResponseDTO atualizar(Long id, ProjetoRequestDTO dto) {
		Projeto projeto = buscarProjeto(id);
		validarDatas(dto);

		Membro gerente = buscarGerente(dto.gerenteResponsavelId());
		Set<Membro> membros = buscarMembros(dto.membrosIds());
		validarMembros(membros, projeto.getId());

		preencherProjeto(projeto, dto, gerente, membros, false);

		Projeto salvo = projetoRepository.save(projeto);
		return projetoMapper.toResponse(salvo, calcularRisco(salvo));
	}

	@Override
	public void excluir(Long id) {
		Projeto projeto = buscarProjeto(id);

		if (STATUS_BLOQUEIAM_EXCLUSAO.contains(projeto.getStatusAtual())) {
			throw new RegraNegocioException("Não é permitido excluir projeto com status iniciado, em andamento ou encerrado");
		}

		projetoRepository.delete(projeto);
	}

	@Override
	public PortfolioResumoDTO gerarResumoPortfolio() {
		var projetos = projetoRepository.findAll();

		Map<StatusProjeto, java.util.List<Projeto>> porStatus = projetos.stream()
				.collect(Collectors.groupingBy(Projeto::getStatusAtual));

		var resumoPorStatus = Arrays.stream(StatusProjeto.values())
				.map(status -> {
					var lista = porStatus.getOrDefault(status, java.util.List.of());
					BigDecimal total = lista.stream()
							.map(Projeto::getOrcamentoTotal)
							.reduce(BigDecimal.ZERO, BigDecimal::add);

					return new StatusResumoDTO(status, lista.size(), total);
				})
				.toList();

		var encerrados = projetoRepository.findAllByStatusAtualAndDataRealTerminoIsNotNull(StatusProjeto.ENCERRADO);
		double mediaDuracao = encerrados.stream()
				.mapToLong(p -> ChronoUnit.DAYS.between(p.getDataInicio(), p.getDataRealTermino()))
				.average()
				.orElse(0.0);

		long totalMembrosUnicos = projetoRepository.contarMembrosUnicosAlocados();

		return new PortfolioResumoDTO(resumoPorStatus, mediaDuracao, totalMembrosUnicos);
	}

	private Projeto buscarProjeto(Long id) {
		return projetoRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Projeto não encontrado"));
	}

	private Membro buscarGerente(Long gerenteId) {
		Membro gerente = membroRepository.findById(gerenteId)
				.orElseThrow(() -> new NotFoundException("Gerente responsável não encontrado"));

		if (gerente.getAtribuicao() != AtribuicaoMembro.GERENTE) {
			throw new RegraNegocioException("Gerente responsável deve possuir atribuição GERENTE");
		}

		return gerente;
	}

	private Set<Membro> buscarMembros(Set<Long> membrosIds) {
		var membros = membroRepository.findAllById(membrosIds).stream().collect(Collectors.toSet());

		if (membros.size() != membrosIds.size()) {
			throw new NotFoundException("Um ou mais membros informados não foram encontrados");
		}

		return membros;
	}

	private void validarMembros(Set<Membro> membros, Long projetoId) {
		if (membros.size() < 1 || membros.size() > 10) {
			throw new RegraNegocioException("Projeto deve ter entre 1 e 10 membros");
		}

		for (Membro membro : membros) {
			if (membro.getAtribuicao() != AtribuicaoMembro.FUNCIONARIO) {
				throw new RegraNegocioException("Apenas membros com atribuição FUNCIONARIO podem ser associados ao projeto");
			}

			long ativos = projetoRepository.contarProjetosAtivosDoMembro(
					membro.getId(),
					EnumSet.of(StatusProjeto.ENCERRADO, StatusProjeto.CANCELADO),
					projetoId
			);

			if (ativos >= 3) {
				throw new RegraNegocioException("Membro " + membro.getNome() + " já está alocado em 3 projetos ativos");
			}
		}
	}

	private void preencherProjeto(Projeto projeto,
								  ProjetoRequestDTO dto,
								  Membro gerente,
								  Set<Membro> membros,
								  boolean criando) {
		projeto.setNome(dto.nome());
		projeto.setDataInicio(dto.dataInicio());
		projeto.setPrevisaoTermino(dto.previsaoTermino());
		projeto.setDataRealTermino(dto.dataRealTermino());
		projeto.setOrcamentoTotal(dto.orcamentoTotal());
		projeto.setDescricao(dto.descricao());
		projeto.setGerenteResponsavel(gerente);
		projeto.setMembros(membros);

		StatusProjeto novoStatus = dto.statusAtual() != null ? dto.statusAtual() : StatusProjeto.EM_ANALISE;
		if (criando) {
			projeto.setStatusAtual(novoStatus);
			return;
		}

		StatusProjeto statusAtual = projeto.getStatusAtual();
		if (statusAtual != novoStatus && !statusAtual.podeTransicionarPara(novoStatus)) {
			throw new RegraNegocioException("Transição de status inválida: " + statusAtual + " -> " + novoStatus);
		}

		projeto.setStatusAtual(novoStatus);
	}

	private void validarDatas(ProjetoRequestDTO dto) {
		if (dto.previsaoTermino().isBefore(dto.dataInicio())) {
			throw new RegraNegocioException("Previsão de término deve ser maior ou igual à data de início");
		}

		if (dto.dataRealTermino() != null && dto.dataRealTermino().isBefore(dto.dataInicio())) {
			throw new RegraNegocioException("Data real de término deve ser maior ou igual à data de início");
		}
	}

	private ClassificacaoRisco calcularRisco(Projeto projeto) {
		long prazoMeses = calcularPrazoMeses(projeto.getDataInicio(), projeto.getPrevisaoTermino());
		BigDecimal orcamento = projeto.getOrcamentoTotal();

		boolean altoPorOrcamento = orcamento.compareTo(new BigDecimal("500000")) > 0;
		boolean altoPorPrazo = prazoMeses > 6;
		if (altoPorOrcamento || altoPorPrazo) {
			return ClassificacaoRisco.ALTO;
		}

		boolean baixoPorOrcamento = orcamento.compareTo(new BigDecimal("100000")) <= 0;
		boolean baixoPorPrazo = prazoMeses <= 3;
		if (baixoPorOrcamento && baixoPorPrazo) {
			return ClassificacaoRisco.BAIXO;
		}

		return ClassificacaoRisco.MEDIO;
	}

	private long calcularPrazoMeses(LocalDate inicio, LocalDate fim) {
		long meses = ChronoUnit.MONTHS.between(inicio, fim);
		if (inicio.plusMonths(meses).isBefore(fim)) {
			meses++;
		}
		return Math.max(meses, 0);
	}
}

