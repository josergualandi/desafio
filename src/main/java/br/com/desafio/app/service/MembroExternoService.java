package br.com.desafio.app.service;

import br.com.desafio.app.dto.MembroRequestDTO;
import br.com.desafio.app.dto.MembroResponseDTO;

import java.util.List;

public interface MembroExternoService {

    MembroResponseDTO criar(MembroRequestDTO dto);

    MembroResponseDTO buscarPorId(Long id);

    List<MembroResponseDTO> listar();
}
