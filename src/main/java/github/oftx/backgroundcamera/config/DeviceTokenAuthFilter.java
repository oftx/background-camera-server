package github.oftx.backgroundcamera.config;

import github.oftx.backgroundcamera.entity.Device;
import github.oftx.backgroundcamera.repository.DeviceRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
public class DeviceTokenAuthFilter extends OncePerRequestFilter {

    private final DeviceRepository deviceRepository;
    private final PasswordEncoder passwordEncoder;

    public DeviceTokenAuthFilter(DeviceRepository deviceRepository, PasswordEncoder passwordEncoder) {
        this.deviceRepository = deviceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String deviceToken = request.getHeader("X-Device-Token");
        // We expect the deviceId to be part of the request, e.g., for file uploads
        final String deviceId = request.getParameter("deviceId");

        if (deviceToken == null || deviceId == null || !request.getRequestURI().contains("/api/v1/photos/upload")) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isPresent() && deviceOpt.get().getAuthTokenHash() != null) {
            Device device = deviceOpt.get();
            if (passwordEncoder.matches(deviceToken, device.getAuthTokenHash())) {
                // Authenticated as a device
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        device.getId(), // The principal is the deviceId
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_DEVICE"))
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}