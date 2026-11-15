
BEGIN;

ALTER TABLE public.tarefa
ADD COLUMN IF NOT EXISTS usuario_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_tarefa_usuario'
          AND conrelid = 'public.tarefa'::regclass
    ) THEN
        ALTER TABLE public.tarefa
        ADD CONSTRAINT fk_tarefa_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES public.usuario(id)
        ON DELETE RESTRICT;
    END IF;
END
$$;

CREATE INDEX IF NOT EXISTS idx_tarefa_usuario_id
ON public.tarefa (usuario_id, id);

COMMIT;
