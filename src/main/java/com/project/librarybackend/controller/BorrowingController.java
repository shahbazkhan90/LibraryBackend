package com.project.librarybackend.controller;

import com.project.librarybackend.dto.BrowsingHistoryDTO;
import com.project.librarybackend.service.BorrowingService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrowings")
@AllArgsConstructor
public class BorrowingController {

    private final BorrowingService borrowingService;

    @PostMapping("/issue")
    public ResponseEntity<String > issueBook(@RequestParam Integer bookId,@RequestParam Integer memberId){
        borrowingService.issueBook(bookId,memberId);
        return ResponseEntity.ok("Book issued Successfully");
    }

    @PostMapping("/return")
    public ResponseEntity<String> returnBook(@RequestParam Integer bookId,@RequestParam Integer memberId){
        borrowingService.returnBook(bookId,memberId);
        return ResponseEntity.ok("Book return Successfully");
    }

    @GetMapping("/history")
    public ResponseEntity<List<BrowsingHistoryDTO>> memberHistory(@RequestParam Integer memberId){
        return ResponseEntity.ok(borrowingService.getMemberHistory(memberId));
    }

}
