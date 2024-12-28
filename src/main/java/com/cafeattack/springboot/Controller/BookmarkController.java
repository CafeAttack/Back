package com.cafeattack.springboot.Controller;

import com.cafeattack.springboot.Domain.Dto.request.*;
import com.cafeattack.springboot.Service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class BookmarkController {
    private final BookmarkService bookmarkService;

    @GetMapping("/member/{memberid}/bookmark")
    public ResponseEntity bookmark_page(@PathVariable("memberid") int memberid) {
        return bookmarkService.bookmark_Page(memberid);
    }

    @GetMapping("/member/{memberid}/add-bookmark")
    public ResponseEntity getPageforEdit(@PathVariable("memberid") int memberid
            , @RequestBody CafeIdDTO cafeIdDTO) {
        return bookmarkService.getPageforEdit(memberid, cafeIdDTO.getCafeId());
    }

    @PostMapping("/member/{memberid}/add-bookmark")
    public ResponseEntity addBookmark(@PathVariable("memberid") int memberid
            , @RequestBody addbookmarkDto AddbookmarkDto) {
        return bookmarkService.addBookmark(memberid, AddbookmarkDto);
    }

    @PostMapping("/member/{memberid}/add-group")
    public ResponseEntity addGroup(@PathVariable("memberid") int memberid,
                                   @RequestBody addGroupDto AddGroupDto) {
        return bookmarkService.addGroup(memberid, AddGroupDto);
    }

    @DeleteMapping("/member/{memberid}/delete-group")
    public ResponseEntity deleteGroup(@PathVariable("memberid") int memberid,
                                      @RequestBody deleteGroupDto DeleteGroupDto) {
        return bookmarkService.deleteGroup(memberid,DeleteGroupDto);
    }
}
