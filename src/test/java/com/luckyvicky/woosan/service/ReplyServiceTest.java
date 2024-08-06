package com.luckyvicky.woosan.service;

import com.luckyvicky.woosan.global.util.PageRequestDTO;
import com.luckyvicky.woosan.global.util.PageResponseDTO;
import com.luckyvicky.woosan.domain.board.dto.ReplyDTO;
import com.luckyvicky.woosan.domain.board.repository.jpa.BoardRepository;
import com.luckyvicky.woosan.domain.board.repository.jpa.ReplyRepository;
import com.luckyvicky.woosan.domain.board.service.ReplyService;
import com.luckyvicky.woosan.domain.member.repository.jpa.MemberRepository;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Log4j2
public class ReplyServiceTest {

    @Autowired
    private ReplyService replyService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private ReplyRepository replyRepository;

//    /**
//     * 댓글 등록 테스트
//     */
//    @Test
//    public void testRegisterReply() {
//        Long boardId = 357L;
//        Long writerId = 1L;
//        Long parentId = 52L;
//
//        for (int i = 0; i < 10; i++) {
//            ReplyDTO replyDTO = new ReplyDTO();
//            replyDTO.setBoardId(boardId);
//            replyDTO.setWriterId(writerId);
//            replyDTO.setContent("댓글 수 테스트");
//
//            replyService.add(replyDTO, parenㅌtId);
//        }
//    }

    /**
     * 댓글 삭제 테스트
     */
//    @Test
//    public void testDeleteReply() {
//        Long replyId = 64L;  // 테스트에 사용할 댓글 ID
//
//        replyService.deleteReply(replyId);
//        log.info("Deleted Reply ID: " + replyId);
//    }
//

    /**
     * 특정 게시물에 대한 댓글 전체 조회 테스트
     */
    @Test
    @Transactional(readOnly = true)
    public void testGetRepliesByBoardId() {
        Long boardId = 3L;  // 테스트에 사용할 게시물 ID
        PageRequestDTO pageRequestDTO = PageRequestDTO.builder().page(1).size(10).build();

        PageResponseDTO<ReplyDTO> responseDTO = replyService.getReplies(boardId, pageRequestDTO);

        log.info("Total Pages: " + responseDTO.getTotalPage());
        log.info("Total Elements: " + responseDTO.getTotalCount());
        log.info("Current Page Number: " + responseDTO.getCurrent());
        log.info("Page Size: " + pageRequestDTO.getSize());
        log.info("Has Next Page: " + responseDTO.isNext());
        log.info("Has Previous Page: " + responseDTO.isPrev());

        responseDTO.getDtoList().forEach(replyDTO -> {
            log.info("Reply ID: " + replyDTO.getId());
            log.info("Reply Content: " + replyDTO.getContent());
            log.info("Reply Writer ID: " + replyDTO.getWriterId());
            log.info("Reply RegDate: " + replyDTO.getRegDate());
            log.info("Reply Children: " + replyDTO.getChildren().size());
            replyDTO.getChildren().forEach(childReplyDTO -> {
                log.info("    Child Reply ID: " + childReplyDTO.getId());
                log.info("    Child Reply Content: " + childReplyDTO.getContent());
                log.info("    Child Reply Writer ID: " + childReplyDTO.getWriterId());
                log.info("    Child Reply RegDate: " + childReplyDTO.getRegDate());
            });
        });
    }

}