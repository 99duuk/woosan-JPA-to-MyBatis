package com.luckyvicky.woosan.domain.board.service;

import com.luckyvicky.woosan.domain.board.dto.*;
import com.luckyvicky.woosan.domain.board.mapper.BoardMapper;
import com.luckyvicky.woosan.domain.board.repository.jpa.BoardRepository;
import com.luckyvicky.woosan.domain.board.util.BoardUtil;
import com.luckyvicky.woosan.domain.fileImg.service.FileImgService;
import com.luckyvicky.woosan.domain.member.mybatisMapper.MemberMyBatisMapper;
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

import java.util.List;

import static com.luckyvicky.woosan.global.util.Constants.NOTICE;


@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class CsServiceImpl implements CsService {

    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;
    private final CommonUtils commonUtils;
    private final FileImgService fileImgService;
    private final ValidationHelper validationHelper;
    private final MemberMyBatisMapper memberMyBatisMapper;
    private final BoardMapper boardMapper;
    private final BoardUtil boardUtil;

    /**
     * 공지사항 다건 조회 (cs page)
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<BoardListDTO> getNoticePage(PageRequestDTO pageRequestDTO) {
        pageRequestDTO.validate();

        int offset = (pageRequestDTO.getPage() - 1) * pageRequestDTO.getSize();
        int pageSize = pageRequestDTO.getSize();

        List<BoardListDTO> dtoList = boardMapper.findAllByCategoryName(NOTICE, offset, pageSize);
        // 총 게시물 수 조회
        long totalCount = boardMapper.countBoardsByCategory(NOTICE);

        // 페이지 응답 DTO 생성
        return  PageResponseDTO.<BoardListDTO>withAll()
                .dtoList(dtoList)
                .pageRequestDTO(pageRequestDTO)
                .totalCount(totalCount)
                .build();
    }


    /**
     * 공지사항 단건 조회 - 상세 페이지
     */
    @Override
    @Transactional
    public BoardDTO getNotice(Long id) {
        validationHelper.boardExist(id); // 게시물 존재 여부 검증

        boardUtil.increaseViewCount(id); // 조회수 증가

        BoardDTO boardDTO = boardMapper.findNoticeById(id, NOTICE); // 게시물 조회
        validationHelper.alreadyDeletedBoard(boardDTO); // 게시물이 null일 경우 예외 처리

        boardDTO.setFilePathUrl(fileImgService.findFiles("board", id));   // 버킷에서 이미지 url 꺼내고 DTO에 반영
        boardDTO.setWriterProfile(fileImgService.findFiles("member", boardDTO.getWriterId()));   // 버킷에서 이미지 url 꺼내고 DTO에 반영

        return boardDTO;
    }

    /**
     * 공지사항 게시물 10개 조회
     */
    @Override
    @Transactional
    public List<BoardListDTO> getNotices() {
        return boardMapper.findTop5Notices(NOTICE);
    }

    /**
     * 인기글 게시물 10개 조회 (추천순)
     */
    @Override
    @Transactional
    public List<BoardListDTO> getBestBoard() {
        return boardMapper.findTop10OrderByLikesCountDesc();
    }
}


