package com.example.backend.security;

import com.example.backend.domain.User;
import com.example.backend.exception.UserNotInCoupleException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CoupleScopeAspect {

    @Around("@annotation(com.example.backend.security.CoupleScoped)")
    public Object requireCouple(ProceedingJoinPoint joinPoint) throws Throwable {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof User user && user.getCouple() == null) {
                throw new UserNotInCoupleException("Пользователь не состоит в паре");
            }
        }
        return joinPoint.proceed();
    }
}
