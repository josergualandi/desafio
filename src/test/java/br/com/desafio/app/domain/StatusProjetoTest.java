package br.com.desafio.app.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatusProjetoTest {

    @Test
    void devePermitirProximaEtapaLinear() {
        assertTrue(StatusProjeto.EM_ANALISE.podeTransicionarPara(StatusProjeto.ANALISE_REALIZADA));
    }

    @Test
    void naoDevePermitirPularEtapa() {
        assertFalse(StatusProjeto.EM_ANALISE.podeTransicionarPara(StatusProjeto.INICIADO));
    }

    @Test
    void devePermitirCancelamentoDeQualquerEtapaNaoFinal() {
        assertTrue(StatusProjeto.PLANEJADO.podeTransicionarPara(StatusProjeto.CANCELADO));
    }

    @Test
    void naoDevePermitirSairDeCanceladoOuEncerrado() {
        assertFalse(StatusProjeto.CANCELADO.podeTransicionarPara(StatusProjeto.EM_ANALISE));
        assertTrue(StatusProjeto.ENCERRADO.podeTransicionarPara(StatusProjeto.CANCELADO));
    }
}
