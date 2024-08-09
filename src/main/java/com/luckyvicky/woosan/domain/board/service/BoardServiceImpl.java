package com.luckyvicky.woosan.domain.board.service;

import com.luckyvicky.woosan.domain.board.dto.*;
import com.luckyvicky.woosan.domain.board.mapper.BoardMapper;
import com.luckyvicky.woosan.domain.board.repository.jpa.BoardRepository;
import com.luckyvicky.woosan.domain.board.util.BoardUtil;
import com.luckyvicky.woosan.domain.fileImg.service.FileImgService;
import com.luckyvicky.woosan.domain.member.repository.jpa.MemberRepository;
import com.luckyvicky.woosan.global.util.CommonUtils;
import com.luckyvicky.woosan.global.util.PageRequestDTO;
import com.luckyvicky.woosan.global.util.PageResponseDTO;
import com.luckyvicky.woosan.global.util.ValidationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.luckyvicky.woosan.global.util.Constants.NOTICE;


@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;
    private final CommonUtils commonUtils;
    private final FileImgService fileImgService;
    private final ElasticsearchBoardService elasticsearchBoardService;
    private final ValidationHelper validationHelper;
    private final BoardMapper boardMapper;
    private final BoardUtil boardUtil;


    /**
     * 게시물 작성
     */
    @Override
    @Transactional
    public void createBoard(BoardDTO boardDTO, List<MultipartFile> images) {
        validationHelper.boardInput(boardDTO); // 입력값 검증
        validationHelper.memberExistAndUpdatePoints(boardDTO.getWriterId(), 10); // 회원 존재 여부 확인 및 포인트 지급
        Long boardId = boardMapper.insertBoard(boardDTO); // 게시글 작성
        handleFileUpload(images, boardId); //파일이 있으면 파일 정보를 버킷 및 db에 저장
    }


    /**
     * 게시물 다건 조회(공지사항1 + 인기글3 + 전체 조회)
     */
    @Override
    @Transactional(readOnly = true)
    public BoardPageResponseDTO getBoardList(PageRequestDTO pageRequestDTO, String categoryName) {
        pageRequestDTO.validate();

        int offset = (pageRequestDTO.getPage() - 1) * pageRequestDTO.getSize();
        int pageSize = pageRequestDTO.getSize();

        // 공지사항 및 인기 게시물 조회
        BoardListDTO notice = getNotice(NOTICE);
        List<BoardListDTO> popularList = getTop3ByViews();

        // 게시물 목록 조회
        List<BoardListDTO> dtoList = boardMapper.findAllByCategoryName(categoryName, offset, pageSize);

        // 총 게시물 수 조회
        long totalCount = boardMapper.countBoardsByCategory(categoryName);

        // 페이지 응답 DTO 생성
        PageResponseDTO<BoardListDTO> boardPage = PageResponseDTO.<BoardListDTO>withAll()
                .dtoList(dtoList)
                .pageRequestDTO(pageRequestDTO)
                .totalCount(totalCount)
                .build();


        return BoardPageResponseDTO.builder()
                .notice(notice)
                .popularList(popularList)
                .boardPage(boardPage)
                .build();
    }


    /**
     * 게시물 단건 조회 - 상세 페이지
     */
    @Override
    @Transactional
    public BoardDetailDTO getBoard(Long id) {
        validationHelper.boardExist(id); // 게시물 존재 여부 검증

        boardUtil.increaseViewCount(id); // 조회수 증가

        BoardDTO boardDTO = boardMapper.findById(id); // 게시물 조회
        validationHelper.alreadyDeletedBoard(boardDTO); // 게시물이 null일 경우 예외 처리

        boardDTO.setFilePathUrl(fileImgService.findFiles("board", id));   // 버킷에서 이미지 url 꺼내고 DTO에 반영
        boardDTO.setWriterProfile(fileImgService.findFiles("member", boardDTO.getWriterId()));   // 버킷에서 이미지 url 꺼내고 DTO에 반영


        // 연관 게시물 검색
        List<SuggestedBoardDTO> suggestedBoards = elasticsearchBoardService.getSuggestedBoards(id, boardDTO.getTitle(), boardDTO.getContent());

        return BoardDetailDTO.builder()
                .boardDTO(boardDTO)
                .suggestedBoards(suggestedBoards)
                .build();
    }

    /**
     * 게시물 단건 조회 (For UPDATE)
     */
    @Override
    public UpdateBoardDTO getBoardForUpdate(Long id) {
        validationHelper.boardExist(id); // 게시물 존재 여부 검증
        UpdateBoardDTO updateBoardDTO = boardMapper.findByIdForUpdate(id);

        updateBoardDTO.setFilePathUrl(fileImgService.findFiles("board", id));
        return updateBoardDTO;
    }


    /**
     * 게시물 수정
     */
    @Override
    @Transactional
    public void updateBoard(BoardDTO boardDTO, List<MultipartFile> images) {
        validationHelper.boardInput(boardDTO); // 입력값 검증
        validationHelper.checkBoardOwnership(boardDTO.getId(), boardDTO.getWriterId()); // 소유자 검증

        boardMapper.updateBoard(boardDTO);
        updateBoardFiles(boardDTO, images, boardDTO.getId());
    }


    /**
     * 게시물 삭제
     */
    @Override
    public void deleteBoard(RemoveDTO removeDTO) {
        validationHelper.boardExist(removeDTO.getId()); // 게시물 존재 여부 검증
        validationHelper.checkBoardOwnership(removeDTO.getId(), removeDTO.getWriterId()); // 소유자 검증
        boardMapper.deleteBoard(removeDTO.getId());
    }


    /**
     * 파일 업로드 처리
     */
    private void handleFileUpload(List<MultipartFile> images, Long boardId) {
        if (images != null) {
            fileImgService.fileUploadMultiple("board", boardId, images);
        }
    }



    /**
     * 최신 공지사항 단건 조회
     */
    @Transactional(readOnly = true)
    public BoardListDTO getNotice(String categoryName) {
        return boardMapper.findFirstNotice(categoryName);
    }


    /**
     * 인기 게시물 상위 3개 조회
     */
    @Transactional(readOnly = true)
    public List<BoardListDTO> getTop3ByViews() {
        return boardMapper.findTop3OrderByViewsDesc();
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


