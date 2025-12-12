package com.urjcservice.backend.controller;

import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class NoSuchElementExceptionCA {

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(NoSuchElementException.class)
	public ResponseEntity<String> handleNotFound(NoSuchElementException exception) {  //IT IS NOT USED BUT NEEDED TO RETURN 404
		return org.springframework.http.ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
	}


	@ExceptionHandler(LockedException.class)
    public ResponseEntity<Object> handleLockedUser(LockedException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", new java.util.Date());
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", "Unauthorized");
        body.put("message", "User account is locked"); 
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

	@ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentials(BadCredentialsException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", new java.util.Date());
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", "Unauthorized");
        body.put("message", "Bad credentials");
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }
}