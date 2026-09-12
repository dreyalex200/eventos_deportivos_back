
package com.drey.eventsSports.infrastructure.controllers;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<ErrorResponse> handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object uri = request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
        if (uri == null) {
            uri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        }
        String httpMethod = request.getMethod();

        int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        if (status != null) {
            statusCode = Integer.parseInt(status.toString());
        }

        String code = "INTERNAL_SERVER_ERROR";
        String details = "An unexpected error occurred";

        if (statusCode == HttpStatus.NOT_FOUND.value()) {
            code = "ROUTE_NOT_FOUND";
            String path = (uri != null) ? uri.toString() : "/";
            details = "no route found for " + httpMethod + " " + path;
        }

        ErrorResponse response = ErrorResponse.builder()
                .status("error")
                .error(ErrorResponse.ErrorDetails.builder()
                        .code(code)
                        .details(details)
                        .build())
                .build();

        return new ResponseEntity<>(response, HttpStatus.valueOf(statusCode));
    }
}
