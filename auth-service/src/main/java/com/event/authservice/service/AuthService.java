package com.event.authservice.service;

import com.event.authservice.aop.AuditAction;
import com.event.authservice.dto.request.LoginRequest;
import com.event.authservice.dto.request.RegisterRequest;
import com.event.authservice.dto.response.AuthResponse;
import com.event.authservice.dto.response.TokenValidationResponse;
import com.event.authservice.dto.response.UserResponse;
import com.event.authservice.entity.Role;
import com.event.authservice.entity.RoleName;
import com.event.authservice.entity.User;
import com.event.authservice.entity.UserStatus;
import com.event.authservice.exception.BadRequestException;
import com.event.authservice.exception.ResourceNotFoundException;
import com.event.authservice.repository.RoleRepository;
import com.event.authservice.repository.UserRepository;
import com.event.authservice.security.AuthUserPrincipal;
import com.event.authservice.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    @AuditAction("REGISTER_USER")
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new BadRequestException("Email is already registered");
        }

        if (request.getRole() != RoleName.PARTICIPANT) {
            throw new BadRequestException("Public registration only allows PARTICIPANT accounts");
        }

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRole()));

        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(Set.of(role));

        return toUserResponse(userRepository.save(user));
    }

    @AuditAction("LOGIN_USER")
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail().trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        List<String> roles = extractRoles(user);
        String token = jwtService.generateToken(user.getId(), user.getEmail(), roles);
        return new AuthResponse(token, "Bearer", user.getId(), user.getEmail(), roles);
    }

    @Transactional(readOnly = true)
    @AuditAction("GET_CURRENT_USER")
    public UserResponse getCurrentUser(AuthUserPrincipal principal) {
        if (principal == null || principal.getUserId() == null) {
            throw new BadCredentialsException("Invalid session");
        }
        return getUserById(principal.getUserId());
    }

    @Transactional(readOnly = true)
    @AuditAction("GET_USER_BY_ID")
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toUserResponse(user);
    }

    @Transactional(readOnly = true)
    @AuditAction("GET_USER_ROLES")
    public List<String> getUserRoles(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return extractRoles(user);
    }

    @AuditAction("VALIDATE_TOKEN")
    public TokenValidationResponse validate(AuthUserPrincipal principal) {
        if (principal == null || principal.getUserId() == null) {
            return new TokenValidationResponse(false, null, null, null);
        }
        return new TokenValidationResponse(true, principal.getUserId(), principal.getEmail(), principal.getRoles());
    }

    public void ensureSelfOrAdmin(AuthUserPrincipal principal, Long userId) {
        if (principal == null || principal.getUserId() == null) {
            throw new AccessDeniedException("Authentication is required");
        }
        boolean isAdmin = principal.getRoles() != null && principal.getRoles().contains("ADMIN");
        boolean isSelf = principal.getUserId().equals(userId);
        if (!isAdmin && !isSelf) {
            throw new AccessDeniedException("Access is denied");
        }
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getStatus().name(),
                extractRoles(user)
        );
    }

    private List<String> extractRoles(User user) {
        return user.getRoles().stream()
                .map(role -> role.getName().name())
                .sorted()
                .toList();
    }
}
