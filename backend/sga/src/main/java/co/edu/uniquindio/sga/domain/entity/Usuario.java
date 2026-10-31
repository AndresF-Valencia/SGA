package co.edu.uniquindio.sga.domain.entity;

import co.edu.uniquindio.sga.domain.exception.ReglaDominioException;
import co.edu.uniquindio.sga.domain.port.CifradorContrasena;
import co.edu.uniquindio.sga.domain.valueobject.Correo;
import co.edu.uniquindio.sga.domain.valueobject.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.valueobject.Rol;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Cuenta de acceso al sistema (3.6 y 7.2). Una persona puede ser titular de una
 * reserva sin tener usuario; el usuario es solo la forma de iniciar sesión.
 *
 * La contraseña nunca se guarda en texto plano: el dominio valida que sea
 * segura y la entrega al CifradorContrasena antes de guardarla.
 */
public class Usuario {

    private final Correo correo;              // identidad: único y no editable
    private final DocumentoIdentidad documento;
    private final LocalDate fechaNacimiento;
    private String nombre;
    private String telefono;
    private String contrasenaCifrada;
    private Rol rol;
    private boolean activo;                   // eliminación lógica
    private String fotoPerfil;                // URL de la foto (opcional)

    // 7.2 Recuperación: código de un solo uso, guardado cifrado, válido 15 minutos
    public static final int MINUTOS_VIGENCIA_CODIGO = 15;
    public static final int MAX_INTENTOS_CODIGO = 5;
    private String codigoRecuperacionCifrado;
    private LocalDateTime codigoRecuperacionExpira;
    private int intentosCodigoFallidos;

    private Usuario(Correo correo, String nombre, String contrasenaCifrada, String telefono,
            DocumentoIdentidad documento, LocalDate fechaNacimiento, Rol rol, boolean activo) {
        this.correo = correo;
        this.nombre = nombre;
        this.contrasenaCifrada = contrasenaCifrada;
        this.telefono = telefono;
        this.documento = documento;
        this.fechaNacimiento = fechaNacimiento;
        this.rol = rol;
        this.activo = activo;
    }

    /** 7.2: el registro público siempre crea un HUÉSPED. */
    public static Usuario registrarHuesped(Correo correo, String nombre, String contrasenaPlana,
            String telefono, DocumentoIdentidad documento, LocalDate fechaNacimiento,
            CifradorContrasena cifrador, LocalDate hoy) {
        return registrar(correo, nombre, contrasenaPlana, telefono, documento, fechaNacimiento,
                Rol.HUESPED, cifrador, hoy);
    }

    /** Usuario con un rol cualquiera. Solo lo usa el administrador (la seguridad lo controla). */
    public static Usuario registrar(Correo correo, String nombre, String contrasenaPlana,
            String telefono, DocumentoIdentidad documento, LocalDate fechaNacimiento, Rol rol,
            CifradorContrasena cifrador, LocalDate hoy) {
        if (correo == null) {
            throw new ReglaDominioException("El usuario debe tener correo");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException("El usuario debe tener nombre");
        }
        if (telefono == null || telefono.isBlank()) {
            throw new ReglaDominioException("El usuario debe tener teléfono");
        }
        if (documento == null) {
            throw new ReglaDominioException("El usuario debe tener documento de identidad");
        }
        if (fechaNacimiento == null) {
            throw new ReglaDominioException("El usuario debe tener fecha de nacimiento");
        }
        if (fechaNacimiento.isAfter(hoy)) {
            throw new ReglaDominioException("La fecha de nacimiento no puede ser futura");
        }
        if (rol == null) {
            throw new ReglaDominioException("El usuario debe tener un rol");
        }
        validarContrasenaSegura(contrasenaPlana);
        return new Usuario(correo, nombre.trim(), cifrador.cifrar(contrasenaPlana), telefono.trim(),
                documento, fechaNacimiento, rol, true);
    }

    /** Vuelve a armar un usuario guardado (la usa la persistencia). La contraseña ya viene cifrada. */
    public static Usuario reconstruir(Correo correo, String nombre, String contrasenaCifrada, String telefono,
            DocumentoIdentidad documento, LocalDate fechaNacimiento, Rol rol, boolean activo) {
        return new Usuario(correo, nombre, contrasenaCifrada, telefono, documento, fechaNacimiento, rol, activo);
    }

    /** Igual que el anterior, con la foto y el código de recuperación pendiente (si lo hay). */
    public static Usuario reconstruir(Correo correo, String nombre, String contrasenaCifrada, String telefono,
            DocumentoIdentidad documento, LocalDate fechaNacimiento, Rol rol, boolean activo,
            String fotoPerfil, String codigoRecuperacionCifrado, LocalDateTime codigoRecuperacionExpira,
            int intentosCodigoFallidos) {
        Usuario usuario = reconstruir(correo, nombre, contrasenaCifrada, telefono, documento, fechaNacimiento, rol, activo);
        usuario.fotoPerfil = fotoPerfil;
        usuario.codigoRecuperacionCifrado = codigoRecuperacionCifrado;
        usuario.codigoRecuperacionExpira = codigoRecuperacionExpira;
        usuario.intentosCodigoFallidos = intentosCodigoFallidos;
        return usuario;
    }

    /** 7.2: mínimo 8 caracteres, con al menos una mayúscula y un número. */
    private static void validarContrasenaSegura(String contrasena) {
        if (contrasena == null || contrasena.length() < 8) {
            throw new ReglaDominioException("La contraseña debe tener al menos 8 caracteres");
        }
        if (contrasena.chars().noneMatch(Character::isUpperCase)) {
            throw new ReglaDominioException("La contraseña debe tener al menos una mayúscula");
        }
        if (contrasena.chars().noneMatch(Character::isDigit)) {
            throw new ReglaDominioException("La contraseña debe tener al menos un número");
        }
    }

    // ---------- Comportamiento ----------

    /** Solo un usuario activo con la contraseña correcta puede iniciar sesión. */
    public boolean puedeIniciarSesion(String contrasenaPlana, CifradorContrasena cifrador) {
        return activo && contrasenaPlana != null && cifrador.coincide(contrasenaPlana, contrasenaCifrada);
    }

    /** Eliminación lógica (12.4): el usuario queda inactivo, sus reservas no se tocan. */
    public void desactivar() {
        if (!activo) {
            throw new ReglaDominioException("El usuario ya está eliminado");
        }
        this.activo = false;
        limpiarCodigo();
    }

    // ---------- 7.2 Perfil y contraseña ----------

    /** Actualiza nombre, teléfono y foto. El correo no es editable (es la identidad). */
    public void actualizarPerfil(String nombre, String telefono, String fotoPerfil) {
        verificarActivo();
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException("El usuario debe tener nombre");
        }
        if (telefono == null || telefono.isBlank()) {
            throw new ReglaDominioException("El usuario debe tener teléfono");
        }
        if (fotoPerfil != null && !fotoPerfil.isBlank()
                && !(fotoPerfil.startsWith("http://") || fotoPerfil.startsWith("https://"))) {
            throw new ReglaDominioException("La foto de perfil debe ser una URL (http o https)");
        }
        this.nombre = nombre.trim();
        this.telefono = telefono.trim();
        this.fotoPerfil = fotoPerfil == null || fotoPerfil.isBlank() ? null : fotoPerfil.trim();
    }

    /** Cambio voluntario: exige la contraseña actual y una nueva segura y distinta. */
    public void cambiarContrasena(String actual, String nueva, CifradorContrasena cifrador) {
        verificarActivo();
        if (actual == null || !cifrador.coincide(actual, contrasenaCifrada)) {
            throw new ReglaDominioException("La contraseña actual no es correcta");
        }
        if (nueva != null && nueva.equals(actual)) {
            throw new ReglaDominioException("La nueva contraseña debe ser distinta de la actual");
        }
        validarContrasenaSegura(nueva);
        this.contrasenaCifrada = cifrador.cifrar(nueva);
        limpiarCodigo();
    }

    /**
     * Recuperación, paso 1: guarda el código (cifrado, como la contraseña) con vigencia de
     * 15 minutos. Pedir otro reemplaza al anterior.
     */
    public void asignarCodigoRecuperacion(String codigoPlano, CifradorContrasena cifrador, LocalDateTime ahora) {
        verificarActivo();
        if (codigoPlano == null || codigoPlano.isBlank()) {
            throw new ReglaDominioException("El código de recuperación es obligatorio");
        }
        this.codigoRecuperacionCifrado = cifrador.cifrar(codigoPlano);
        this.codigoRecuperacionExpira = ahora.plusMinutes(MINUTOS_VIGENCIA_CODIGO);
        this.intentosCodigoFallidos = 0;
    }

    /**
     * Recuperación, paso 2: con el código vigente se pone una contraseña nueva.
     * El código es de un solo uso; tras 5 intentos fallidos se anula.
     * Devuelve false si el código no sirve (el caso de uso decide qué responder).
     */
    public boolean restablecerContrasena(String codigoPlano, String nueva, CifradorContrasena cifrador,
            LocalDateTime ahora) {
        if (!activo || codigoRecuperacionCifrado == null || ahora.isAfter(codigoRecuperacionExpira)) {
            limpiarCodigo();
            return false;
        }
        if (codigoPlano == null || !cifrador.coincide(codigoPlano, codigoRecuperacionCifrado)) {
            intentosCodigoFallidos++;
            if (intentosCodigoFallidos >= MAX_INTENTOS_CODIGO) {
                limpiarCodigo();
            }
            return false;
        }
        validarContrasenaSegura(nueva);
        this.contrasenaCifrada = cifrador.cifrar(nueva);
        limpiarCodigo();   // un solo uso
        return true;
    }

    // ---------- 7.2 Roles (solo administrador) ----------

    public void cambiarRol(Rol nuevoRol) {
        verificarActivo();
        if (nuevoRol == null) {
            throw new ReglaDominioException("Debe indicarse el nuevo rol");
        }
        this.rol = nuevoRol;
    }

    private void verificarActivo() {
        if (!activo) {
            throw new ReglaDominioException("El usuario está eliminado");
        }
    }

    private void limpiarCodigo() {
        this.codigoRecuperacionCifrado = null;
        this.codigoRecuperacionExpira = null;
        this.intentosCodigoFallidos = 0;
    }

    public boolean esHuesped() {
        return rol == Rol.HUESPED;
    }

    // ---------- Consultas ----------

    public Correo getCorreo() {
        return correo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public DocumentoIdentidad getDocumento() {
        return documento;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public String getContrasenaCifrada() {
        return contrasenaCifrada;
    }

    public Rol getRol() {
        return rol;
    }

    public boolean estaActivo() {
        return activo;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public String getCodigoRecuperacionCifrado() {
        return codigoRecuperacionCifrado;
    }

    public LocalDateTime getCodigoRecuperacionExpira() {
        return codigoRecuperacionExpira;
    }

    public int getIntentosCodigoFallidos() {
        return intentosCodigoFallidos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Usuario otro)) {
            return false;
        }
        return correo.equals(otro.correo);
    }

    @Override
    public int hashCode() {
        return correo.hashCode();
    }
}
