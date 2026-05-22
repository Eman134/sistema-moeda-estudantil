package br.pucminas.karv_coins.controller;

import br.pucminas.karv_coins.dto.response.PaginaResponseDto;
import br.pucminas.karv_coins.dto.response.VantagemResponseDto;
import br.pucminas.karv_coins.service.VantagemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vantagens")
@Tag(name = "Vantagens", description = "Consulta de vantagens disponíveis para alunos")
public class AlunoVantagemController {
    private final VantagemService vantagemService;

    public AlunoVantagemController(VantagemService vantagemService) {
        this.vantagemService = vantagemService;
    }

    @GetMapping
    @Operation(summary = "Listar vantagens disponíveis para resgate")
    public ResponseEntity<PaginaResponseDto<VantagemResponseDto>> listarVantagensDisponiveis(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ) {
        return ResponseEntity.ok(vantagemService.listarVantagensDisponiveis(page, size));
    }
}
