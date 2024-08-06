package com.luckyvicky.woosan.domain.fileImg.utils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.luckyvicky.woosan.domain.fileImg.entity.FileImg;
import com.luckyvicky.woosan.domain.fileImg.repository.FileImgRepository;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public class FileImgUtils {

    public static void uploadMultipartFile(AmazonS3 s3, String bucketName, String objectName, MultipartFile file, String type) throws IOException {
        String savePath = bucketName + "/" + type;
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            s3.putObject(savePath, objectName, file.getInputStream(), metadata);
            makeFilePublic(s3, objectName, savePath);
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
    }

    public static void makeFilePublic(AmazonS3 s3, String objectName, String savePath) {
        try {
            s3.setObjectAcl(savePath, objectName, CannedAccessControlList.PublicRead);
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
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

    public static String extractKeyFromUrl(String fileUrl, String type) {
        String parseType = type + "/";
        String prefix = "woosan/" + parseType;
        int startIndex = fileUrl.indexOf(prefix) + prefix.length() - parseType.length();
        if (startIndex == -1) {
            throw new IllegalArgumentException("URL does not contain the expected prefix: " + prefix);
        }
        return fileUrl.substring(startIndex);
    }

    public static void saveFiles(AmazonS3 s3, FileImgRepository fileImgRepository, String bucketName, String type, Long targetId, List<MultipartFile> files) {
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
                uploadMultipartFile(s3, bucketName, uniqueSaveName, files.get(i), type);
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

    public static void deleteFileFromTargetMap(AmazonS3 s3, FileImgRepository fileImgRepository, String bucketName, Map<Long, String> targetMap, String getFileName, String type) {
        for (Map.Entry<Long, String> entry : targetMap.entrySet()) {
            try {
                String decodedEntryValue = URLDecoder.decode(entry.getValue(), StandardCharsets.UTF_8.toString());
                if (decodedEntryValue.equals(getFileName)) {
                    FileImg targetImg = fileImgRepository.findById(entry.getKey()).orElse(null);
                    if (targetImg != null) {
                        fileImgRepository.deleteById(entry.getKey());
                        s3.deleteObject(bucketName, type + "/" + targetImg.getUuid() + "_" + targetImg.getFileName());
                    }
                    break;
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }
}
