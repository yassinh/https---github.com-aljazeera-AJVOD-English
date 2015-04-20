package com.brightcove.consulting.alj.models;

import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;


public class PaginationTest {

    /**
     * 1,2,3,4,5,6,7,8,9,10...
     *         ^
     *    (3,4,5,6,7)
     */
    @Test
    public void itShouldReturnTheCorrectStartPageAndEndPagesWhenTheCurrentPageIsWithinTheSpan() {
        Pagination pagination = new Pagination(5, 20, 5);
        List<Page> pages = pagination.getPages();
        assertEquals(3, pages.get(0).getPageNum());
        assertEquals(7, pages.get(pages.size()-1).getPageNum());
        assertEquals("total pages should be the given span size", 5, pages.size());
    }
    
    /**
     * 1,2,3,4,5,6,7,8,9,10...
     *     ^
     *(1,2,3,4,5)
     */
    @Test
    public void itShouldReturnAStartPageOf1WhenThePageIsLessThanTheSpanRange() {
        Pagination pagination = new Pagination(3,20,5);
        List<Page> pages = pagination.getPages();
        assertEquals(1, pages.get(0).getPageNum());
        assertEquals(5, pages.get(pages.size()-1).getPageNum());
    }

    /**
     * 1,2,3,4,5,6,7,8,9,10...
     *     ^
     *(1,2,3,4,5)
     */
    @Test
    public void itShouldEstablishTheCurrentPreviousAndNextPage() {
        Pagination pagination = new Pagination(3,20,5);
        List<Page> pages = pagination.getPages();
        assertTrue(pages.get(2).isCurrentPage());
        assertNotNull(pages.get(2).getNextPage());
        assertNotNull(pages.get(2).getPreviousPage());
    }

    /**
     * 1,2,3,4
     *     ^
     *(1,2,3,4)
     */
    public void itShouldReturnTheFirstAndLastPageWhenTheSpanRangeIsLongerThanTheTotalPages() {
        Pagination pagination = new Pagination(3,4,5);
        List<Page> pages = pagination.getPages();
        assertEquals(1, pages.get(0).getPageNum());
        assertEquals(4, pages.get(pages.size()-1).getPageNum());        
    }
    
    /**
     * 1,2,3,4
     *     ^
     *(1,2,3,4)
     */
    public void theFirstPageShouldNotHaveAPreviousPage() {
        Pagination pagination = new Pagination(3,4,5);
        List<Page> pages = pagination.getPages();
        assertNull(pages.get(0).getPreviousPage());
        assertNotNull(pages.get(0).getNextPage());
    }

    /**
     * 1,2,3,4
     *     ^
     *(1,2,3,4)
     */
    public void theLastPageShouldNotHaveANextPage() {
        Pagination pagination = new Pagination(3,4,5);
        List<Page> pages = pagination.getPages();
        assertNull(pages.get(pages.size()-1).getNextPage());
        assertNotNull(pages.get(pages.size()-1).getPreviousPage());
    }
    
    /**
     * 1,2,3,4,5,6,7,8,9,10
     *                 ^
     *          (6,7,8,9,10)
     */
    @Test
    public void itShouldReturnTheLastPageWhenThePageIsLessThanTheSpanRangeToTheRight() {
        Pagination pagination = new Pagination(9,10,5);
        List<Page> pages = pagination.getPages();
        assertEquals(6, pages.get(0).getPageNum());
        assertEquals(10, pages.get(pages.size()-1).getPageNum());
        
        // even if we're at the very last page, we still have 5 pages
        pagination = new Pagination(10,10,5);
        pages = pagination.getPages();
        assertEquals(6, pages.get(0).getPageNum());
        assertEquals(10, pages.get(pages.size()-1).getPageNum());
    }

    /**
     * 1
     * ^
     *(1)
     */
    @Test
    public void itShouldHandleASinglePage() {
        Pagination pagination = new Pagination(1,1,5);
        List<Page> pages = pagination.getPages();
        assertEquals(1, pages.size());
        assertEquals(1, pages.get(0).getPageNum());
        Page page = pages.get(0);
        assertTrue(page.isCurrentPage());
        assertTrue(page.isLastPage());
    }
}
