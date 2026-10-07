package com.project.librarybackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class BorrowingRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer borrowingId;
    private LocalDateTime issueDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book bookRelation;
    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member memberRelation;

}
