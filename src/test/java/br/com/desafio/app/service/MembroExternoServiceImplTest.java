package br.com.desafio.app.service;

import br.com.desafio.app.domain.AtribuicaoMembro;
import br.com.desafio.app.domain.Membro;
import br.com.desafio.app.dto.MembroRequestDTO;
import br.com.desafio.app.exception.NotFoundException;
import br.com.desafio.app.repository.MembroRepository;
import br.com.desafio.app.service.impl.MembroExternoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MembroExternoServiceImplTest {

    @Mock
    private MembroRepository membroRepository;

    private MembroExternoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MembroExternoServiceImpl(membroRepository);
    }

    @Test
    void deveCriarMembroExterno() {
        MembroRequestDTO dto = new MembroRequestDTO("Maria", AtribuicaoMembro.FUNCIONARIO);

        when(membroRepository.save(any(Membro.class))).thenAnswer(invocation -> {
            Membro membro = invocation.getArgument(0);
            membro.setId(10L);
            return membro;
        });

        var response = service.criar(dto);

        assertEquals(10L, response.id());
        assertEquals("Maria", response.nome());
        assertEquals(AtribuicaoMembro.FUNCIONARIO, response.atribuicao());
    }

    @Test
    void deveBuscarMembroPorId() {
        Membro membro = new Membro();
        membro.setId(20L);
        membro.setNome("Joao");
        membro.setAtribuicao(AtribuicaoMembro.GERENTE);

        when(membroRepository.findById(20L)).thenReturn(Optional.of(membro));

        var response = service.buscarPorId(20L);

        assertEquals("Joao", response.nome());
    }

    @Test
    void deveLancarErroQuandoMembroNaoExiste() {
        when(membroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.buscarPorId(99L));
    }

    @Test
    void deveListarMembros() {
        Membro m1 = new Membro();
        m1.setId(1L);
        m1.setNome("A");
        m1.setAtribuicao(AtribuicaoMembro.FUNCIONARIO);

        Membro m2 = new Membro();
        m2.setId(2L);
        m2.setNome("B");
        m2.setAtribuicao(AtribuicaoMembro.GERENTE);

        when(membroRepository.findAll()).thenReturn(List.of(m1, m2));

        var lista = service.listar();

        assertEquals(2, lista.size());
    }
}
