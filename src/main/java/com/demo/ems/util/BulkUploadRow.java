package com.demo.ems.util;

public record BulkUploadRow<T>(int rowNumber, T value) {
}
