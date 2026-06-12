package br.com.desafio.app.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projetos")
@Getter
@Setter
@NoArgsConstructor
public class Projeto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 160)
	private String nome;

	@Column(nullable = false)
	private LocalDate dataInicio;

	@Column(nullable = false)
	private LocalDate previsaoTermino;

	@Column
	private LocalDate dataRealTermino;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal orcamentoTotal;

	@Column(nullable = false, length = 1000)
	private String descricao;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "gerente_id", nullable = false)
	private Membro gerenteResponsavel;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private StatusProjeto statusAtual;

	@ManyToMany
	@JoinTable(
			name = "projeto_membros",
			joinColumns = @JoinColumn(name = "projeto_id"),
			inverseJoinColumns = @JoinColumn(name = "membro_id")
	)
	private Set<Membro> membros = new HashSet<>();
}
