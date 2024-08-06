package com.luckyvicky.woosan.domain.fileImg.utils;

import com.luckyvicky.woosan.domain.fileImg.entity.FileImg;
import com.luckyvicky.woosan.domain.fileImg.repository.FileImgRepository;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FileImgUtils {

    public static void uploadMultipartFile(String uploadDir, String objectName, MultipartFile file, String type) throws IOException {
        Path savePath = Paths.get(uploadDir, type);
        if (!Files.exists(savePath)) {
            Files.createDirectories(savePath);
        }
        Path filePath = savePath.resolve(objectName);
        Files.copy(file.getInputStream(), filePath);
    }

    public static String getFileNameInUrl(String url) {
        try {
            String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8.toString());
            return decodedUrl.substring(decodedUrl.lastIndexOf('/') + 1);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void saveFiles(String uploadDir, FileImgRepository fileImgRepository, String type, Long targetId, List<MultipartFile> files) {
        for (int i = 0; i < files.size(); i++) {
            String uuid = UUID.randomUUID().toString();
            String uniqueSaveName = uuid + "_" + files.get(i).getOriginalFilename();

            FileImg fileImg = FileImg.builder()
                    .type(type)
                    .targetId(targetId)
                    .uuid(uuid)
                    .fileName(files.get(i).getOriginalFilename())
                    .ord(i)
                    .path("/" + type)
                    .build();
            fileImgRepository.save(fileImg);

            try {
                uploadMultipartFile(uploadDir, uniqueSaveName, files.get(i), type);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to upload file: " + files.get(i), e);
            }
        }
    }

    public static void populateTargetMap(List<FileImg> existFiles, Map<Long, String> targetMap) {
        for (FileImg fileImg : existFiles) {
            try {
                String encodedFileName = URLEncoder.encode(fileImg.getFileName(), StandardCharsets.UTF_8.toString()).replace("+", "%20");
                targetMap.put(fileImg.getId(), fileImg.getUuid() + "_" + encodedFileName);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public static void deleteFileFromTargetMap(FileImgRepository fileImgRepository, String uploadDir, Map<Long, String> targetMap, String getFileName, String type) {
        for (Map.Entry<Long, String> entry : targetMap.entrySet()) {
            try {
                String decodedEntryValue = URLDecoder.decode(entry.getValue(), StandardCharsets.UTF_8.toString());
                if (decodedEntryValue.equals(getFileName)) {
                    FileImg targetImg = fileImgRepository.findById(entry.getKey()).orElse(null);
                    if (targetImg != null) {
                        fileImgRepository.deleteById(entry.getKey());
                        Files.deleteIfExists(Paths.get(uploadDir, type, targetImg.getUuid() + "_" + targetImg.getFileName()));
                    }
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
