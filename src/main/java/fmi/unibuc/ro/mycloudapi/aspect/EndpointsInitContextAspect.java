package fmi.unibuc.ro.mycloudapi.aspect;

import fmi.unibuc.ro.mycloudapi.service.FileStorageService;
import fmi.unibuc.ro.mycloudapi.util.AuthenticationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
@Aspect
@Order(value=2)
@Slf4j
@RequiredArgsConstructor
public class EndpointsInitContextAspect {

    private final FileStorageService fileStorageService;
    private final AuthenticationUtil authenticationUtil;

    @Around("@annotation(org.springframework.web.bind.annotation.PostMapping) ||" +
            "@annotation(org.springframework.web.bind.annotation.GetMapping)")
    public Object authorizeRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        Method calledMethod = getCurrentMethod(joinPoint);
        log.debug("Calling method {}.{}", calledMethod.getDeclaringClass().getSimpleName(), calledMethod.getName());

        if (authenticationUtil.isAuthenticated()) {
            log.debug("Init context for {}", authenticationUtil.getLoggedInUserEmail());
            fileStorageService.initContext();
        }

        return joinPoint.proceed();
    }

    private Method getCurrentMethod(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod();
    }
}
