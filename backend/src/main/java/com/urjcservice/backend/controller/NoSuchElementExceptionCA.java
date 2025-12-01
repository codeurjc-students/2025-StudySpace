package com.urjcservice.backend.controller;

import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
}