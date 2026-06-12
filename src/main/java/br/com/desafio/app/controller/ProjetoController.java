package br.com.desafio.app.controller;

import br.com.desafio.app.domain.StatusProjeto;
import br.com.desafio.app.dto.PortfolioResumoDTO;
import br.com.desafio.app.dto.ProjetoRequestDTO;
import br.com.desafio.app.dto.ProjetoResponseDTO;
import br.com.desafio.app.service.ProjetoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projetos")
@Tag(name = "Projetos")
@RequiredArgsConstructor
public class ProjetoController {

	private final ProjetoService projetoService;

	@PostMapping
	public ResponseEntity<ProjetoResponseDTO> criar(@Valid @RequestBody ProjetoRequestDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(projetoService.criar(dto));
	}

	@GetMapping
	public ResponseEntity<Page<ProjetoResponseDTO>> listar(
			@RequestParam(required = false) String nome,
			@RequestParam(required = false) StatusProjeto status,
			Pageable pageable
	) {
		return ResponseEntity.ok(projetoService.listar(nome, status, pageable));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ProjetoResponseDTO> buscarPorId(@PathVariable Long id) {
		return ResponseEntity.ok(projetoService.buscarPorId(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ProjetoResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody ProjetoRequestDTO dto) {
		return ResponseEntity.ok(projetoService.atualizar(id, dto));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluir(@PathVariable Long id) {
		projetoService.excluir(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/relatorio/resumo")
	public ResponseEntity<PortfolioResumoDTO> resumoPortfolio() {
		return ResponseEntity.ok(projetoService.gerarResumoPortfolio());
	}
}
