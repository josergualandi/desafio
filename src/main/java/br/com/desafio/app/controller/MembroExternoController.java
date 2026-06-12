package br.com.desafio.app.controller;

import br.com.desafio.app.dto.request.MembroRequestDTO;
import br.com.desafio.app.dto.response.MembroResponseDTO;
import br.com.desafio.app.service.MembroService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/externo/membros")
@Tag(name = "Membros Externos Mock")
@RequiredArgsConstructor
public class MembroExternoController {

    private final MembroService membroExternoService;

    @PostMapping
    public ResponseEntity<MembroResponseDTO> criar(@Valid @RequestBody MembroRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(membroExternoService.criar(dto));
    }

    @GetMapping
    public ResponseEntity<List<MembroResponseDTO>> listar() {
        return ResponseEntity.ok(membroExternoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MembroResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(membroExternoService.buscarPorId(id));
    }
}

