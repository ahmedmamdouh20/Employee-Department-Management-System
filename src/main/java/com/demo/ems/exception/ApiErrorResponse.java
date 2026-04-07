package com.demo.ems.exception;

public record ApiErrorResponse(int status, String message) {
}
