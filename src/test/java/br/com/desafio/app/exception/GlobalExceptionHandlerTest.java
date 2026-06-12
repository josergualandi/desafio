package br.com.desafio.app.exception;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void deveTratarNotFound() {
        var response = handler.handleNotFound(new NotFoundException("nao encontrado"));

        assertEquals(404, response.getStatusCode().value());
        assertEquals("nao encontrado", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void deveTratarRegraNegocio() {
        var response = handler.handleBusiness(new RegraNegocioException("invalido"));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("invalido", response.getBody().message());
    }

    @Test
    void deveTratarErroDeValidacao() throws Exception {
        MethodParameter parameter = new MethodParameter(Dummy.class.getDeclaredMethod("setNome", String.class), 0);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Dummy(), "dummy");
        bindingResult.addError(new FieldError("dummy", "nome", "obrigatorio"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        var response = handler.handleValidation(ex);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Erro de validação", response.getBody().message());
        assertEquals("obrigatorio", response.getBody().fieldErrors().get("nome"));
    }

    @Test
    void deveTratarConstraintViolation() {
        var response = handler.handleConstraintViolation(new ConstraintViolationException("constraint", java.util.Set.of()));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("constraint", response.getBody().message());
    }

    @Test
    void deveTratarExcecaoGenerica() {
        var response = handler.handleGeneric(new RuntimeException("erro"));

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Erro interno do servidor", response.getBody().message());
    }

    static class Dummy {
        public void setNome(String nome) {
            // no-op
        }
    }
}
