package com.project.librarybackend.dto;

import com.project.librarybackend.model.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BrowsingHistoryDTO {
    private String bookTitle;
    private LocalDateTime issueDate;
    private LocalDateTime returnDate;

}
