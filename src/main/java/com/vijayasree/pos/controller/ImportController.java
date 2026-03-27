package com.vijayasree.pos.controller;

import com.vijayasree.pos.dto.response.ApiResponse;
import com.vijayasree.pos.dto.response.ImportResponse;
import com.vijayasree.pos.service.ImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ImportController {

    private final ImportService importService;

    @PostMapping("/products")
    @PreAuthorize("hasAuthority('PRODUCT_IMPORT')")
    public ResponseEntity<ApiResponse<ImportResponse>> importProducts(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Please upload an Excel file"));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.endsWith(".xlsx")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Only .xlsx files are supported"));
        }

        ImportResponse result = importService.importProducts(file);
        String message = result.getSuccessCount() + " products imported, "
                + result.getFailedCount() + " failed";

        return ResponseEntity.ok(ApiResponse.success(result, message));
    }
}