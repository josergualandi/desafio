package br.com.desafio.app.repository;

import br.com.desafio.app.domain.Membro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembroRepository extends JpaRepository<Membro, Long> {
}
