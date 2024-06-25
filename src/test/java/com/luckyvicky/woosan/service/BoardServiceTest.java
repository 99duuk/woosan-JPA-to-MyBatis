package com.luckyvicky.woosan.service;

import com.luckyvicky.woosan.domain.board.dto.BoardDTO;
import com.luckyvicky.woosan.domain.board.entity.Board;
import com.luckyvicky.woosan.domain.board.projection.IBoard;
import com.luckyvicky.woosan.domain.board.projection.IBoardMember;
import com.luckyvicky.woosan.domain.board.service.BoardService;
import com.luckyvicky.woosan.domain.member.dto.WriterDTO;
import com.luckyvicky.woosan.domain.member.entity.Member;
import com.luckyvicky.woosan.domain.member.repository.MemberRepository;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@Log4j2
public class BoardServiceTest {

    @Autowired
    private BoardService boardService;

    @Autowired
    private MemberRepository memberRepository;

    /**
     * <Test>
     * id, title, content
     * board 단일 인터페이스 프로젝션
     */
//    @Test
//    @Transactional
//    public void getOneTestI(){
//        List<IBoard> projectedBoards = boardService.findAllProjectedBoard();
//        log.info("$$$$$$$$$$$$$$$$$$$$$$$$$$$4");
//
//
//        // 로깅을 통해 결과 확인
//        for (IBoard board : projectedBoards) {
//            System.out.println("Projected Board - Title: " + board.getTitle() + ", Content: " + board.getContent());
//        }
//
//
//    }

    /**
     * id, title, content, writerId, nickname
     * board-member 연관관계 인터페이스 프로젝션
     * 게시물 단건 조회
     */
    @Test
    public void testFindProjectedBoardMemberById() {
        Long boardId = 2L; // 존재하는 보드 ID로 설정해야 함

        Optional<IBoardMember> boardMemberOptional = boardService.findProjectedBoardMemberById(boardId);



        if (boardMemberOptional.isPresent()) {
            IBoardMember boardMember = boardMemberOptional.get();
            System.out.println("Board Member - Id: " + boardMember.getId() +
                    ", Title: " + boardMember.getTitle() +
                    ", Content: " + boardMember.getContent() +
                    ", RegDate: " + boardMember.getRegDate() +
                    ", Views: " + boardMember.getViews() +
                    ", IsDeleted: " + boardMember.getIsDeleted() +
                    ", CategoryName: " + boardMember.getCategoryName() +
                    ", WriterId: " + boardMember.getWriter().getId() +
                    ", WriterNickname: " + boardMember.getWriter().getNickname());
        } else {
            System.out.println("Board member not found.");
        }
    }


    /**
     * id, title, content, writerId, nickname
     * 연관관계 인터페이스 프로젝션
     * 게시물 전체 조회
     */
    @Test
    @Transactional(readOnly = true)
    public void testFindProjectedBoardMembers() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        Page<IBoardMember> page = boardService.findAllProjectedBoardMember(pageable);


        log.info("Total Pages: " + page.getTotalPages());
        log.info("Total Elements: " + page.getTotalElements());
        log.info("Current Page Number: " + page.getNumber());
        log.info("Page Size: " + page.getSize());
        log.info("Has Next Page: " + page.hasNext());
        log.info("Has Previous Page: " + page.hasPrevious());

        log.info(" ");

        page.getContent().forEach(boardMember -> {
            log.info("Board Member ID: " + boardMember.getId());
            log.info("Board Member Title: " + boardMember.getTitle());
            log.info("Board Member Content: " + boardMember.getContent());
            log.info("Board Member RegDate: " + boardMember.getRegDate());
            log.info("Board Member Views: " + boardMember.getViews());
            log.info("Board Member IsDeleted: " + boardMember.getIsDeleted());
            log.info("Board Member CategoryName: " + boardMember.getCategoryName());
            log.info("Board Member WriterId: " + boardMember.getWriter().getId());
            log.info("Board Member Writer Nickname: " + boardMember.getWriter().getNickname());
        });
    }



//    <---------------------------------------------------------------------->


    /**
     * id, title, content, writerId, nickname
     * 연관관계 인터페이스 프로젝션
     * 특정 카테고리 이름의 게시물 전체 조회
     */
    @Test
    @Transactional(readOnly = true)
    public void testFindProjectedBoardMembersByCategoryName() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        String categoryName = "스프링"; // 테스트에 사용할 카테고리 이름

        Page<IBoardMember> page = boardService.findAllProjectedBoardMemberByCategoryName(categoryName, pageable);

        log.info("Total Pages: " + page.getTotalPages());
        log.info("Total Elements: " + page.getTotalElements());
        log.info("Current Page Number: " + page.getNumber());
        log.info("Page Size: " + page.getSize());
        log.info("Has Next Page: " + page.hasNext());
        log.info("Has Previous Page: " + page.hasPrevious());

        log.info(" ");

        page.getContent().forEach(boardMember -> {
            log.info("Board Member ID: " + boardMember.getId());
            log.info("Board Member Title: " + boardMember.getTitle());
            log.info("Board Member Content: " + boardMember.getContent());
            log.info("Board Member RegDate: " + boardMember.getRegDate());
            log.info("Board Member Views: " + boardMember.getViews());
            log.info("Board Member IsDeleted: " + boardMember.getIsDeleted());
            log.info("Board Member CategoryName: " + boardMember.getCategoryName());
            log.info("Board Member WriterId: " + boardMember.getWriter().getId());
            log.info("Board Member Writer Nickname: " + boardMember.getWriter().getNickname());
        });
    }

    @Test
    void boardInesrtTest() throws IOException {

        String filePath1 = "C:\\Users\\ho976\\IdeaSnapshots\\OneDrive\\사진\\스크린샷\\민지.png";
        String filePath2 = "C:\\Users\\ho976\\IdeaSnapshots\\OneDrive\\사진\\스크린샷\\karina.png";

        MultipartFile multipartFile1 = new MockMultipartFile("file1", "민지.png", "image/png", new FileInputStream(filePath1));
        MultipartFile multipartFile2 = new MockMultipartFile("file2", "karina.png", "image/png", new FileInputStream(filePath2));
        List<MultipartFile> files = Arrays.asList(multipartFile1, multipartFile2);

        WriterDTO writerDTO = new WriterDTO();
        writerDTO.setId(1L);
        writerDTO.setNickname("asdf");

        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setWriter(writerDTO);
        boardDTO.setTitle("제목 test");
        boardDTO.setContent("내용 test");
        boardDTO.setCategoryName("꿀팁");
        boardDTO.setImages(files);

        Long board = boardService.add(boardDTO);


    }



}
