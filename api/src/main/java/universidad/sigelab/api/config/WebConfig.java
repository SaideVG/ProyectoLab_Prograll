package universidad.sigelab.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final InterceptorSesion interceptorSesion;
    private final String[] origenesPermitidos;

    public WebConfig(InterceptorSesion interceptorSesion, @Value("${sigelab.cors.origenes}") String[] origenesPermitidos) {
        this.interceptorSesion = interceptorSesion;
        this.origenesPermitidos = origenesPermitidos;
    }

    /** Todo /api pide token, menos el login y la prueba de conexión. */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptorSesion)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/estado");
    }

    /*
     * La app de Android no necesita CORS. Solo aplica cuando la app se abre
     * en un navegador (Flutter web), que es como se prueba en la computadora.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(origenesPermitidos)
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type");
    }
}
