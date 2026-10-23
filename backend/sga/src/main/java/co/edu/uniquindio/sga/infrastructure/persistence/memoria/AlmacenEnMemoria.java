package co.edu.uniquindio.sga.infrastructure.persistence.memoria;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Base de los repositorios en memoria: guarda los agregados en un mapa, con su
 * identificador como clave y en el orden en que se guardaron.
 *
 * Primera implementación de la capa de infraestructura. Permite que los
 * servicios de aplicación y los controladores funcionen y se prueben desde ya.
 * Los datos se pierden al apagar la aplicación. Cuando se haga la versión con
 * JPA y H2, se escribe otra clase que implemente la misma interfaz del
 * dominio (por ejemplo {@code ReservaRepositoryJpa implements ReservaRepository});
 * el dominio y los servicios no cambian.
 *
 * Ojo: el mapa guarda la misma instancia que se le entrega. Igual conviene
 * llamar siempre a {@code guardar} después de modificar un agregado, porque
 * con JPA sí será obligatorio.
 *
 * @param <K> tipo del identificador (un value object como CodigoReserva)
 * @param <T> tipo del agregado
 */
abstract class AlmacenEnMemoria<K, T> {

    private final Map<K, T> datos = new LinkedHashMap<>();
    private final Function<T, K> identificador;

    protected AlmacenEnMemoria(Function<T, K> identificador) {
        this.identificador = identificador;
    }

    protected synchronized void almacenar(T agregado) {
        if (agregado == null) {
            throw new IllegalArgumentException("No se puede guardar un valor nulo");
        }
        datos.put(identificador.apply(agregado), agregado);
    }

    protected synchronized Optional<T> porId(K id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(datos.get(id));
    }

    protected synchronized List<T> filtrar(Predicate<T> condicion) {
        return datos.values().stream().filter(condicion).toList();
    }

    protected synchronized List<T> todos() {
        return List.copyOf(new ArrayList<>(datos.values()));
    }

    /** Vacía el almacén; útil en pruebas. */
    public synchronized void limpiar() {
        datos.clear();
    }
}
