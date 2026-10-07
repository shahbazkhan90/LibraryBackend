package com.project.librarybackend.service;

import com.project.librarybackend.model.Book;
import com.project.librarybackend.repository.BookRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BookService {
    private BookRepository repository;

    public Book addBook(Book book) {
        return repository.save(book);
    }

}
