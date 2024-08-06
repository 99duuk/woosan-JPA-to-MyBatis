package com.luckyvicky.woosan.domain.fileImg.service;

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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class FileImgServiceImpl implements FileImgService {

    private final FileImgRepository fileImgRepository;
    private final String uploadDir;

    public FileImgServiceImpl(FileImgRepository fileImgRepository, @Value("${file.upload-dir}") String uploadDir) {
        this.fileImgRepository = fileImgRepository;
        this.uploadDir = uploadDir;
    }

    @Override
    public void fileUploadMultiple(String type, Long targetId, List<MultipartFile> files) {
        FileImgUtils.saveFiles(uploadDir, fileImgRepository, type, targetId, files);
    }

    @SlaveDBRequest
    @Override
    public List<String> findFiles(String type, Long targetId) {
        return fileImgRepository.findByTypeAndTargetIdOrderByOrdAsc(type, targetId).stream()
                .map(fileImg -> "/img" + fileImg.getPath() + "/" + fileImg.getUuid() + "_" + fileImg.getFileName())
                .collect(Collectors.toList());
    }

    @Override
    public void targetFilesDelete(String type, Long targetId) {
        List<FileImg> fileImgs = fileImgRepository.findByTypeAndTargetIdOrderByOrdAsc(type, targetId);
        fileImgs.forEach(fileImg -> {
            try {
                Files.deleteIfExists(Paths.get(uploadDir + fileImg.getPath() + "/" + fileImg.getUuid() + "_" + fileImg.getFileName()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        fileImgRepository.deleteByTypeAndTargetId(type, targetId);
    }

    @Override
    public void deleteS3FileByUrl(Long id, String type, String beforeFile) {
        List<FileImg> existFiles = fileImgRepository.findByTypeAndTargetIdOrderByOrdAsc(type, id);
        Map<Long, String> targetMap = new HashMap<>();

        String getFileName = FileImgUtils.getFileNameInUrl(beforeFile);
        FileImgUtils.populateTargetMap(existFiles, targetMap);

        FileImgUtils.deleteFileFromTargetMap(fileImgRepository, uploadDir, targetMap, getFileName, type);
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
