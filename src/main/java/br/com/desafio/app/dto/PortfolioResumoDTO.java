package br.com.desafio.app.dto;

import java.util.List;

public record PortfolioResumoDTO(
        List<StatusResumoDTO> resumoPorStatus,
        double mediaDuracaoProjetosEncerradosDias,
        long totalMembrosUnicosAlocados
) {
}
