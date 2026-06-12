package br.com.desafio.app.service;

import br.com.desafio.app.dto.request.MembroRequestDTO;
import br.com.desafio.app.dto.response.MembroResponseDTO;

import java.util.List;

public interface MembroService {

    MembroResponseDTO criar(MembroRequestDTO dto);

    MembroResponseDTO buscarPorId(Long id);

    List<MembroResponseDTO> listar();
}

