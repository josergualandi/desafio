package br.com.desafio.app.service;

import br.com.desafio.app.domain.StatusProjeto;
import br.com.desafio.app.dto.PortfolioResumoDTO;
import br.com.desafio.app.dto.ProjetoRequestDTO;
import br.com.desafio.app.dto.ProjetoResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjetoService {

	ProjetoResponseDTO criar(ProjetoRequestDTO dto);

	ProjetoResponseDTO buscarPorId(Long id);

	Page<ProjetoResponseDTO> listar(String nome, StatusProjeto status, Pageable pageable);

	ProjetoResponseDTO atualizar(Long id, ProjetoRequestDTO dto);

	void excluir(Long id);

	PortfolioResumoDTO gerarResumoPortfolio();
}
