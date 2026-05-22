package br.pucminas.karv_coins.dto.response;

import br.pucminas.karv_coins.model.Professor;

public record ProfessorResumoResponseDto(
        Long id,
        String nome,
        String email,
        String departamento,
        Double saldo
) {
    public static ProfessorResumoResponseDto from(Professor professor) {
        return new ProfessorResumoResponseDto(
                professor.getId(),
                professor.getNome(),
                professor.getEmail(),
                professor.getDepartamento(),
                professor.getSaldo()
        );
    }
}
