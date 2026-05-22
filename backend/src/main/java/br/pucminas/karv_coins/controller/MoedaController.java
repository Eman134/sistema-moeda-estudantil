package br.pucminas.karv_coins.controller;

import br.pucminas.karv_coins.dto.request.EnviarMoedasRequestDto;
import br.pucminas.karv_coins.dto.response.EnviarMoedasResponseDto;
import br.pucminas.karv_coins.service.MoedaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/moedas")
@Tag(name = "Moedas", description = "Operações de envio de moedas")
public class MoedaController {
    private final MoedaService moedaService;

    public MoedaController(MoedaService moedaService) {
        this.moedaService = moedaService;
    }

    @PostMapping("/envios")
    @Operation(summary = "Enviar moedas para um ou mais alunos")
    public ResponseEntity<Object> enviarMoedas(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody EnviarMoedasRequestDto request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            String erros = bindingResult.getFieldErrors().stream()
                    .map(e -> e.getField() + ": " + e.getDefaultMessage())
                    .collect(Collectors.joining("; "));
            return ResponseEntity.badRequest().body(erros);
        }

        try {
            EnviarMoedasResponseDto response = moedaService.enviarMoedas(jwt.getSubject(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
