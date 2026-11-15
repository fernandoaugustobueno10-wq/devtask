
package com.devtask.backend.repository;

import com.devtask.backend.model.Tarefa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TarefaRepository extends
        JpaRepository<Tarefa, Long>,
        JpaSpecificationExecutor<Tarefa> {
}
