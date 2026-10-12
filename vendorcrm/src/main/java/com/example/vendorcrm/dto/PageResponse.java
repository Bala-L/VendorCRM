package com.example.vendorcrm.dto;


import java.util.List;

/** page is 1-based */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {}
