package com.vijayasree.pos.service;

import com.vijayasree.pos.dto.response.ImportResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ImportService {
    ImportResponse importProducts(MultipartFile file);
}