package com.opt.backend.common.exception;

import com.opt.backend.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailAlreadyUsed(EmailAlreadyUsedException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, "EMAIL_ALREADY_USED", Map.of("email", ex.getEmail()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect", request, "BAD_CREDENTIALS", Map.of());
    }

    @ExceptionHandler(InvalidPixelRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidPixelRequest(InvalidPixelRequestException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, ex.getCode(), Map.of());
    }

    @ExceptionHandler(CooldownActiveException.class)
    public ResponseEntity<ApiErrorResponse> handleCooldownActive(CooldownActiveException ex, HttpServletRequest request) {
        ApiErrorResponse body = ApiErrorResponse.of(HttpStatus.TOO_MANY_REQUESTS.value(), HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                ex.getMessage(), request.getRequestURI(), "COOLDOWN_ACTIVE", Map.of("remainingSeconds", ex.getRemainingSeconds()));
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(ex.getRemainingSeconds()))
                .body(body);
    }

    @ExceptionHandler(InsufficientPointsException.class)
    public ResponseEntity<ApiErrorResponse> handleInsufficientPoints(InsufficientPointsException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, "INSUFFICIENT_POINTS",
                Map.of("cost", ex.getCost(), "available", ex.getAvailable()));
    }

    @ExceptionHandler(NoBombChargeException.class)
    public ResponseEntity<ApiErrorResponse> handleNoBombCharge(NoBombChargeException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, "NO_BOMB_CHARGE", Map.of());
    }

    @ExceptionHandler(SpeedBuffAlreadyActiveException.class)
    public ResponseEntity<ApiErrorResponse> handleSpeedBuffAlreadyActive(SpeedBuffAlreadyActiveException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, "SPEED_BUFF_ALREADY_ACTIVE", Map.of());
    }

    @ExceptionHandler(NoFortressChargeException.class)
    public ResponseEntity<ApiErrorResponse> handleNoFortressCharge(NoFortressChargeException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, "NO_FORTRESS_CHARGE", Map.of());
    }

    @ExceptionHandler(SkinAlreadyOwnedException.class)
    public ResponseEntity<ApiErrorResponse> handleSkinAlreadyOwned(SkinAlreadyOwnedException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, "SKIN_ALREADY_OWNED", Map.of());
    }

    @ExceptionHandler(SkinNotOwnedException.class)
    public ResponseEntity<ApiErrorResponse> handleSkinNotOwned(SkinNotOwnedException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, "SKIN_NOT_OWNED", Map.of());
    }

    @ExceptionHandler(SkinNotPurchasableException.class)
    public ResponseEntity<ApiErrorResponse> handleSkinNotPurchasable(SkinNotPurchasableException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, "SKIN_NOT_PURCHASABLE", Map.of());
    }

    @ExceptionHandler(InvalidContributionException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidContribution(InvalidContributionException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, ex.getCode(), Map.of());
    }

    @ExceptionHandler(EmoteRateLimitException.class)
    public ResponseEntity<ApiErrorResponse> handleEmoteRateLimit(EmoteRateLimitException ex, HttpServletRequest request) {
        ApiErrorResponse body = ApiErrorResponse.of(HttpStatus.TOO_MANY_REQUESTS.value(), HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                ex.getMessage(), request.getRequestURI(), "EMOTE_RATE_LIMIT", Map.of());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(body);
    }

    @ExceptionHandler(InvalidEmoteException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidEmote(InvalidEmoteException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, "INVALID_EMOTE", Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Requete invalide");
        return build(HttpStatus.BAD_REQUEST, message, request, "VALIDATION_ERROR", Map.of("details", message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Erreur non geree sur {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur inattendue est survenue", request, "INTERNAL_ERROR", Map.of());
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message, HttpServletRequest request, String code, Map<String, Object> params) {
        ApiErrorResponse body = ApiErrorResponse.of(status.value(), status.getReasonPhrase(), message, request.getRequestURI(), code, params);
        return ResponseEntity.status(status).body(body);
    }
}
