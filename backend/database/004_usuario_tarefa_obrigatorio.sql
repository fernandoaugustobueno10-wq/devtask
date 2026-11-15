
BEGIN;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM public.tarefa
        WHERE usuario_id IS NULL
    ) THEN
        RAISE EXCEPTION
            'Existem tarefas sem proprietario. Corrija os registros antes de continuar.';
    END IF;
END
$$;

ALTER TABLE public.tarefa
ALTER COLUMN usuario_id SET NOT NULL;

COMMIT;
