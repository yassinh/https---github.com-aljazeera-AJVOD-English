package com.brightcove.consulting.alj.utils;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.FastArrayList;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;

import com.brightcove.consulting.alj.models.NavigationItem;
import com.brightcove.consulting.alj.models.Show;
import com.brightcove.consulting.alj.models.Topic;

public class RelatedNavSearchPredicate implements Predicate {

    // TODO: naive implementation to be removed later
    private static List<String> stopList = Arrays.asList(new String[] {"the", "but", "and", "i", "a", "of"});

    private String searchTerm;
    private List<String> terms;

    @SuppressWarnings("rawtypes")
    private List evaluated = new FastArrayList();

    public RelatedNavSearchPredicate(String searchTerm) {
        this.searchTerm = searchTerm;

        // filtered list of terms
        terms = Arrays.asList(searchTerm.split("/\\s+/"));

        CollectionUtils.transform(terms, new Transformer() {
            public Object transform(Object input) {
                return ((String)input).toLowerCase();
            }
        });
    }

    @SuppressWarnings("unchecked")
    public boolean evaluate(Object object) {
        NavigationItem item = (NavigationItem) object;

        // we may end up crawling the same items again (at this point)
        // because we put the same objects into different child collections
        if (evaluated.contains(item)) {
            return false;
        }
        evaluated.add(item);

        if (item instanceof Show || item instanceof Topic) {
            if (item.getName() != null) {
                String name = item.getName().toLowerCase();
                if (terms.indexOf(name) > -1) {
                    return true;
                }
                if (StringUtils.contains(searchTerm.toLowerCase(), name)) {
                    return true;
                }
                for (String term : terms) {
                    if (!stopList.contains(term) && StringUtils.contains(name, term)) {
                        return true;
                    }
                }
                
            }
        }
        return false;
    }
};

