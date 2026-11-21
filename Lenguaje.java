package chomsky.modelo;

import java.util.List;

/**
 * Un lenguaje de ejemplo: su nombre, una descripción formal, la máquina que lo
 * reconoce y las cadenas de prueba. El tipo M es AFD, AP o MaquinaTuring.
 */
public record Lenguaje<M>(String nombre, String descripcion, M maquina, List<Ejemplo> ejemplos) {

    /** JComboBox usa toString() para mostrar el elemento. */
    @Override
    public String toString() {
        return nombre;
    }
}
