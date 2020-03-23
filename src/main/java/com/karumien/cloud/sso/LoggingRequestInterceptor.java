/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from 
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import lombok.extern.slf4j.Slf4j;


/**
 * Base Request/Response logging interceptor.
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 25. 9. 2019 22:07:02 
 */
@Component
@Slf4j
public class LoggingRequestInterceptor extends HandlerInterceptorAdapter {

    protected final static List<String> MDC_HEADERS_CONTEXT = 
        Arrays.asList("x-locale", "x-real-ip", "x-request-id", "x-forwarded-for", "x-forwarded-for",
            "x-original-forwarded-for", "x-trackingid", "user-agent");

    private ThreadLocal<Long> startTime = new ThreadLocal<Long>();
    
    @Value("${spring.application.env:dev}")
    private String env;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                
        ContentCachingRequestWrapper requestCacheWrapperObject = new ContentCachingRequestWrapper(request);
        requestCacheWrapperObject.getParameterMap();
        
        MDC.put("env", env);
        MDC.put("method", requestCacheWrapperObject.getMethod());
        MDC.put("uri", requestCacheWrapperObject.getRequestURI());
        MDC.put("protocol", requestCacheWrapperObject.getProtocol());
        MDC.put("content_type", requestCacheWrapperObject.getContentType());
        MDC.put("encoding", requestCacheWrapperObject.getCharacterEncoding());
        
        if (requestCacheWrapperObject.getAuthType() != null) {
            MDC.put("authType", requestCacheWrapperObject.getAuthType());
        }
        if (requestCacheWrapperObject.getUserPrincipal() != null) {
            MDC.put("principal", requestCacheWrapperObject.getUserPrincipal().getName());
        }
        String queryString = requestCacheWrapperObject.getQueryString();
        if (queryString != null) {
            MDC.put("query", queryString);
        }

        String client = requestCacheWrapperObject.getRemoteAddr();
        if (StringUtils.hasLength(client)) {
            MDC.put("client", client);
        }

        HttpSession session = requestCacheWrapperObject.getSession(false);
        if (session != null) {
            MDC.put("session", session.getId());
        }
        String user = requestCacheWrapperObject.getRemoteUser();
        if (user != null) {
            MDC.put("user", user);
        }

        //MDC.put("headers_all", toJson(new ServletServerHttpRequest(requestCacheWrapperObject).getHeaders()));
        MDC_HEADERS_CONTEXT.stream().forEach(h -> scripted(h, requestCacheWrapperObject));
       
        //if (requestCacheWrapperObject.getContentAsByteArray().length > 0) {
         //   MDC.put("request", getContentAsString(requestCacheWrapperObject.getContentAsByteArray(), this.maxPayloadLength, 
           //   requestCacheWrapperObject.getCharacterEncoding()));
        //}
        
        startTime.set(System.currentTimeMillis());        
        return true;
    }

    protected void scripted(String header, HttpServletRequest requestCacheWrapperObject) {
        
        String value = requestCacheWrapperObject.getHeader(header);
        if (value != null) {
            MDC.put(header, value);
        }
    }

    protected String toJson(HttpHeaders headers) {
//      return headers.toString();

        StringBuilder sb = new StringBuilder();
        for (String key : headers.keySet()) {
            
            Optional<String> val = headers.get(key).stream().findFirst();
            if (!val.isPresent()) {
                continue;
            }
            
            if (sb.length()==0) {
                sb.append(" {");
            } else {
                sb.append(", ");
            }
            
            sb.append("\"").append(key).append("\" : ");
            sb.append("\"").append(val.get()).append("\"");
        }
        
        return sb.append(" }").toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        
        long duration = System.currentTimeMillis()-startTime.get();
        MDC.put("status", ""+response.getStatus());
        MDC.put("duration_ms", ""+duration);
        
        //MDC.put("response", getContentAsString(wrappedResponse.getContentAsByteArray(), this.maxPayloadLength, response.getCharacterEncoding()));
       
        startTime.remove();
        wrappedResponse.copyBodyToResponse();  
        
        if (!StringUtils.endsWithIgnoreCase(MDC.get("uri"), "/publicKey")) {
            log.info("integration-call");
        }
        MDC.clear();
    }

    public static String getContentAsString(byte[] buf, int maxPayloadLength, String charsetName) {
        if (buf == null || buf.length == 0) return "";
        int length = Math.min(buf.length, maxPayloadLength);
        try {
          return new String(buf, 0, length, charsetName);
        } catch (UnsupportedEncodingException ex) {
          return "Unsupported Encoding";
        }
      }
}
