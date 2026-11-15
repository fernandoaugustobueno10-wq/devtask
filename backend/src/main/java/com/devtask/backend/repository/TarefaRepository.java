
package com.devtask.backend.repository;

import com.devtask.backend.model.Tarefa;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface TarefaRepository extends
        JpaRepository<Tarefa, Long>,
        JpaSpecificationExecutor<Tarefa> {

    List<Tarefa> findAllByUsuarioId(
            Long usuarioId,
            Sort sort
    );

    Optional<Tarefa> findByIdAndUsuarioId(
            Long id,
            Long usuarioId
    );
}
