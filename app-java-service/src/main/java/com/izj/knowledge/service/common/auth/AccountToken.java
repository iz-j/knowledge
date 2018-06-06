package com.izj.knowledge.service.common.auth;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifier.BaseVerification;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Clock;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.izj.knowledge.service.base.i18n.MLString;
import com.izj.knowledge.service.base.time.SystemClock;

import lombok.extern.slf4j.Slf4j;

/**
 * Publish and verify a token(JWT) for user's account.
 *
 * @author iz-j
 *
 */
@Slf4j
public final class AccountToken {
    private AccountToken() {
    }

    private static final Clock CLOCK = new Clock() {
        @Override
        public java.util.Date getToday() {
            return java.util.Date.from(SystemClock.now().toInstant());
        }
    };

    public static Publisher publisher(String secret) {
        try {
            return new Publisher(Algorithm.HMAC256(secret));
        } catch (IllegalArgumentException | UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Verifier verifier(String secret) {
        try {
            return new Verifier(Algorithm.HMAC256(secret), CLOCK);
        } catch (IllegalArgumentException | UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static class Publisher {
        private final Algorithm algorithm;

        private Publisher(Algorithm algorithm) {
            this.algorithm = algorithm;
        }

        public String publish() {
            return JWT
                .create()
                .withIssuer("~~~~")
                /*
                 * .withSubject(account.getAccountId().toString())
                 * .withIssuedAt(java.util.Date.from(SystemClock.now().toInstant()))
                 * .withExpiresAt(java.util.Date.from(SystemClock.now().plusDays(1).toInstant()))
                 * .withJWTId(UUID.randomUUID().toString()) .withClaim("email", account.getEmail())
                 * .withClaim("companyId", account.getCompanyId().toString()) .withClaim("companyName",
                 * encode(account.getCompanyName())) .withClaim("companyIcon", account.getCompanyIcon())
                 * .withClaim("deptName", encode(account.getDeptName())) .withClaim("firstName",
                 * encode(account.getFirstName())) .withClaim("lastName", encode(account.getLastName()))
                 * .withClaim("fullName", encode(account.getFullName())) .withClaim("avatar", account.getAvatar())
                 * .withClaim("timezoneOffset", TimeZone.getTimeZone(account.getTimeZone()).getRawOffset())
                 * .withClaim("host", account.isHost()) .withClaim("admin", account.isAdmin())
                 */
                .sign(this.algorithm);
        }

        private String encode(MLString ms) {
            String s = ms.get();
            if (StringUtils.isEmpty(s)) {
                return StringUtils.EMPTY;
            }
            try {
                return URLEncoder
                    .encode(s, StandardCharsets.UTF_8.name())
                    .replace("*", "%2a")
                    .replace("-", "%2d")
                    .replace("+", "%20");
            } catch (UnsupportedEncodingException e) {
                log.error("Failed to encode a claim! -> " + s, e);
                throw new IllegalStateException(e);
            }
        }
    }

    public static class Verifier {
        private final Algorithm algorithm;
        private final Clock clock;

        private Verifier(Algorithm algorithm, Clock clock) {
            this.algorithm = algorithm;
            this.clock = clock;
        }

        /**
         * @param token
         * @return accountId
         */
        public void verify(String token) {
            try {
                BaseVerification verification = (BaseVerification)JWT
                    .require(this.algorithm)
                    .withIssuer("hoge-connect");
                JWTVerifier verifier = verification.build(this.clock);
                DecodedJWT jwt = verifier.verify(token);

            } catch (JWTVerificationException e) {
                log.error("Verification error!", e);
                throw e;
            }
        }
    }

}
