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

import java.util.Arrays;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.jboss.logging.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

//@Aspect
//@Order(1)
//@Component
//@ConditionalOnExpression("${endpoint.aspect.enabled:true}")
public class EndpointAspect {

  private final static List<String> MDC_CONTEXT = Arrays.asList("crmAccountId", "crmContactId", "moduleId", "nav4Id");
    
  @Around("within(com.karumien.cloud.sso.api..*)")
  public void aroundAdvice(ProceedingJoinPoint joinPoint) {
      CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();
      for (int i = 0; i < codeSignature.getParameterNames().length; i++) {
          String name = codeSignature.getParameterNames()[i];
          if (MDC_CONTEXT.contains(name)) {
              MDC.put(name, joinPoint.getArgs()[i]);
          }
      }
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
