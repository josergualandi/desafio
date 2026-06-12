package br.com.desafio.app.domain;

import java.util.List;

public enum StatusProjeto {
    EM_ANALISE,
    ANALISE_REALIZADA,
    ANALISE_APROVADA,
    INICIADO,
    PLANEJADO,
    EM_ANDAMENTO,
    ENCERRADO,
    CANCELADO;

    private static final List<StatusProjeto> FLUXO = List.of(
            EM_ANALISE,
            ANALISE_REALIZADA,
            ANALISE_APROVADA,
            INICIADO,
            PLANEJADO,
            EM_ANDAMENTO,
            ENCERRADO
    );

    public boolean podeTransicionarPara(StatusProjeto proximo) {
        if (proximo == CANCELADO) {
            return this != CANCELADO;
        }
        if (this == CANCELADO || this == ENCERRADO) {
            return false;
        }

        int indiceAtual = FLUXO.indexOf(this);
        int indiceProximo = FLUXO.indexOf(proximo);
        return indiceAtual >= 0 && indiceProximo == indiceAtual + 1;
    }
}
