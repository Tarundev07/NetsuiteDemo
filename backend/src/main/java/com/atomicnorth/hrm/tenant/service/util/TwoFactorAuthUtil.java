package com.atomicnorth.hrm.tenant.service.util;

import com.atomicnorth.hrm.tenant.helper.Constant;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
@Component
public class TwoFactorAuthUtil {

    private static final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    public static String generateSecretKey() {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }

    public static String getGoogleAuthenticatorQRCode(String secretKey, String username, String issuer) {
        String otpAuthUrl = String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                issuer,
                username,
                secretKey,
                issuer
        );
        return Constant.TWO_FACTOR_LOGIN_QR + urlEncode(otpAuthUrl);
    }

    private static String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static boolean verifyOTP(String secretKey, String code) {
        try {
            int otp = Integer.parseInt(code);
            return gAuth.authorize(secretKey, otp);
        } catch (Exception e) {
            return false;
        }
    }
}
