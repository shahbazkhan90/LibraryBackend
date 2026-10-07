package com.project.librarybackend.service;

import com.project.librarybackend.dto.BrowsingHistoryDTO;
import com.project.librarybackend.model.Book;
import com.project.librarybackend.model.BorrowingRecord;
import com.project.librarybackend.model.Member;
import com.project.librarybackend.repository.BookRepository;
import com.project.librarybackend.repository.BorrowingRecordRepository;
import com.project.librarybackend.repository.MemberRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class BorrowingService {
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final BorrowingRecordRepository borrowingRecordRepository;


    @Transactional
    public void issueBook(Integer bookId,Integer memberId){
        Book book = bookRepository.findById(bookId).orElseThrow();
        Member member = memberRepository.findById(memberId).orElseThrow();

        Integer availableCopies = book.getAvailableCopies();
        if(availableCopies<=0){
            throw new RuntimeException("Book not available");
        }

        BorrowingRecord borrowingRecord = new BorrowingRecord();
        borrowingRecord.setBookRelation(book);
        borrowingRecord.setMemberRelation(member);
        borrowingRecord.setIssueDate(LocalDateTime.now());
        borrowingRecord.setDueDate(LocalDateTime.now().plusDays(14));
        book.setAvailableCopies(availableCopies-1);
        //As the hibernate will autmatically update the state changed objects
//        bookRepository.save(book);
        borrowingRecordRepository.save(borrowingRecord);
    }

    @Transactional
    public void returnBook(Integer bookId, Integer memberId) {
        BorrowingRecord record= borrowingRecordRepository.findFirstByBookRelation_BookIdAndMemberRelation_MemberIdAndReturnDateIsNull(bookId,memberId).orElseThrow(()->new RuntimeException("No record found"));
        record.setReturnDate(LocalDateTime.now());
//        Book book = bookRepository.findById(record.getBookRelation().getBookId()).orElseThrow();
        Book book = record.getBookRelation();
        book.setAvailableCopies(book.getAvailableCopies()+1);
//        borrowingRecordRepository.save(record);
//        bookRepository.save(book);
        //Commented these lines because of hibernate auto updating state changed objects

    }

    public List<BrowsingHistoryDTO> getMemberHistory(Integer memberId){
        List<BorrowingRecord> members = borrowingRecordRepository.findByMemberRelation_MemberId(memberId);
        List<BrowsingHistoryDTO> dto = members.stream().map(record ->{
            BrowsingHistoryDTO history = new BrowsingHistoryDTO();
            history.setBookTitle(record.getBookRelation().getTitle());
            history.setIssueDate(record.getIssueDate());
            history.setReturnDate(record.getReturnDate());
            return history;
        }).toList();
        return dto;
    }
}
