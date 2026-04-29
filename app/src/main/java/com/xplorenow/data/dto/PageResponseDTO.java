package com.xplorenow.data.dto;
import java.util.List;

public class PageResponseDTO<T> {

    private List<T> content;
    private int totalElements;
    private int totalPages;
    private int number;
    private int size;
    private boolean first;
    private boolean last;

    public List<T> getContent() { return content; }
    public int getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public int getNumber() { return number; }
    public int getSize() { return size; }
    public boolean isFirst() { return first; }
    public boolean isLast() { return last; }
}