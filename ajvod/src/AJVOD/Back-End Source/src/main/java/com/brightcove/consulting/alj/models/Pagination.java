package com.brightcove.consulting.alj.models;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.ListUtils;

/**
 * A view model used to represent state for pagination.
 *
 * @author ssayles
 */
public class Pagination {

    public int getTotalPages() {
        return totalPages;
    }

    private int pageNum;
    private int totalPages;

    // number of pages to span either side of the page
    private Integer span = 5;

    private List<Page> pages = new ArrayList<Page>(); 

    public Pagination(int pageNum, int totalPages, Integer span) {
        if (pageNum == 0 || totalPages == 0) {
            return;
        }
        this.pageNum = pageNum;
        this.totalPages = totalPages;
        if (span != null) {
            this.span  = span;
        }
        init();
    }
    
    public int getPageNum() {
        return pageNum;
    }

    public List<Page> getPages() {
        return pages;
    }

    @SuppressWarnings("unchecked")
    private void init() {
        
        int preSpan = span / 2;
        // start page is either 1 or current page - span
        int startPage = (pageNum - preSpan);
        startPage = startPage > 0 ? startPage : 1;

        int endPage = startPage + span - 1;
        if (endPage > totalPages) {
            int diff = (endPage - totalPages);
            startPage = (startPage - diff) > 0 ? (startPage - diff) : 1;
            endPage = endPage > totalPages ? totalPages : endPage;
        }

        Page prevPage = null;
        for (int i = startPage; i <= endPage; i++) {
            Page page = new Page(i, i == pageNum, i == totalPages);
            if (prevPage != null) {
                prevPage.setNextPage(page);
                page.setPreviousPage(prevPage);
            }
            pages.add(page);
            prevPage = page;
        }
        pages = ListUtils.unmodifiableList(pages);
    }

}
