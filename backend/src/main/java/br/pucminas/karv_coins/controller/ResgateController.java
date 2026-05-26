package br.pucminas.karv_coins.controller;

import br.pucminas.karv_coins.dto.response.ResgateVerificacaoResponseDto;
import br.pucminas.karv_coins.service.ResgateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/resgates")
@Tag(name = "Resgates", description = "Verificação de resgates de vantagens")
public class ResgateController {
    private final ResgateService resgateService;

    public ResgateController(ResgateService resgateService) {
        this.resgateService = resgateService;
    }

    @GetMapping("/{codigoCupom}/verificacao")
    @Operation(summary = "Verificar resgate por código de cupom")
    public ResponseEntity<Object> verificar(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String codigoCupom
    ) {
        try {
            ResgateVerificacaoResponseDto response = resgateService.verificar(jwt.getSubject(), codigoCupom);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
