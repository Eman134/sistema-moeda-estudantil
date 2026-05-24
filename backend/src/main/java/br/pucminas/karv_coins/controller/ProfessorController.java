package br.pucminas.karv_coins.controller;

import br.pucminas.karv_coins.dto.response.ExtratoResponseDto;
import br.pucminas.karv_coins.dto.response.ProfessorResumoResponseDto;
import br.pucminas.karv_coins.service.ExtratoService;
import br.pucminas.karv_coins.service.ProfessorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/professores")
@Tag(name = "Professores", description = "Consulta de dados do professor autenticado")
public class ProfessorController {
    private final ProfessorService professorService;
    private final ExtratoService extratoService;

    public ProfessorController(ProfessorService professorService, ExtratoService extratoService) {
        this.professorService = professorService;
        this.extratoService = extratoService;
    }

    @GetMapping("/me/resumo")
    @Operation(summary = "Buscar resumo do professor autenticado")
    public ResponseEntity<Object> buscarResumo(@AuthenticationPrincipal Jwt jwt) {
        try {
            ProfessorResumoResponseDto response = professorService.buscarResumo(jwt.getSubject());
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/me/extrato")
    @Operation(summary = "Consultar extrato do professor autenticado")
    public ResponseEntity<Object> consultarMeuExtrato(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            ExtratoResponseDto response = extratoService.consultarExtratoProfessor(jwt.getSubject(), page, size);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
