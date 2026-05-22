package br.pucminas.karv_coins.dto.response;

import br.pucminas.karv_coins.model.Aluno;

public record AlunoResumoResponseDto(
        Long id,
        String nome,
        String email,
        String curso,
        String instituicao,
        Double saldo
) {
    public static AlunoResumoResponseDto from(Aluno aluno) {
        return new AlunoResumoResponseDto(
                aluno.getId(),
                aluno.getNome(),
                aluno.getEmail(),
                aluno.getCurso(),
                aluno.getInstituicao(),
                aluno.getSaldo()
        );
    }
}
