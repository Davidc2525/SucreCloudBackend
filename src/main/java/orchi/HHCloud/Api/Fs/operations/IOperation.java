package orchi.HHCloud.Api.Fs.operations;

import orchi.HHCloud.store.response.Response;

/**
 * Interface para operaciones de entrada y salida
 * @author Colmenares David
 *
 * */
public interface IOperation {
    /**
     * Ejecuta la operacion
     * */
    public Response run();
}
