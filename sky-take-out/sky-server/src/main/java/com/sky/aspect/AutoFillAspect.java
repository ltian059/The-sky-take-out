package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Customized Apsect
 * Implement autofill common fields logic.
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    /**
     * Pointcut
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointcut() {
    }

    /**
     * Before Advice.
     * In the advice, assign the common fields in the tables.
     */
    @Before("autoFillPointcut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("START TO ASSIGN COMMON FIELDS AUTOMATICALLY...");
        //1. Get the operation type(UPDATE or INSERT) of the current intercepted method.
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        OperationType operationType = signature.getMethod().getAnnotation(AutoFill.class).value();

        //2. Get the parameter(the object needed to be assigned) of the current intercepted method.
        //By convention, the first parameter is the entity object
        Object[] args = joinPoint.getArgs();
        if (args.length == 0) return;
        Object entity = args[0];

        //3. Prepare the data to be assigned to the object.
        LocalDateTime time = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //4. According to current operation type, assign data to the object's corresponding field.
        try {
            if(entity instanceof List<?>){
                List<?> list = (List<?>) entity;
                for (Object item : list){
                    fillCommonFields(item, operationType, time, currentId);
                }
            }else{
                fillCommonFields(entity, operationType, time, currentId);
            }

        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private Method getDeclaredMethodSafe(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            return clazz.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
    private void fillCommonFields(Object entity, OperationType operationType, LocalDateTime time, Long currentId)
    throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (OperationType.INSERT == operationType) {
            //Assign 4 common fields.
            Method setCreateTime = getDeclaredMethodSafe(entity.getClass(), AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
            Method setUpdateTime = getDeclaredMethodSafe(entity.getClass(), AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setCreateUser = getDeclaredMethodSafe(entity.getClass(), AutoFillConstant.SET_CREATE_USER, Long.class);
            Method setUpdateUser = getDeclaredMethodSafe(entity.getClass(), AutoFillConstant.SET_UPDATE_USER, Long.class);
            //Assign the fields using reflection
            if (setCreateTime != null)
                setCreateTime.invoke(entity, time);
            if (setUpdateTime != null)
                setUpdateTime.invoke(entity, time);
            if (setCreateUser != null)
                setCreateUser.invoke(entity, currentId);
            if (setUpdateUser != null)
                setUpdateUser.invoke(entity, currentId);

        } else if (OperationType.UPDATE == operationType) {
            //Assign 2 common fields.
            Method setUpdateTime = getDeclaredMethodSafe(entity.getClass(), AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = getDeclaredMethodSafe(entity.getClass(), AutoFillConstant.SET_UPDATE_USER, Long.class);
            //Assign the fields using reflection
            if (setUpdateTime != null)
                setUpdateTime.invoke(entity, time);
            if (setUpdateUser != null)
                setUpdateUser.invoke(entity, currentId);
        }
    }


}
