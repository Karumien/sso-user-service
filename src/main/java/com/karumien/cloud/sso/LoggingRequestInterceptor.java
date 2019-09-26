/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from 
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso;


import static net.logstash.logback.marker.Markers.appendRaw;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.MDC;
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

    // TODO spring config
    private int maxPayloadLength = 1000;
    
    private ThreadLocal<Long> startTime = new ThreadLocal<Long>();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        startTime.set(System.currentTimeMillis());        
        
        ContentCachingRequestWrapper requestCacheWrapperObject = new ContentCachingRequestWrapper(request);
        requestCacheWrapperObject.getParameterMap();
        
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

        MDC.put("headers", "" +  new ServletServerHttpRequest(requestCacheWrapperObject).getHeaders());
        
        String locale = requestCacheWrapperObject.getHeader("x-locale");
        if (locale != null) {
            MDC.put("x-locale", locale);
        }

        if (requestCacheWrapperObject.getContentAsByteArray().length > 0) {
         //   MDC.put("request", getContentAsString(requestCacheWrapperObject.getContentAsByteArray(), this.maxPayloadLength, 
           //   requestCacheWrapperObject.getCharacterEncoding()));
        }
        
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        
        long duration = System.currentTimeMillis()-startTime.get();
        MDC.put("status", ""+response.getStatus());
        MDC.put("duration", ""+duration);
        
        //MDC.put("response", getContentAsString(wrappedResponse.getContentAsByteArray(), this.maxPayloadLength, response.getCharacterEncoding()));
       
        startTime.remove();
        wrappedResponse.copyBodyToResponse();  
        
        log.info(
                //appendRaw("access_token", "{ access_token : { moje : 5 } }" )
                //MDC.get("access_token"))
                //, 
                "integration-call");
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
