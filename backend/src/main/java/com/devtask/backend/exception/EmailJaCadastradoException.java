
package com.devtask.backend.exception;

public class EmailJaCadastradoException extends RuntimeException {

    public EmailJaCadastradoException() {
        super("Este email ja esta cadastrado.");
    }
}
