package com.example.xlsxziptotxtzip.logging.aop;

import com.example.xlsxziptotxtzip.base.AbstractBaseServiceTest;

import ch.qos.logback.classic.Level;
import com.example.xlsxziptotxtzip.common.exception.ApiException;
import com.example.xlsxziptotxtzip.convert.exception.InvalidZipContentException;
import com.example.xlsxziptotxtzip.convert.exception.XlsxConversionException;
import com.example.xlsxziptotxtzip.convert.exception.ZipProcessingException;
import com.example.xlsxziptotxtzip.logging.model.entity.LogEntity;
import com.example.xlsxziptotxtzip.logging.service.LogService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoggerAspectJTest extends AbstractBaseServiceTest {

    @InjectMocks
    private LoggerAspectJ loggerAspectJ;

    @Mock
    private LogService logService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private ServletRequestAttributes servletRequestAttributes;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private Signature signature;

    @BeforeEach
    public void setUp() {
        // Initialize mocks and set request attributes
        when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
        when(servletRequestAttributes.getResponse()).thenReturn(httpServletResponse);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);

        // Mock JoinPoint signature
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("testMethod");
        when(signature.getDeclaringTypeName()).thenReturn("LoggerAspectJ");
        when(signature.getDeclaringType()).thenReturn(LoggerAspectJ.class);
    }

    @Test
    void logAfterThrowing_withInvalidZipContentException_persistsBadRequestStatus() {
        // given
        ApiException ex = new InvalidZipContentException("No XLSX files in ZIP");

        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://localhost/api/upload-zip"));
        when(httpServletRequest.getMethod()).thenReturn("POST");
        when(signature.getName()).thenReturn("uploadZip");

        // when
        loggerAspectJ.logAfterThrowing(joinPoint, ex);

        // then
        verify(logService).saveLogToDatabase(argThat(log ->
                "http://localhost/api/upload-zip".equals(log.getEndpoint()) &&
                        "POST".equals(log.getMethod()) &&
                        "uploadZip".equals(log.getOperation()) &&
                        ex.getMessage().equals(log.getMessage()) &&
                        ex.getMessage().equals(log.getResponse()) &&
                        ex.getClass().getName().equals(log.getErrorType()) &&
                        log.getStatus() == HttpStatus.BAD_REQUEST
        ));
    }

    @Test
    void logAfterThrowing_withZipProcessingException_persistsInternalServerErrorStatus() {
        // given
        ApiException ex = new ZipProcessingException("Error while creating TXT ZIP response",
                new RuntimeException("IO boom"));

        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://localhost/api/upload-zip"));
        when(httpServletRequest.getMethod()).thenReturn("POST");
        when(signature.getName()).thenReturn("uploadZip");

        // when
        loggerAspectJ.logAfterThrowing(joinPoint, ex);

        // then
        verify(logService).saveLogToDatabase(argThat(log ->
                "http://localhost/api/upload-zip".equals(log.getEndpoint()) &&
                        "POST".equals(log.getMethod()) &&
                        "uploadZip".equals(log.getOperation()) &&
                        ex.getMessage().equals(log.getMessage()) &&
                        ex.getMessage().equals(log.getResponse()) &&
                        ex.getClass().getName().equals(log.getErrorType()) &&
                        log.getStatus() == HttpStatus.INTERNAL_SERVER_ERROR
        ));
    }

    @Test
    void logAfterThrowing_whenSaveLogThrows_logsErrorButDoesNotPropagate() {
        // given
        ApiException ex = new ZipProcessingException("Conversion failed", new RuntimeException("DB down"));

        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://localhost/api/upload-zip"));
        when(httpServletRequest.getMethod()).thenReturn("POST");
        when(signature.getName()).thenReturn("uploadZip");

        doThrow(new RuntimeException("Database error"))
                .when(logService).saveLogToDatabase(any(LogEntity.class));

        // when / then
        assertDoesNotThrow(() -> loggerAspectJ.logAfterThrowing(joinPoint, ex));

        verify(logService, times(1)).saveLogToDatabase(any(LogEntity.class));

        Optional<String> dbError = logTracker.checkMessage(Level.ERROR, "Database error");
        assertTrue(dbError.isPresent(), "Expected ERROR log with 'Database error' message to be present");
    }

    @Test
    void logAfterThrowing_whenRequestAttributesAreNull_logsErrorAndDoesNotPersistLog() {
        // given
        RequestContextHolder.resetRequestAttributes();
        Exception ex = new RuntimeException("Some error");

        // when
        loggerAspectJ.logAfterThrowing(joinPoint, ex);

        // then
        verify(logService, never()).saveLogToDatabase(any(LogEntity.class));

        Optional<String> logMessage =
                logTracker.checkMessage(Level.ERROR, "logAfterThrowing | Request Attributes are null!");
        assertTrue(logMessage.isPresent(), "Expected error log message not found.");
        assertEquals("logAfterThrowing | Request Attributes are null!", logMessage.get());
    }

    @Test
    void logAfterReturning_withStringResult_persistsLog() throws IOException {
        // given
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://localhost/api/test"));
        when(httpServletRequest.getMethod()).thenReturn("POST");
        when(httpServletResponse.getStatus()).thenReturn(HttpStatus.OK.value());
        when(signature.getName()).thenReturn("testMethod");
        when(joinPoint.getSignature()).thenReturn(signature);

        // when
        loggerAspectJ.logAfterReturning(joinPoint, "test response");

        // then
        verify(logService, times(1)).saveLogToDatabase(any(LogEntity.class));
    }

    @Test
    void logAfterReturning_withJsonNode_serializesToJson_andPersistsLog() throws IOException {
        // given
        JsonNode jsonNode = new ObjectMapper().createObjectNode().put("key", "value");

        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://localhost/api/test"));
        when(httpServletRequest.getMethod()).thenReturn("POST");
        when(httpServletResponse.getStatus()).thenReturn(HttpStatus.OK.value());
        when(signature.getName()).thenReturn("testMethod");
        when(joinPoint.getSignature()).thenReturn(signature);

        // when
        loggerAspectJ.logAfterReturning(joinPoint, jsonNode);

        // then
        verify(logService, times(1)).saveLogToDatabase(any(LogEntity.class));
    }

    @Test
    void logAfterReturning_whenRequestAttributesAreNull_doesNotPersistLog_andLogsError() throws IOException {
        // given
        RequestContextHolder.resetRequestAttributes();

        // when
        loggerAspectJ.logAfterReturning(mock(JoinPoint.class), "test response");

        // then
        verify(logService, never()).saveLogToDatabase(any(LogEntity.class));

        Optional<String> logMessage =
                logTracker.checkMessage(Level.ERROR, "logAfterReturning | Request Attributes are null!");
        assertTrue(logMessage.isPresent(), "Expected error log message not found.");
    }

    @Test
    void logAfterReturning_whenSaveLogThrowsException_logsErrorButDoesNotPropagate() throws IOException {
        // given
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://localhost/api/test"));
        when(httpServletRequest.getMethod()).thenReturn("POST");
        when(httpServletResponse.getStatus()).thenReturn(HttpStatus.OK.value());
        when(signature.getName()).thenReturn("testMethod");
        when(joinPoint.getSignature()).thenReturn(signature);

        doThrow(new RuntimeException("Database error"))
                .when(logService).saveLogToDatabase(any(LogEntity.class));

        // when / then
        assertDoesNotThrow(() -> loggerAspectJ.logAfterReturning(joinPoint, "test response"));

        verify(logService, times(1)).saveLogToDatabase(any(LogEntity.class));

        Optional<String> databaseError =
                logTracker.checkMessage(Level.ERROR, "Database error");
        assertTrue(databaseError.isPresent(), "Expected ERROR log with 'Database error' message to be present");
    }

    @Test
    void getHttpStatusFromException_coversAllBranches_includingApiExceptionSubclasses() {
        // given
        BindingResult dummyBinding = mock(BindingResult.class);

        ApiException invalidZip = new InvalidZipContentException("bad zip");
        ApiException xlsxError = new XlsxConversionException("xlsx fail", new RuntimeException("cause"));
        ApiException zipError = new ZipProcessingException("zip fail", new RuntimeException("cause"));

        var cases = Map.<Exception, String>ofEntries(
                Map.entry(invalidZip, HttpStatus.BAD_REQUEST.name()),
                Map.entry(xlsxError, HttpStatus.INTERNAL_SERVER_ERROR.name()),
                Map.entry(zipError, HttpStatus.INTERNAL_SERVER_ERROR.name()),
                Map.entry(new MethodArgumentNotValidException(null, dummyBinding),
                        HttpStatus.BAD_REQUEST.name()),
                Map.entry(new ConstraintViolationException(Collections.emptySet()),
                        HttpStatus.BAD_REQUEST.name()),
                Map.entry(new RuntimeException("oops"),
                        HttpStatus.INTERNAL_SERVER_ERROR.name())
        );

        // when / then
        cases.forEach((exc, expected) -> {
            String actual = (String) org.springframework.test.util.ReflectionTestUtils
                    .invokeMethod(loggerAspectJ, "getHttpStatusFromException", exc);
            assertEquals(expected, actual, "Mismatch for: " + exc.getClass().getSimpleName());
        });
    }

}