package com.heim.api.users.infraestructure.controller;

import com.heim.api.drivers.domain.entity.Driver;
import com.heim.api.drivers.infraestructure.repository.DriverRepository;
import com.heim.api.users.domain.entity.User;
import com.heim.api.users.infraestructure.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/auth/")
public class AuthController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DriverRepository driverRepository;

    @PostMapping("auth")
    public ResponseEntity<?> authenticateUser(@RequestBody Map<String, String> request, HttpSession session) {
        String email = request.get("email");
        String password = request.get("password");

        // Intentar autenticar como usuario
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return authenticateAndRespond(user, password, session);
        }

        // Intentar autenticar como conductor
        Optional<Driver> driverOptional = driverRepository.findDriverByEmail(email);
        if (driverOptional.isPresent()) {
            Driver driver = driverOptional.get();
            return authenticateAndRespond(driver, password, session);
        }

        // Si no se encuentra ni como usuario ni como conductor, devolver error
        Map<String, String> response = new HashMap<>();
        response.put("message", "No dudamos que eres tú, pero no reconocemos tu email");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private ResponseEntity<?> authenticateAndRespond(Object user, String password, HttpSession session) {
        if (user instanceof User) {
            User u = (User) user;

            if (!passwordEncoder.matches(password, u.getPassword())) {
                return generateErrorResponse("No dudamos que eres tú, pero no reconocemos tu contraseña", HttpStatus.UNAUTHORIZED);
            }

            session.setAttribute("userId", u.getUserId());
            session.setAttribute("role", u.getRole());

            Map<String, Object>  response = new HashMap<>();
            response.put("userId",u.getUserId());
            response.put("role", u.getRole());

            return ResponseEntity.ok(response);

        } else if (user instanceof Driver) {
            Driver d = (Driver) user;

            if (!passwordEncoder.matches(password, d.getPassword())) {
                return generateErrorResponse("No dudamos que eres tú, pero no reconocemos tu contraseña", HttpStatus.UNAUTHORIZED);
            }

            session.setAttribute("userId", d.getDriverId());
            session.setAttribute("role", d.getRole());

            Map<String, Object>  response = new HashMap<>();
            response.put("userId",d.getDriverId());
            response.put("role", d.getRole());


            return ResponseEntity.ok(response);
        }

        return generateErrorResponse("No dudamos que eres tú, pero no reconocemos tu email", HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<Map<String, String>> generateErrorResponse(String message, HttpStatus status) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }



    @PostMapping("logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Logout exitoso");
    }

   /* @Autowired
    private AuthenticationManager authenticationManager;


   @Autowired
    private UserDetailsImpl userDetailsImpl;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("auth")
    public ResponseEntity<?> generateToken(@RequestBody JwtRequest jwtRequest) throws Exception {
        try{
            // Cargar usuario por correo electrónico
            UserDetails userDetails = this.userDetailsImpl.loadUserByUsername(jwtRequest.getEmail());

            authenticate(jwtRequest.getEmail(), jwtRequest.getPassword());

            // Generar token
            String token = this.jwtUtils.generateToken(userDetails.getUsername());
            return ResponseEntity.ok(new JwtResponse(token));

        } catch (DisabledException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "El usuario está deshabilitado. Por favor, contacte al administrador.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

        }catch (BadCredentialsException e){
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Tu contraseña es incorrecta. Por favor intentalo de nuevo.");
            return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            
        }catch (UsernameNotFoundException e){
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "No se encontró un usuario con ese email.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }catch (Exception e){
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Ocurrió un error inesperado. Nuestros desarrolladores ya fueron informados.");
            return  ResponseEntity.status(500).body(errorResponse);
        }

    }



    private void authenticate(String email, String password) {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

    }

    @PostMapping("logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return ResponseEntity.ok("Logout successful");
    } */
}
