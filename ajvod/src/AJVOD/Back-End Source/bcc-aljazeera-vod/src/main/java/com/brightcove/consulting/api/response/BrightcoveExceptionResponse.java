package com.brightcove.consulting.api.response;

import java.util.ArrayList;
import java.util.List;

import com.brightcove.consulting.api.models.ItemCollection;

public class BrightcoveExceptionResponse<T extends ItemCollection> implements
        BrightcoveResponse<T> {

    @Override
    public long getTotalCount() {
        return 0;
    }

    @Override
    public int getPageNumber() {
        return 0;
    }

    @Override
    public int getPageSize() {
        return 0;
    }

    @Override
    public List<T> getBrightcoveItemCollection() {
        return new ArrayList< T > ( );
    }

}
