package com.luckyvicky.woosan.domain.admin.service;

import com.luckyvicky.woosan.domain.board.dto.BoardDTO;
import com.luckyvicky.woosan.domain.board.dto.RemoveDTO;
import com.luckyvicky.woosan.domain.board.mapper.BoardMapper;
import com.luckyvicky.woosan.domain.board.repository.jpa.BoardRepository;
import com.luckyvicky.woosan.domain.fileImg.service.FileImgService;
import com.luckyvicky.woosan.global.util.TargetType;
import com.luckyvicky.woosan.global.util.ValidationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

    private final BoardRepository boardRepository;
    private final FileImgService fileImgService;
    private final ValidationHelper validationHelper;
    private final BoardMapper boardMapper;


    /**
     * 공지사항 작성
     */
    @Override
    public Long createNotice(BoardDTO boardDTO, List<MultipartFile> images) {
        validationHelper.noticeInput(boardDTO); // 입력값 검증


        //파일이 있으면 파일 정보를 버킷 및 db에 저장합니다.
        if (images != null) {
            fileImgService.fileUploadMultiple(TargetType.BOARD, boardDTO.getId(), images);
        }

        validationHelper.checkAdmin(boardDTO.getWriterId()); // 관리자 여부 검증
        boardMapper.insertBoard(boardDTO);

        return boardDTO.getId();
    }

    /**
     * 게시물 수정
     */
    @Override
    public void updateNoitce(BoardDTO boardDTO, List<MultipartFile> images) {
        validationHelper.noticeInput(boardDTO); // 입력값 검증
        validationHelper.checkAdmin(boardDTO.getWriterId()); // 관리자 여부 검증

        boardMapper.updateBoard(boardDTO);
        updateBoardFiles(boardDTO, images, boardDTO.getId());
    }


    /**
     * 게시물 삭제
     */
    @Override
    public void deleteNotice(RemoveDTO removeDTO) {
        validationHelper.checkBoardNotDeleted(removeDTO.getId()); // 게시물 삭제 여부 확인
        validationHelper.checkAdmin(removeDTO.getWriterId()); // 관리자 여부 검증
        boardMapper.deleteBoard(removeDTO.getId());
    }


    /**
     * 게시물 파일 정보 갱신
     */
    private void updateBoardFiles(BoardDTO boardDTO, List<MultipartFile> images, Long boardId) {
        if (boardDTO.getFilePathUrl() == null) {
            fileImgService.targetFilesDelete("board", boardId);
        } else {
            List<String> beforeFiles = fileImgService.findFiles("board", boardId);
            List<String> afterFiles = boardDTO.getFilePathUrl();

            for (String beforeFile : beforeFiles) {
                if (!afterFiles.contains(beforeFile)) {
                    fileImgService.deleteS3FileByUrl(boardId, "board", beforeFile);
                }
            }
        }

        if (images != null) {
            fileImgService.fileUploadMultiple("board", boardId, images);
        }
    }

}
