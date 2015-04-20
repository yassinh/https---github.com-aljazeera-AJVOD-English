package com.brightcove.consulting.alj.models;

/**
 * A view model that represents states for a single page used in pagination.
 * 
 * @author ssayles
 */
public class Page {

    private boolean currentPage;
    private boolean lastPage;
    private int pageNum;
    private Page previousPage;
    private Page nextPage;

    public Page(int pageNum, boolean currentPage, boolean lastPage) {
        this.pageNum = pageNum;
        this.currentPage = currentPage;
        this.lastPage = lastPage;
    }

    public int getPageNum() {
        return pageNum;
    }

    public boolean isCurrentPage() {
        return currentPage;
    }

    public boolean isLastPage() {
        return lastPage;
    }

    public boolean getHasNext() {
        return !lastPage;
    }

    public boolean getHasPrevious() {
        return pageNum > 1;
    }

    public void setCurrentPage(boolean currentPage) {
        this.currentPage = currentPage;
    }

    /**
     * Returns the previous page this page is associated with in the established
     * range.
     * @return The previous page or null.
     */
    public Page getPreviousPage() {
        return previousPage;
    }
    
    public void setPreviousPage(Page previousPage) {
        this.previousPage = previousPage;
    }

    /**
     * Returns the next page this page is associated with in the established
     * range.
     * @return The next page or null.
     */
    public Page getNextPage() {
        return nextPage;
    }

    public void setNextPage(Page nextPage) {
        this.nextPage = nextPage;
    }

    @Override
    public String toString() {
        String text = "{page:" + pageNum +
                      ",currentPage:" + currentPage +
                      ",lastPage:" + lastPage + "}";
        return text;
    };
}
