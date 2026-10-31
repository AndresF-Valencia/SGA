package co.edu.uniquindio.sga.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Qué rol puede llamar a qué endpoint (7.1 y 7.2).
 *
 * Las reglas se revisan EN ORDEN y gana la primera que coincide, por eso las
 * más específicas van primero. Lo que no esté en la lista exige sesión iniciada.
 *
 * Ojo: el rol no basta para el huésped. Que solo vea SUS reservas se verifica
 * en el controller con VerificarAccesoReservaUseCase (propietario del recurso).
 */
@Configuration
public class SecurityConfig {

    private static final String HUESPED = "HUESPED";
    private static final String RECEPCIONISTA = "RECEPCIONISTA";
    private static final String ADMINISTRADOR = "ADMINISTRADOR";
    private static final String PERSONAL_SERVICIO = "PERSONAL_SERVICIO";

    @Bean
    public SecurityFilterChain cadenaDeSeguridad(HttpSecurity http, JwtServicio jwtServicio) throws Exception {
        ManejadorErroresSeguridad errores = new ManejadorErroresSeguridad();

        http
                // API sin sesión de servidor: cada petición trae su token
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // La consola de H2 se muestra dentro de un iframe
                .headers(encabezados -> encabezados.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .exceptionHandling(e -> e.authenticationEntryPoint(errores).accessDeniedHandler(errores))
                .authorizeHttpRequests(reglas -> reglas

                        // ---------- Públicas ----------
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/h2-console/**", "/swagger-ui/**", "/swagger-ui.html",
                                "/v3/api-docs/**", "/api-docs/**", "/actuator/health", "/error").permitAll()
                        // Los canales externos se autentican con su credencial (X-Canal-Credencial), no con JWT
                        .requestMatchers("/api/canales/**").permitAll()
                        // El tablero de estados es del personal: va antes de la regla pública de abajo,
                        // porque "/api/apartamentos/*" también coincidiría con "/api/apartamentos/estado"
                        .requestMatchers(HttpMethod.GET, "/api/apartamentos/estado")
                                .hasAnyRole(RECEPCIONISTA, ADMINISTRADOR, PERSONAL_SERVICIO)
                        // Buscar apartamentos, ver su detalle y cotizar no exige cuenta (7.1, Huésped).
                        // Solo el listado y el detalle: novedades y bloqueos NO son públicos.
                        .requestMatchers(HttpMethod.GET, "/api/apartamentos", "/api/apartamentos/*",
                                "/api/disponibilidad").permitAll()
                        // La consulta del alojamiento (dirección, normas, servicios) es pública; modificarlo (PUT /parametros) es solo del administrador
                        .requestMatchers(HttpMethod.GET, "/api/alojamiento").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/cotizaciones").permitAll()

                        // ---------- Reservas ----------
                        // El huésped crea, cancela y consulta las suyas; recepción y administración, todas
                        .requestMatchers(HttpMethod.POST, "/api/reservas")
                                .hasAnyRole(HUESPED, RECEPCIONISTA, ADMINISTRADOR)
                        .requestMatchers(HttpMethod.PUT, "/api/reservas/*/cancelar")
                                .hasAnyRole(HUESPED, RECEPCIONISTA, ADMINISTRADOR)
                        // Listado general solo para el personal; el huésped usa "mis reservas"
                        .requestMatchers(HttpMethod.GET, "/api/reservas")
                                .hasAnyRole(RECEPCIONISTA, ADMINISTRADOR)
                        .requestMatchers(HttpMethod.GET, "/api/reservas/mis-reservas").hasRole(HUESPED)
                        .requestMatchers(HttpMethod.GET, "/api/reservas/**")
                                .hasAnyRole(HUESPED, RECEPCIONISTA, ADMINISTRADOR)
                        // Lo demás de reservas y folios: confirmar, modificar, llegada, salida, no-show, cargos y pagos
                        .requestMatchers("/api/reservas/**").hasAnyRole(RECEPCIONISTA, ADMINISTRADOR)

                        // ---------- Operación del apartamento (personal de servicio) ----------
                        .requestMatchers("/api/apartamentos/*/estado-operativo", "/api/apartamentos/*/novedades")
                                .hasAnyRole(PERSONAL_SERVICIO, ADMINISTRADOR)

                        // ---------- Mi cuenta: cualquier usuario con sesión, sobre la suya ----------
                        .requestMatchers("/api/usuarios/me", "/api/usuarios/me/**").authenticated()

                        // ---------- Solo administrador ----------
                        .requestMatchers("/api/usuarios/**", "/api/alojamiento/**", "/api/apartamentos/**",
                                "/api/bloqueos/**", "/api/temporadas/**", "/api/politicas-cancelacion/**",
                                "/api/conflictos-canal/**").hasRole(ADMINISTRADOR)

                        // Cualquier otra ruta: al menos haber iniciado sesión
                        .anyRequest().authenticated())
                .addFilterBefore(new FiltroJwt(jwtServicio), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** Orígenes que pueden llamar a la API desde el navegador (el frontend en React). */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${sga.cors.origenes:http://localhost:5173}") List<String> origenes) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(origenes);
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("*"));
        cors.setExposedHeaders(List.of("Location"));
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", cors);
        return fuente;
    }
}
