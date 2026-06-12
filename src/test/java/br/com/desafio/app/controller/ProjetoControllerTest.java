package br.com.desafio.app.controller;

import br.com.desafio.app.domain.ClassificacaoRisco;
import br.com.desafio.app.domain.StatusProjeto;
import br.com.desafio.app.domain.AtribuicaoMembro;
import br.com.desafio.app.dto.MembroResumoDTO;
import br.com.desafio.app.dto.PortfolioResumoDTO;
import br.com.desafio.app.dto.ProjetoRequestDTO;
import br.com.desafio.app.dto.ProjetoResponseDTO;
import br.com.desafio.app.service.ProjetoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjetoControllerTest {

    @Mock
    private ProjetoService projetoService;

    private ProjetoController controller;

    @BeforeEach
    void setUp() {
        controller = new ProjetoController(projetoService);
    }

    @Test
    void deveCriarProjeto() {
        ProjetoRequestDTO request = requestDto();
        ProjetoResponseDTO response = responseDto(10L);

        when(projetoService.criar(request)).thenReturn(response);

        var result = controller.criar(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(10L, result.getBody().id());
    }

    @Test
    void deveListarProjetos() {
        var pageable = PageRequest.of(0, 10);
        var page = new PageImpl<>(List.of(responseDto(1L), responseDto(2L)), pageable, 2);

        when(projetoService.listar(eq("Projeto"), eq(StatusProjeto.EM_ANALISE), eq(pageable))).thenReturn(page);

        var result = controller.listar("Projeto", StatusProjeto.EM_ANALISE, pageable);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(2, result.getBody().getTotalElements());
    }

    @Test
    void deveBuscarPorId() {
        when(projetoService.buscarPorId(1L)).thenReturn(responseDto(1L));

        var result = controller.buscarPorId(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1L, result.getBody().id());
    }

    @Test
    void deveAtualizarProjeto() {
        ProjetoRequestDTO request = requestDto();
        ProjetoResponseDTO updated = responseDto(99L);

        when(projetoService.atualizar(99L, request)).thenReturn(updated);

        var result = controller.atualizar(99L, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(99L, result.getBody().id());
    }

    @Test
    void deveExcluirProjeto() {
        doNothing().when(projetoService).excluir(15L);

        var result = controller.excluir(15L);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(projetoService).excluir(15L);
    }

    @Test
    void deveRetornarResumoPortfolio() {
        PortfolioResumoDTO resumo = new PortfolioResumoDTO(List.of(), 0.0, 3L);
        when(projetoService.gerarResumoPortfolio()).thenReturn(resumo);

        var result = controller.resumoPortfolio();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(3L, result.getBody().totalMembrosUnicosAlocados());
    }

    private ProjetoRequestDTO requestDto() {
        return new ProjetoRequestDTO(
                "Projeto Teste",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 1),
                null,
                new BigDecimal("1000"),
                "Descricao",
                1L,
                StatusProjeto.EM_ANALISE,
                Set.of(2L)
        );
    }

    private ProjetoResponseDTO responseDto(Long id) {
        return new ProjetoResponseDTO(
                id,
                "Projeto Teste",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 1),
                null,
                new BigDecimal("1000"),
                "Descricao",
                new MembroResumoDTO(1L, "Gerente", AtribuicaoMembro.GERENTE),
                StatusProjeto.EM_ANALISE,
                ClassificacaoRisco.BAIXO,
                List.of(new MembroResumoDTO(2L, "Dev", AtribuicaoMembro.FUNCIONARIO))
        );
    }
}
