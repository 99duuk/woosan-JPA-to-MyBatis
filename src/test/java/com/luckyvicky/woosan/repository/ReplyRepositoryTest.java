package com.luckyvicky.woosan.repository;

import com.luckyvicky.woosan.domain.board.entity.Board;
import com.luckyvicky.woosan.domain.board.entity.Reply;
import com.luckyvicky.woosan.domain.board.repository.jpa.BoardRepository;
import com.luckyvicky.woosan.domain.board.repository.jpa.ReplyRepository;
import com.luckyvicky.woosan.domain.member.entity.Member;
import com.luckyvicky.woosan.domain.member.repository.jpa.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Commit;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Log4j2
@SpringBootTest
public class ReplyRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    BoardRepository boardRepository;

    @Autowired
    ReplyRepository replyRepository;




    @Test
    public void addTest() {
        Board testBoard = boardRepository.findById(3L).orElseThrow();
        Member testMember = memberRepository.findById(1L).orElseThrow();

        for (int i = 0; i < 13; i++) {
            Reply reply = Reply.builder()
                    .board(testBoard)
                    .writer(testMember)
                    .content("사건은 계속되아 오예이" + i)
                    .regDate(LocalDateTime.now())
                    .parentId(null)
                    .build();

            Reply saveReply = replyRepository.save(reply);
//            log.info("댓글 생성" + saveReply);
            log.info("댓글 생성: id={}, content={}", saveReply.getId(), saveReply.getContent());
        }
    }

    @Test
    public void addTest2() {
        Board testBoard = boardRepository.findById(3L).orElseThrow();
        Member testMember = memberRepository.findById(1L).orElseThrow();

        for (int i = 0; i < 2; i++) {
            Reply reply = Reply.builder()
                    .board(testBoard)
                    .writer(testMember)
                    .content("ㅇㅇ")
                    .regDate(LocalDateTime.now())
                    .parentId(16L)
                    .build();

            Reply saveReply = replyRepository.save(reply);
            log.info("대댓글 생성: parentId={}, id={}, content={}",saveReply.getParentId(), saveReply.getId(),  saveReply.getContent());
        }
    }

    @Test
    @Transactional
    public void readTest() {
        Optional<Reply> reply = replyRepository.findById(6L);
        log.info("Reply: " + reply);
    }

    @Commit
    @Test
    @Transactional
    public void updateTest() {
        Reply reply = replyRepository.findById(6L).orElseThrow();
        reply.changeContent("Updated Content");

        replyRepository.save(reply);
        log.info("Reply updated: " + reply);
    }


    @Commit
    @Transactional
    @Test
    public void deleteTest() {
        replyRepository.deleteById(3L);
        log.info("Reply with ID 1 deleted");
    }

//왜 두개만 먼저 땡겨와?????
    @Test
    @Transactional
    public void listAllTest() {
        List<Reply> replies = replyRepository.findAll();
        replies.forEach(reply -> log.info("Reply: " + reply));
    }


    @Test
    @Transactional
    public void testPaging(){
        Pageable pageable = PageRequest.of(0, 3, Sort.by("id").ascending());
        Page<Reply> result = replyRepository.findAll(pageable);

        log.info("Total Pages: " + result.getTotalPages());
        log.info("Total Elements: " + result.getTotalElements());
        log.info("Current Page Number: " + result.getNumber());
        log.info("Page Size: " + result.getSize());
        log.info("Has Next Page: " + result.hasNext());
        log.info("Has Previous Page: " + result.hasPrevious());

        result.getContent().forEach(reply -> log.info("Reply: " + reply));
    }

    @Test
    @Transactional
    public void testPagingParentAndChildReplies() {
        int parentPageSize = 3; // 부모 댓글 페이지 크기
        int childPageSize = 2;  // 자식 댓글 페이지 크기

        // 부모 댓글을 페이징하여 가져옵니다.
        Pageable parentPageable = PageRequest.of(0, parentPageSize, Sort.by("id").ascending());
        Page<Reply> parentReplies = replyRepository.findByParentIdIsNull(parentPageable);

        log.info("Total Parent Pages: 4" + parentReplies.getTotalPages());
        log.info("Total Parent Elements: " + parentReplies.getTotalElements());
        log.info("Current Parent Page Number: " + parentReplies.getNumber());
        log.info("Parent Page Size: " + parentReplies.getSize());
        log.info("Has Next Parent Page: " + parentReplies.hasNext());
        log.info("Has Previous Parent Page: " + parentReplies.hasPrevious());

        parentReplies.getContent().forEach(parentReply -> {
            log.info("Parent Reply: " + parentReply);

            // 각 부모 댓글에 대해 자식 댓글을 페이징하여 가져옵니다.
            Pageable childPageable = PageRequest.of(0, childPageSize, Sort.by("id").ascending());
            Page<Reply> childReplies = replyRepository.findByParentId(parentReply.getId(), childPageable);

            log.info("Total Child Pages for Parent ID " + parentReply.getId() + ": " + childReplies.getTotalPages());
            log.info("Total Child Elements for Parent ID " + parentReply.getId() + ": " + childReplies.getTotalElements());
            log.info("Current Child Page Number for Parent ID " + parentReply.getId() + ": " + childReplies.getNumber());
            log.info("Child Page Size for Parent ID " + parentReply.getId() + ": " + childReplies.getSize());
            log.info("Has Next Child Page for Parent ID " + parentReply.getId() + ": " + childReplies.hasNext());
            log.info("Has Previous Child Page for Parent ID " + parentReply.getId() + ": " + childReplies.hasPrevious());

            childReplies.getContent().forEach(childReply -> log.info("Child Reply: " + childReply));
        });
    }
    }

