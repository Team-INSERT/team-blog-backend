package com.project.insert.global.jwt.util;

import com.project.insert.global.jwt.config.JwtProperties;
import com.project.insert.global.jwt.exception.InvalidJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final JwtProperties jwtProperties;

    public String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader(jwtProperties.getHeader());
        if(bearer.isEmpty()){
            throw InvalidJwtException.EXCEPTION;
        }

        return parseToken(bearer);
    }

    public String parseToken(String bearer){
        if(!bearer.startsWith(jwtProperties.getPrefix())){
            throw InvalidJwtException.EXCEPTION;
        }

        return  bearer.replaceAll(jwtProperties.getPrefix(), "").trim();
    }

    public Jws<Claims> getJwt(String token){
        if(token.isEmpty()){
            throw InvalidJwtException.EXCEPTION;
        }
        return Jwts.parser().setSigningKey(jwtProperties.getSecret()).parseClaimsJws(token);
    }

    public Claims getJwtBody(String bearer){
        Jws<Claims> jwt = getJwt(parseToken(bearer));
        return jwt.getBody();
    }

    public JwsHeader getHeader(String bearer) {
        Jws<Claims> jwt = getJwt(parseToken(bearer));
        return jwt.getHeader();
    }
}
