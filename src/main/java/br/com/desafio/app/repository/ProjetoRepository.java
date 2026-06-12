package br.com.desafio.app.repository;

import br.com.desafio.app.domain.Projeto;
import br.com.desafio.app.domain.StatusProjeto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ProjetoRepository extends JpaRepository<Projeto, Long>, JpaSpecificationExecutor<Projeto> {

		@Query("""
						select count(p)
							from Projeto p
							join p.membros m
						 where m.id = :membroId
							 and p.statusAtual not in :statusFinalizados
							 and (:projetoId is null or p.id <> :projetoId)
						""")
		long contarProjetosAtivosDoMembro(@Param("membroId") Long membroId,
																			@Param("statusFinalizados") Collection<StatusProjeto> statusFinalizados,
																			@Param("projetoId") Long projetoId);

		List<Projeto> findAllByStatusAtualAndDataRealTerminoIsNotNull(StatusProjeto statusAtual);

		@Query("select count(distinct m.id) from Projeto p join p.membros m")
		long contarMembrosUnicosAlocados();
}
