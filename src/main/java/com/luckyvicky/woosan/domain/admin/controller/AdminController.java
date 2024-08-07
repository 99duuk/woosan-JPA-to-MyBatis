package com.luckyvicky.woosan.domain.admin.controller;

import com.luckyvicky.woosan.domain.admin.service.AdminService;
import com.luckyvicky.woosan.domain.board.dto.BoardDTO;
import com.luckyvicky.woosan.domain.board.dto.BoardListDTO;
import com.luckyvicky.woosan.domain.board.dto.RemoveDTO;
import com.luckyvicky.woosan.domain.board.dto.UpdateBoardDTO;
import com.luckyvicky.woosan.domain.board.service.BoardService;
import com.luckyvicky.woosan.domain.board.service.CsService;
import com.luckyvicky.woosan.domain.fileImg.dto.FileUpdateDTO;
import com.luckyvicky.woosan.domain.fileImg.service.FileImgService;
import com.luckyvicky.woosan.domain.report.dto.ReportDTO;
import com.luckyvicky.woosan.domain.report.dto.TargetDTO;
import com.luckyvicky.woosan.domain.report.service.ReportService;
import com.luckyvicky.woosan.global.util.PageRequestDTO;
import com.luckyvicky.woosan.global.util.PageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final BoardService boardService;
    private final CsService csService;
    private final ReportService reportService;
    private final FileImgService fileImgService;


    /**
     * 게시물 작성
     */
    @PostMapping("/notices")
    public ResponseEntity<Void> createNotice(@RequestPart("boardDTO") BoardDTO boardDTO,
                                             @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        adminService.createNotice(boardDTO, images);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    /**
     * 공지사항 다건 조회 (cs)
     */
    @GetMapping("/notices")
    public ResponseEntity<PageResponseDTO<BoardListDTO>> getNoticePage(PageRequestDTO pageRequestDTO) {
        PageResponseDTO<BoardListDTO> responseDTO = csService.getNoticePage(pageRequestDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }


    /**
     * 게시물 수정 페이지 조회
     */
    @GetMapping("notices/{id}/modify")
    public ResponseEntity<UpdateBoardDTO> getBoardForModification(@PathVariable Long id) {
        UpdateBoardDTO updateBoardDTO = boardService.getBoardForUpdate(id);
        return new ResponseEntity<>(updateBoardDTO, HttpStatus.OK);
    }

    /**
     * 게시물 수정
     */
    @PatchMapping("notices")
    public ResponseEntity<Void> updateBoard(@RequestPart("boardDTO") BoardDTO boardDTO,
                                            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        adminService.updateNoitce(boardDTO, images);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * 게시물 삭제
     */
    @PatchMapping("notices/delete")
    public ResponseEntity<Void> deleteBoard(@RequestBody RemoveDTO removeDTO) {
        adminService.deleteNotice(removeDTO);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    /**
     * 신고 목록
     */
    @GetMapping("/report")
    public ResponseEntity<PageResponseDTO<ReportDTO>> getReportList(PageRequestDTO pageRequestDTO) {
        PageResponseDTO<ReportDTO> responseDTO = reportService.reportList(pageRequestDTO);
        return ResponseEntity.ok(responseDTO);
    }


    /**
     * 신고 상세보기
     */
    @GetMapping("/report/{id}")
    public ResponseEntity<ReportDTO> getReport(@PathVariable Long id) {
        ReportDTO result = reportService.getReport(id);
        return ResponseEntity.ok(result);
    }


    /**
     * 신고 확인
     */
    @PostMapping("/report/{id}")
    public ResponseEntity<Long> checkReport(@PathVariable Long id) {
        Long reportId = reportService.checkReport(id);
        return ResponseEntity.ok(reportId);
    }


    /**
     * 신고 대상으로 이동 (게시글 = 해당 게시글, 댓글 = 댓글이 작성된 게시글)
     */
    @GetMapping("/report/target")
    public ResponseEntity<TargetDTO> goToTarget(@RequestParam Long id) {
        TargetDTO boardId = reportService.goToTarget(id);
        return ResponseEntity.ok(boardId);
    }


    /**
     * 메인 화면 배너사진 목록
     */
    @GetMapping("/myBanner")
    public ResponseEntity<List<String>> adminBanner() {
        List<String> bannerList = fileImgService.findFiles("admin", 0L);
        return ResponseEntity.ok(bannerList);
    }


    /**
     * 메인 화면 배너사진 업데이트
     * */
    @PostMapping("/myBanner/modify")
    public ResponseEntity<String> updateBanner(FileUpdateDTO fileUpdateDTO){
        fileImgService.updateMainBanner(fileUpdateDTO);
        return ResponseEntity.ok("banner 수정 완료");
    }

}