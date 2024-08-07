package com.luckyvicky.woosan.domain.board.service;

import com.luckyvicky.woosan.domain.board.dto.*;
import com.luckyvicky.woosan.global.annotation.SlaveDBRequest;
import com.luckyvicky.woosan.global.util.PageRequestDTO;
import com.luckyvicky.woosan.global.util.PageResponseDTO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Transactional
public interface CsService {


    @Transactional(readOnly = true)
    PageResponseDTO<BoardListDTO> getNoticePage(PageRequestDTO pageRequestDTO);

    @Transactional
    BoardDTO getNotice(Long id);

    @Transactional
    List<BoardListDTO> getNotices();

    @Transactional
    List<BoardListDTO> getBestBoard();
}


