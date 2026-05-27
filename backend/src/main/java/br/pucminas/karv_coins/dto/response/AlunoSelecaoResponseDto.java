package br.pucminas.karv_coins.dto.response;

import br.pucminas.karv_coins.model.Aluno;

public record AlunoSelecaoResponseDto(
        Long id,
        String nome,
        String email,
        String curso,
        String instituicao
) {
    public static AlunoSelecaoResponseDto from(Aluno aluno) {
        return new AlunoSelecaoResponseDto(
                aluno.getId(),
                aluno.getNome(),
                aluno.getEmail(),
                aluno.getCurso(),
                aluno.getInstituicao()
        );
    }
}
