package com.example.librarymanagementsystem.dto;

public class CategoryStatsDto {
    private String name;
    private long booksCount;
    private long loansCount;
    private long returnsCount;

    // Constructors
    public CategoryStatsDto() {}

    public CategoryStatsDto(String name, long booksCount, long loansCount, long returnsCount) {
        this.name = name;
        this.booksCount = booksCount;
        this.loansCount = loansCount;
        this.returnsCount = returnsCount;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getBooksCount() {
        return booksCount;
    }

    public void setBooksCount(long booksCount) {
        this.booksCount = booksCount;
    }

    public long getLoansCount() {
        return loansCount;
    }

    public void setLoansCount(long loansCount) {
        this.loansCount = loansCount;
    }

    public long getReturnsCount() {
        return returnsCount;
    }

    public void setReturnsCount(long returnsCount) {
        this.returnsCount = returnsCount;
    }

    // toString method for debugging
    @Override
    public String toString() {
        return "CategoryStatsDto{" +
                "name='" + name + '\'' +
                ", booksCount=" + booksCount +
                ", loansCount=" + loansCount +
                ", returnsCount=" + returnsCount +
                '}';
    }
}