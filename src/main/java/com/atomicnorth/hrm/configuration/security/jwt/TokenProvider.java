package com.atomicnorth.hrm.configuration.security.jwt;

import com.atomicnorth.hrm.configuration.ApplicationProperties;
import com.atomicnorth.hrm.configuration.multitenant.TenantContextHolder;
import com.atomicnorth.hrm.management.SecurityMetersService;
import com.atomicnorth.hrm.tenant.domain.Employee;
import com.atomicnorth.hrm.tenant.domain.accessgroup.UserAssociation;
import com.atomicnorth.hrm.tenant.domain.roles.Role;
import com.atomicnorth.hrm.tenant.helper.UserLoginDetail;
import com.atomicnorth.hrm.tenant.repository.EmployeeRepository;
import com.atomicnorth.hrm.tenant.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class TokenProvider {

    private static final String AUTHORITIES_KEY = "auth";
    private static final String INVALID_JWT_TOKEN = "Invalid JWT token.";
    private final Logger log = LoggerFactory.getLogger(TokenProvider.class);
    private final Key key;

    private final JwtParser jwtParser;

    private final long tokenValidityInMilliseconds;

    private final long tokenValidityInMillisecondsForRememberMe;

    private final SecurityMetersService securityMetersService;

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private UserRepository userRepository;

    public TokenProvider(ApplicationProperties applicationProperties, SecurityMetersService securityMetersService) {
        byte[] keyBytes;
        String secret = applicationProperties.getSecurity().getAuthentication().getJwt().getBase64Secret();
        if (!ObjectUtils.isEmpty(secret)) {
            log.debug("Using a Base64-encoded JWT secret key");
            keyBytes = Decoders.BASE64.decode(secret);
        } else {
            log.warn("Warning: the JWT key used is not Base64-encoded. " + "We recommend using the `application.security.authentication.jwt.base64-secret` key for optimum security.");
            secret = applicationProperties.getSecurity().getAuthentication().getJwt().getSecret();
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        key = Keys.hmacShaKeyFor(keyBytes);
        jwtParser = Jwts.parserBuilder().setSigningKey(key).build();
        this.tokenValidityInMilliseconds = 1000 * applicationProperties.getSecurity().getAuthentication().getJwt().getTokenValidityInSeconds();
        this.tokenValidityInMillisecondsForRememberMe = 1000 * applicationProperties.getSecurity().getAuthentication().getJwt().getTokenValidityInSecondsForRememberMe();

        this.securityMetersService = securityMetersService;
    }

    public String createToken(Authentication authentication, String clientId, boolean rememberMe) {
        String authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date validity;
        if (rememberMe) {
            validity = new Date(now + this.tokenValidityInMillisecondsForRememberMe);
        } else {
            validity = new Date(now + this.tokenValidityInMilliseconds);
        }

        return Jwts.builder().setSubject(authentication.getName()).setAudience(clientId).claim(AUTHORITIES_KEY, authorities).signWith(SignatureAlgorithm.HS512, key).setExpiration(validity).compact();
    }

    //ADD METHOD FOR MORE DETAIL OF USER
    public String createToken(Authentication authentication, String clientId, boolean rememberMe, com.atomicnorth.hrm.tenant.domain.User user) {
        String authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date validity;
        if (rememberMe) {
            validity = new Date(now + this.tokenValidityInMillisecondsForRememberMe);
        } else {
            validity = new Date(now + this.tokenValidityInMilliseconds);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return Jwts.builder().setSubject(authentication.getName()).setAudience(clientId).claim(AUTHORITIES_KEY, authorities)
                //ADD USER MORE DETAILS
                .claim("UserId", user.getId())
                .claim("DisplayName", user.getDisplayName())
                .claim("eMail", user.getEmail())
                .claim("CreatedDate", sdf.format(user.getStartDate()))
                .claim("Role", user.getAuthorities().stream().map(Role::getRoleId).collect(Collectors.toList()))
                .claim("UserType", user.getAssociations().stream().map(UserAssociation::getUserType).collect(Collectors.toList()))
                .claim("EmpId", user.getAssociations().stream().map(UserAssociation::getUserTypeId).filter(Objects::nonNull).findFirst().orElse(null))
                .signWith(SignatureAlgorithm.HS512, key)
                .setExpiration(validity)
                .compact();
    }

    public UserLoginDetail parse(String token) throws ParseException {
        Claims claims = jwtParser.parseClaimsJws(token).getBody();
        String tenantId = claims.getAudience();

        System.out.println(tenantId);
        /*CHECK HERE FOR TANSLATION IN FOR TENACY CHECK*/
        TenantContextHolder.setTenantId(tenantId);

        //FETCH USER DETAILS FROM TOKEN

        UserLoginDetail userLoginDetail = new UserLoginDetail();
        userLoginDetail.setUsername(((Integer) claims.get("UserId")).longValue());
        userLoginDetail.setAuthorities((String) claims.get(AUTHORITIES_KEY));
        userLoginDetail.setFirstname((String) claims.get("DisplayName"));
        userLoginDetail.setPrimaryemail((String) claims.get("eMail"));
        userLoginDetail.setEmpId((Integer) claims.get("EmpId"));
        List<Integer> roles = (List<Integer>) claims.get("Role");
        if (roles != null && !roles.isEmpty()) {
            String roleString = roles.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            userLoginDetail.setRole(roleString);
        }
        userLoginDetail.setSubject(claims.getSubject());
        return userLoginDetail;
    }

    public Authentication getAuthentication(UserLoginDetail userLoginDetail, String token) {
        Collection<? extends GrantedAuthority> authorities = Arrays.stream(userLoginDetail.getAuthorities().split(",")).filter(auth -> !auth.trim().isEmpty()).map(SimpleGrantedAuthority::new).collect(Collectors.toList());

        User principal = new User(userLoginDetail.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public boolean validateToken(String authToken) {
        try {
            Claims claims = jwtParser.parseClaimsJws(authToken).getBody();
            String subject = claims.getSubject();
            Optional<com.atomicnorth.hrm.tenant.domain.User> endDateByEmail = userRepository.findEndDateByEmail(subject);
            if (endDateByEmail.isPresent()) {
                if (endDateByEmail.get().getEndDate() != null) {
                    return !endDateByEmail.get().getEndDate().before(new Date());
                }
            }
            return true;
        } catch (ExpiredJwtException e) {
            this.securityMetersService.trackTokenExpired();

            log.trace(INVALID_JWT_TOKEN, e);
        } catch (UnsupportedJwtException e) {
            this.securityMetersService.trackTokenUnsupported();

            log.trace(INVALID_JWT_TOKEN, e);
        } catch (MalformedJwtException e) {
            this.securityMetersService.trackTokenMalformed();

            log.trace(INVALID_JWT_TOKEN, e);
        } catch (SignatureException e) {
            this.securityMetersService.trackTokenInvalidSignature();

            log.trace(INVALID_JWT_TOKEN, e);
        } catch (
                IllegalArgumentException e) { // TODO: should we let it bubble (no catch), to avoid defensive programming and follow the fail-fast principle?
            log.error("Token validation error {}", e.getMessage());
        }

        return false;
    }

    public Employee getByUserNameId(Integer usernameId) {
        return employeeRepository.findById(usernameId).orElse(null);
    }

    public String extractTenantId(String token) {
        try {
            Claims claims = jwtParser.parseClaimsJws(token).getBody();
            return claims.get("aud", String.class);
        } catch (ExpiredJwtException e) {
            this.securityMetersService.trackTokenExpired();

            log.trace(INVALID_JWT_TOKEN, e);
        } catch (UnsupportedJwtException e) {
            this.securityMetersService.trackTokenUnsupported();

            log.trace(INVALID_JWT_TOKEN, e);
        } catch (MalformedJwtException e) {
            this.securityMetersService.trackTokenMalformed();

            log.trace(INVALID_JWT_TOKEN, e);
        } catch (SignatureException e) {
            this.securityMetersService.trackTokenInvalidSignature();

            log.trace(INVALID_JWT_TOKEN, e);
        } catch (
                IllegalArgumentException e) { // TODO: should we let it bubble (no catch), to avoid defensive programming and follow the fail-fast principle?
            log.error("Token validation error {}", e.getMessage());
        }
        return null;
    }

}
