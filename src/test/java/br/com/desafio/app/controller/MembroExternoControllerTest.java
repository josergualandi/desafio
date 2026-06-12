package br.com.desafio.app.controller;

import br.com.desafio.app.domain.AtribuicaoMembro;
import br.com.desafio.app.dto.request.MembroRequestDTO;
import br.com.desafio.app.dto.response.MembroResponseDTO;
import br.com.desafio.app.service.MembroService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MembroExternoControllerTest {

    @Mock
    private MembroService membroExternoService;

    private MembroExternoController controller;

    @BeforeEach
    void setUp() {
        controller = new MembroExternoController(membroExternoService);
    }

    @Test
    void deveCriarMembroExterno() {
        MembroRequestDTO request = new MembroRequestDTO("Fulano", AtribuicaoMembro.FUNCIONARIO);
        MembroResponseDTO response = new MembroResponseDTO(1L, "Fulano", AtribuicaoMembro.FUNCIONARIO);

        when(membroExternoService.criar(request)).thenReturn(response);

        var result = controller.criar(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(1L, result.getBody().id());
    }

    @Test
    void deveListarMembrosExternos() {
        when(membroExternoService.listar()).thenReturn(List.of(
                new MembroResponseDTO(1L, "A", AtribuicaoMembro.FUNCIONARIO),
                new MembroResponseDTO(2L, "B", AtribuicaoMembro.FUNCIONARIO)
        ));

        var result = controller.listar();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(2, result.getBody().size());
    }

    @Test
    void deveBuscarMembroExternoPorId() {
        when(membroExternoService.buscarPorId(10L)).thenReturn(
                new MembroResponseDTO(10L, "Nome", AtribuicaoMembro.FUNCIONARIO)
        );

        var result = controller.buscarPorId(10L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(10L, result.getBody().id());
    }
}

