package com.luckyvicky.woosan.domain.fileImg.service;

import com.amazonaws.services.s3.AmazonS3;
import com.luckyvicky.woosan.domain.fileImg.dto.FileUpdateDTO;
import com.luckyvicky.woosan.domain.fileImg.entity.FileImg;
import com.luckyvicky.woosan.domain.fileImg.repository.FileImgRepository;
import com.luckyvicky.woosan.domain.fileImg.utils.FileImgUtils;
import com.luckyvicky.woosan.global.annotation.SlaveDBRequest;
import com.luckyvicky.woosan.global.util.TargetType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class FileImgServiceImpl implements FileImgService {

    private final FileImgRepository fileImgRepository;
    private final AmazonS3 s3;
    private final String bucketName;

    public FileImgServiceImpl(FileImgRepository fileImgRepository, AmazonS3 s3, @Value("${cloud.ncp.s3.bucket-name}") String bucketName) {
        this.fileImgRepository = fileImgRepository;
        this.s3 = s3;
        this.bucketName = bucketName;
    }

    @Override
    public void fileUploadMultiple(String type, Long targetId, List<MultipartFile> files) {
        FileImgUtils.saveFiles(s3, fileImgRepository, bucketName, type, targetId, files);
    }

    @SlaveDBRequest
    @Override
    public List<String> findFiles(String type, Long targetId) {
        return fileImgRepository.findByTypeAndTargetIdOrderByOrdAsc(type, targetId).stream()
                .map(fileImg -> s3.getUrl(bucketName + fileImg.getPath(), fileImg.getUuid().toString() + "_" + fileImg.getFileName()).toString())
                .collect(Collectors.toList());
    }

    @Override
    public void targetFilesDelete(String type, Long targetId) {
        List<FileImg> fileImgs = fileImgRepository.findByTypeAndTargetIdOrderByOrdAsc(type, targetId);
        fileImgs.forEach(fileImg -> s3.deleteObject(bucketName + fileImg.getPath(), fileImg.getUuid().toString() + "_" + fileImg.getFileName()));
        fileImgRepository.deleteByTypeAndTargetId(type, targetId);
    }

    @Override
    public void deleteS3FileByUrl(Long id, String type, String beforeFile) {
        List<FileImg> existFiles = fileImgRepository.findByTypeAndTargetIdOrderByOrdAsc(type, id);
        Map<Long, String> targetMap = new HashMap<>();

        String getFileName = FileImgUtils.getFileNameInUrl(beforeFile);
        FileImgUtils.populateTargetMap(existFiles, targetMap);

        FileImgUtils.deleteFileFromTargetMap(s3, fileImgRepository, bucketName, targetMap, getFileName, type);
    }

    @Override
    public void updateMainBanner(FileUpdateDTO fileUpdateDTO) {
        if (fileUpdateDTO.getExistFiles() == null) {
            targetFilesDelete(TargetType.ADMIN, TargetType.BANNER);
        } else {
            List<String> beforeFiles = findFiles(TargetType.ADMIN, TargetType.BANNER);
            List<String> afterFiles = fileUpdateDTO.getExistFiles();
            beforeFiles.stream().filter(beforeFile -> !afterFiles.contains(beforeFile)).forEach(beforeFile -> deleteS3FileByUrl(TargetType.BANNER, TargetType.ADMIN, beforeFile));
        }

        if (fileUpdateDTO.getNewFiles() != null) {
            fileUploadMultiple(TargetType.ADMIN, TargetType.BANNER, fileUpdateDTO.getNewFiles());
        }
    }
}
