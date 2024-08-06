package com.luckyvicky.woosan.domain.board.controller;

import com.luckyvicky.woosan.domain.board.dto.RemoveDTO;
import com.luckyvicky.woosan.global.util.PageRequestDTO;
import com.luckyvicky.woosan.global.util.PageResponseDTO;
import com.luckyvicky.woosan.domain.board.dto.ReplyDTO;
import com.luckyvicky.woosan.domain.board.service.ReplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/api/replies")
public class ReplyController {

    private final ReplyService replyService;


    /**
     * 댓글 작성
     */
    @PostMapping("/add")
    public ResponseEntity<Void> createReply(@RequestBody ReplyDTO.Request replyDTO){
        replyService.createReply(replyDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
        }

    /**
     * 댓글 조회
     */
    @GetMapping("/{boardId}")
    public ResponseEntity<PageResponseDTO<ReplyDTO.Response>> getReply(@PathVariable Long boardId, PageRequestDTO pageRequestDTO) {
        PageResponseDTO<ReplyDTO.Response> replyDTO  = replyService.getReplies(boardId, pageRequestDTO);
        log.info("#####"+replyDTO.getDtoList());
        return new ResponseEntity<>(replyDTO, HttpStatus.OK);
    }


    /**.
     * 댓글 삭제
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Void>  deleteReply(@RequestBody RemoveDTO removeDTO ) {
        replyService.deleteReply(removeDTO);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

