package br.com.desafio.app.service.impl;

import br.com.desafio.app.domain.Membro;
import br.com.desafio.app.dto.MembroRequestDTO;
import br.com.desafio.app.dto.MembroResponseDTO;
import br.com.desafio.app.exception.NotFoundException;
import br.com.desafio.app.repository.MembroRepository;
import br.com.desafio.app.service.MembroExternoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MembroExternoServiceImpl implements MembroExternoService {

    private final MembroRepository membroRepository;

    @Override
    public MembroResponseDTO criar(MembroRequestDTO dto) {
        Membro membro = new Membro();
        membro.setNome(dto.nome());
        membro.setAtribuicao(dto.atribuicao());
        Membro salvo = membroRepository.save(membro);
        return new MembroResponseDTO(salvo.getId(), salvo.getNome(), salvo.getAtribuicao());
    }

    @Override
    public MembroResponseDTO buscarPorId(Long id) {
        Membro membro = membroRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Membro não encontrado"));
        return new MembroResponseDTO(membro.getId(), membro.getNome(), membro.getAtribuicao());
    }

    @Override
    public List<MembroResponseDTO> listar() {
        return membroRepository.findAll().stream()
                .map(membro -> new MembroResponseDTO(membro.getId(), membro.getNome(), membro.getAtribuicao()))
                .toList();
    }
}
