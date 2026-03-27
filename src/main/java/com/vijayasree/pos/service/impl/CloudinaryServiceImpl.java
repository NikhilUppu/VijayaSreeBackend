package com.vijayasree.pos.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.vijayasree.pos.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadImage(MultipartFile file, String folder) {
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "vijayasreepos/" + folder,
                            "resource_type", "image",
                            "transformation", "q_auto,f_auto,w_400,h_400,c_fill"
                    )
            );
            String url = (String) uploadResult.get("secure_url");
            log.info("Image uploaded to Cloudinary: {}", url);
            return url;
        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary", e);
            throw new RuntimeException("Image upload failed: " + e.getMessage());
        }
    }

    @Override
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;
        try {
            String publicId = extractPublicId(imageUrl);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Image deleted from Cloudinary: {}", publicId);
        } catch (IOException e) {
            log.warn("Failed to delete image from Cloudinary: {}", e.getMessage());
        }
    }

    private String extractPublicId(String imageUrl) {
        String withoutExtension = imageUrl.substring(0, imageUrl.lastIndexOf('.'));
        return withoutExtension.substring(withoutExtension.indexOf("vijayasreepos/"));
    }
}