package br.pucminas.karv_coins.controller;

import br.pucminas.karv_coins.dto.response.PaginaResponseDto;
import br.pucminas.karv_coins.dto.response.ResgateResponseDto;
import br.pucminas.karv_coins.dto.response.VantagemResponseDto;
import br.pucminas.karv_coins.service.ResgateService;
import br.pucminas.karv_coins.service.VantagemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vantagens")
@Tag(name = "Vantagens", description = "Consulta de vantagens disponíveis para alunos")
public class AlunoVantagemController {
    private final VantagemService vantagemService;
    private final ResgateService resgateService;

    public AlunoVantagemController(VantagemService vantagemService, ResgateService resgateService) {
        this.vantagemService = vantagemService;
        this.resgateService = resgateService;
    }

    @GetMapping
    @Operation(summary = "Listar vantagens disponíveis para resgate")
    public ResponseEntity<PaginaResponseDto<VantagemResponseDto>> listarVantagensDisponiveis(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ) {
        return ResponseEntity.ok(vantagemService.listarVantagensDisponiveis(page, size));
    }

    @PostMapping("/{vantagemId}/resgates")
    @Operation(summary = "Resgatar vantagem disponível")
    public ResponseEntity<Object> resgatar(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long vantagemId
    ) {
        try {
            ResgateResponseDto response = resgateService.resgatar(jwt.getSubject(), vantagemId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
