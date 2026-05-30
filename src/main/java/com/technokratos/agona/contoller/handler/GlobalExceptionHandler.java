package com.technokratos.agona.contoller.handler;

import com.technokratos.agona.exception.NotFoundException;
import com.technokratos.agona.exception.ServiceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private boolean isAjax(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        return "XMLHttpRequest".equals(requestedWith)
                || (accept != null && accept.contains("application/json") && !accept.contains("text/html"));
    }

    @ExceptionHandler(ServiceException.class)
    public Object handleServiceException(ServiceException ex, HttpServletRequest request) {
        log.warn("ServiceException [{}] на {}: {}", ex.getHttpStatus(), request.getRequestURI(), ex.getMessage());
        if (isAjax(request)) {
            return ResponseEntity.status(ex.getHttpStatus())
                    .body(ExceptionMessage.builder()
                            .exceptionName(ex.getClass().getSimpleName())
                            .message(ex.getMessage())
                            .build());
        }
        return errorPage(ex.getHttpStatus(), resolveTitle(ex.getHttpStatus()), ex.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public Object handleNotFoundException(NotFoundException ex, HttpServletRequest request) {
        log.warn("NotFoundException на {}: {}", request.getRequestURI(), ex.getMessage());
        if (isAjax(request)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ExceptionMessage.builder()
                            .exceptionName(ex.getClass().getSimpleName())
                            .message(ex.getMessage())
                            .build());
        }
        return errorPage(HttpStatus.NOT_FOUND, "Не найдено", ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Object handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        log.warn("NoResourceFoundException на {}: {}", request.getRequestURI(), ex.getMessage());
        if (isAjax(request)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ExceptionMessage.builder()
                            .exceptionName(ex.getClass().getSimpleName())
                            .message(ex.getMessage())
                            .build());
        }
        return errorPage(HttpStatus.NOT_FOUND, "Страница не найдена",
                "Запрошенная страница не существует или была удалена.");
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Object handleValidationException(Exception ex, HttpServletRequest request) {
        log.warn("Ошибка валидации на {}: {}", request.getRequestURI(), ex.getMessage());
        String message = ex instanceof BindException be
                ? be.getBindingResult().getFieldErrors().stream()
                        .map(e -> e.getField() + ": " + e.getDefaultMessage())
                        .findFirst()
                        .orElse("Некорректные данные")
                : "Некорректные данные";

        if (isAjax(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ExceptionMessage.builder()
                            .exceptionName("ValidationException")
                            .message(message)
                            .build());
        }
        return errorPage(HttpStatus.BAD_REQUEST, "Некорректный запрос", message);
    }

    @ExceptionHandler(Exception.class)
    public Object handleAll(Exception ex, HttpServletRequest request) {
        log.error("Необработанное исключение на {}", request.getRequestURI(), ex);
        if (isAjax(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ExceptionMessage.builder()
                            .exceptionName(ex.getClass().getSimpleName())
                            .message(ex.getMessage())
                            .build());
        }
        return errorPage(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера",
                "Что-то пошло не так на нашей стороне. Пожалуйста, попробуйте позже.");
    }

    private ModelAndView errorPage(HttpStatus status, String title, String message) {
        ModelAndView mav = new ModelAndView("error/error");
        mav.setStatus(status);
        mav.addObject("status", status.value());
        mav.addObject("title", title);
        mav.addObject("message", message);
        return mav;
    }

    private String resolveTitle(HttpStatus status) {
        return switch (status) {
            case NOT_FOUND -> "Не найдено";
            case FORBIDDEN -> "Доступ запрещён";
            case UNAUTHORIZED -> "Требуется авторизация";
            case BAD_REQUEST -> "Некорректный запрос";
            case CONFLICT -> "Конфликт данных";
            default -> "Ошибка " + status.value();
        };
    }
}
