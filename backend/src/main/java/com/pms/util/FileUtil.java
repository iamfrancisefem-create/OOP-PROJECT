package com.pms.util;

import com.pms.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
public class FileUtil {

    @Value("${file.allowed-types}")
    private String allowedTypesString;

    public void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Failed to upload empty file");
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        if (originalFilename.contains("..")) {
            throw new BadRequestException("Cannot upload file with relative path sequence " + originalFilename);
        }

        String extension = getFileExtension(originalFilename);
        List<String> allowedExtensions = Arrays.stream(allowedTypesString.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .toList();

        if (!allowedExtensions.contains(extension.toLowerCase())) {
            throw new BadRequestException("File type not allowed. Allowed types are: " + allowedTypesString);
        }
    }

    public String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
