package com.atomicnorth.hrm.tenant.web.rest;

import com.atomicnorth.hrm.configuration.multitenant.TenantContextHolder;
import com.atomicnorth.hrm.configuration.security.jwt.JWTFilter;
import com.atomicnorth.hrm.configuration.security.jwt.TokenProvider;
import com.atomicnorth.hrm.tenant.domain.Employee;
import com.atomicnorth.hrm.tenant.domain.User;
import com.atomicnorth.hrm.tenant.domain.applicationLogin.SesAppLoginSetting;
import com.atomicnorth.hrm.tenant.helper.Constant;
import com.atomicnorth.hrm.tenant.repository.UserRepository;
import com.atomicnorth.hrm.tenant.repository.applicationLogin.SesAppLoginSettingRepository;
import com.atomicnorth.hrm.tenant.service.UserService;
import com.atomicnorth.hrm.tenant.service.dto.twoFa.OTPRequest;
import com.atomicnorth.hrm.tenant.service.util.TwoFactorAuthUtil;
import com.atomicnorth.hrm.tenant.web.rest.vm.LoginVM;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller to authenticate users.
 */
@RestController
@RequestMapping("/api")
public class UserJWTController {
    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserRepository userRepository;
    private final UserService userService;

    private final TwoFactorAuthUtil twoFactorAuthUtil;

    private final SesAppLoginSettingRepository sesAppLoginSettingRepository;

    public UserJWTController(TokenProvider tokenProvider, AuthenticationManagerBuilder authenticationManagerBuilder, UserRepository userRepository, UserService userService,TwoFactorAuthUtil twoFactorAuthUtil,SesAppLoginSettingRepository sesAppLoginSettingRepository) {
        this.tokenProvider = tokenProvider;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.userRepository = userRepository;
        this.userService = userService;
        this.twoFactorAuthUtil = twoFactorAuthUtil;
        this.sesAppLoginSettingRepository = sesAppLoginSettingRepository;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<JWTToken> authorize(@Valid @RequestBody LoginVM loginVM) throws SQLException {
        TenantContextHolder.setTenantId(loginVM.getClientId());
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginVM.getUsername(),
                loginVM.getPassword()
        );
        if (!userService.isUserActive(loginVM.getUsername())){
            return new ResponseEntity<>(new JWTToken("This user has been de-activated. Please contact admin.", true), HttpStatus.OK);
        }
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Optional<User> optionalUser = userRepository.findOneWithAuthoritiesByEmail(loginVM.getUsername());
        User user = optionalUser.get();
        Optional<SesAppLoginSetting> optionalSetting  = sesAppLoginSettingRepository.findByFeatureCodeAndStatus(Constant.TWO_FACTOR_LOGIN,Constant.TWO_FACTOR_LOGIN_STATUS_ACTIVE);
        if (optionalSetting.isPresent()) {
            SesAppLoginSetting appLoginSetting = optionalSetting.get();
            if ("Y".equalsIgnoreCase(appLoginSetting.getStatus())) {
                if (!user.isTwoFaVerified()) {
                    String secretKey = TwoFactorAuthUtil.generateSecretKey();
                    user.setSecretKey(secretKey);
                    userRepository.save(user);
                    String qrUrl = TwoFactorAuthUtil.getGoogleAuthenticatorQRCode(
                            secretKey, user.getEmail(), "MY_APP"
                    );
                    return ResponseEntity.ok(new JWTToken(true, qrUrl));
                }
                return ResponseEntity.ok(new JWTToken("OTP required", true));
            }
        }
        String jwt = tokenProvider.createToken(authentication, loginVM.getClientId(), loginVM.isRememberMe(), user);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
        return new ResponseEntity<>(new JWTToken(jwt), httpHeaders, HttpStatus.OK);
    }
    @PostMapping("/verify-otp")
    public ResponseEntity<JWTToken> verifyOtp(@RequestBody OTPRequest request) throws SQLException {
        String tenantId = request.getClientId();
        TenantContextHolder.setTenantId(tenantId);
        Optional<User> optionalUser = userRepository.findOneWithAuthoritiesByEmail(request.getUsername());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.ok(new JWTToken("Invalid user", true));
        }
        User user = optionalUser.get();
        if (!TwoFactorAuthUtil.verifyOTP(user.getSecretKey(), request.getOtp())) {
            return ResponseEntity.ok(new JWTToken("Invalid OTP. Please try again.", true));
        }
        if (!user.isTwoFaVerified()) {
            user.setTwoFaVerified(true);
            userRepository.save(user);
        }
        List<SimpleGrantedAuthority> authorities = user.getAuthorities().stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleCode()))
                .collect(Collectors.toList());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null,
                        authorities
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.createToken(authentication, request.getClientId(), false, user);

        return ResponseEntity.ok(new JWTToken(jwt));
    }





    @GetMapping("/users/{usernameId}")
    public ResponseEntity<Employee> getByUserNameId(@PathVariable Integer usernameId) {
        try {
            Integer id = Integer.parseInt(String.valueOf(usernameId));
            Employee user = tokenProvider.getByUserNameId(id);
            return ResponseEntity.ok(user);
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    static class JWTToken {

        private String idToken;

        @Setter
        @Getter
        private String message;
        private boolean isError;

        private String token;

        private boolean otpRequired;
        private boolean showQr;

        private String qrUrl;

        JWTToken(String idToken) {
            this.idToken = idToken;
        }

        JWTToken(String message, boolean isError) {
            this.message = message;
            this.isError = isError;
        }
        JWTToken( boolean isError,String message) {
            this.message = message;
            this.isError = isError;
        }

        @JsonProperty("id_token")
        String getIdToken() {
            return idToken;
        }

        void setIdToken(String idToken) {
            this.idToken = idToken;
        }

        public boolean isError() {
            return isError;
        }

        public void setError(boolean error) {
            isError = error;
        }
    }
}
