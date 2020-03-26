/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.karumien.cloud.sso;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ctc.wstx.io.CharsetNames;

@Aspect
@Order(1)
@Component
@ConditionalOnExpression("${endpoint.aspect.enabled:true}")
public class LoggingMDCAspect {

  private final static List<String> MDC_CONTEXT = Arrays.asList("accountNumber", "contactNumber", "moduleId", "nav4Id");
    
  @Around("within(com.karumien.cloud.sso.api.*)")
  public Object prepareMDCContext(ProceedingJoinPoint joinPoint) throws Throwable {
      
      final Signature signatureClazz = joinPoint.getSignature();
      MDC.put("class", signatureClazz.getDeclaringType().getSimpleName());
      
      CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();
      for (int i = 0; i < codeSignature.getParameterNames().length; i++) {
          String name = codeSignature.getParameterNames()[i];
          if (MDC_CONTEXT.contains(name) && joinPoint.getArgs()[i] != null) {
              MDC.put(name, ""+joinPoint.getArgs()[i]);
          }
      }

      final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
      Method method = signature.getMethod();
      MDC.put("class_method", method.getName() + "()");
      
      RequestMapping requestMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
      if (requestMapping != null && requestMapping.path() != null && requestMapping.path().length > 0) {
          MDC.put("path", requestMapping.path()[0]);
      }

      return joinPoint.proceed();
  }
  
//  @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping)")
//  public Object prepareMDCRequest(ProceedingJoinPoint joinPoint) throws Throwable {
//
//
//
//      return joinPoint.proceed();
//  }

   @AfterThrowing(pointcut = ("within(com.karumien.cloud.sso.api.*)"), throwing = "e")
    public void endpointAfterThrowing(JoinPoint p, Exception e) throws Exception {
       MDC.put("exception", LoggingRequestInterceptor.getContentAsString(getThrowableStackTraceBytes(e), 1000, CharsetNames.CS_UTF8));        
    }
   
   private final static byte[] getThrowableStackTraceBytes(Throwable throwable) {
       ByteArrayOutputStream baos = new ByteArrayOutputStream();
       throwable.printStackTrace(new PrintStream(baos));
       return baos.toByteArray();
   }
  
//    @Around("within(com.karumien.cloud.sso.api..*)")
//    public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
//        CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();
//
//        for (String name : codeSignature.getParameterNames()) {
//            System.out.println("First parameter's name: " + codeSignature.getParameterNames()[0]);
//        }
//
//        return joinPoint.proceed();
//    }
//
//    @Before("within(com.karumien.cloud.sso.api..*)")
//    public void endpointBefore(JoinPoint joinPoint) {
//        Signature methodSignature = joinPoint.getSignature();
//        String[] sigParamNames = methodSignature. codeSignature.getParameterNames(); 
//    }
//    
//    @AfterReturning(value = ("within(com.karumien.cloud.sso.api..*)"), returning = "returnValue")
//    public void endpointAfterReturning(JoinPoint p, Object returnValue) {
//        
//        
//    }
//    
//    @AfterThrowing(pointcut = ("within(com.karumien.cloud.sso.api..*)"), throwing = "e")
//    public void endpointAfterThrowing(JoinPoint p, Exception e) throws Exception {
//        
//    }

}
