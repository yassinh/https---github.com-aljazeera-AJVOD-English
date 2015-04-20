package com.brightcove.consulting.alj.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.SetUtils;
import org.apache.commons.collections.functors.InstanceofPredicate;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.brightcove.consulting.alj.models.Channel;
import com.brightcove.consulting.alj.models.NavigationItem;
import com.brightcove.consulting.api.models.BrightcoveLogic;
import com.brightcove.consulting.api.models.BrightcoveVideo;
import com.brightcove.consulting.api.query.FindVideosBySearchFields;
import com.brightcove.consulting.api.request.SearchApiPageRequest;
import com.brightcove.consulting.api.response.BrightcoveResponse;

@Component
public class NavUtils {

    @Autowired
    private String readToken;

    public NavUtils() {
    }


    /**
     * Queries the read api to see if there are any videos associated with the
     * given NavigationItem. This will also use any parent channel if it exists.
     *
     * @param item 
     * @return <code>true</code> if the item has videos.
     */
    @Cacheable("hasVideos")
    public boolean hasVideos(NavigationItem item) {
        String customFieldName = item.getCustomFieldName();
        String customFieldValue = item.getCustomFieldValue();
        if (customFieldName == null || customFieldValue == null) {
            return false;
        }

        SearchFieldsBuilder fieldsBuilder = new SearchFieldsBuilder();
        Channel channel = null;
        if (item instanceof Channel) {
            channel = (Channel) item;
            fieldsBuilder.add(channel, BrightcoveLogic.ALL);
        } else {
            channel = findAncestor(item, Channel.class);
            fieldsBuilder.add(channel, BrightcoveLogic.ALL);

            fieldsBuilder.add(item, BrightcoveLogic.ALL);
        }

        SearchApiPageRequest searchRequest = new SearchApiPageRequest(0, 1, fieldsBuilder.getFields());
        FindVideosBySearchFields search = new FindVideosBySearchFields(readToken);
        BrightcoveResponse<BrightcoveVideo> result = search.query(searchRequest);

        return (result.getBrightcoveItemCollection().size() > 0);
    }

    /**
     * Searches the descendant children of the given parent navigation item based
     * on a given search path.  This method is a more optimized version of the
     * general breadthSearch in that it only looks through children hierarchies
     * of the given types.  For example, given the a navigation tree of:
     * <pre>
     * <code>
     *    channel(1)
     *       children -> ShowGenre[]
     *         showGenre(1)
     *            children -> Show[]
     *              show1
     *              show2
     *         showGenre(2)
     *            children -> Show[]
     *              show3
     *              show4
     * </code>
     * </pre>
     * 
     * The following call would specifically only search the hiearchy of children
     * of show genres to shows from the parent channel and return show3:
     * 
     * <pre>
     * <code>
     *     Predicate predicate = new Predicate() {
     *         public boolean evaluate(Object obj) {
     *             ((NavigationItem)obj).getKey() == 'show3';
     *         }
     *     }
     *     
     *     NavUtils.findByPath(channel, predicate, ShowGenre.class, Show.class)
     * </code>
     * </pre>
     * 
     * 
     * 
     * @param parent
     * @param predicate
     * @param path An array of NavigationItem class objects indicating the child
     *   hierarchy to search through.
     * @return The found navigation item or null.
     * @throws UnsupportedEncodingException 
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T extends NavigationItem>T findByPath(NavigationItem parent, String showId, Class...path) {
        Collection<? extends NavigationItem> childGroup = findByPath(parent, path); 
        try {
			showId = URLEncoder.encode(showId, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			return null;
		}

        if (childGroup == null) {
            return null;
        }

        // if we reach here, then we found the children we're looking for
        // now test
        for (NavigationItem item : childGroup) {
        	
        	if(item.getKey().equalsIgnoreCase(showId)){
                return (T) item;
            }
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public static Collection<? extends NavigationItem> findByPath(NavigationItem parent, Class<? extends NavigationItem>...path) {
        Collection<? extends NavigationItem> childGroup = null;

        // first level
        childGroup = parent.getChildren(path[0]).values();
        if (childGroup.isEmpty()) {
            return null;
        }
        if (path.length == 1) {
            return childGroup;
        }

        // now on to the next level
        // to find all the children matching the given type
        for (int i = 1; i < path.length; i++) {
            Class clazz = path[i];
            childGroup = childrenOf(childGroup, clazz);
            if (childGroup.isEmpty()) {
                return null;
            }
        }
        return childGroup;
    }
    /**
     * Finds the first ancestor that is of the given type.
     * @param type
     * @return A navigation item or null.
     */
    @SuppressWarnings("unchecked")
    public static <T extends NavigationItem>T findAncestor(NavigationItem child, Class<T> type) {
        return (T)ancestorSearch(child, new InstanceofPredicate(type));
    }

    /**
     * Searches all descendants of the given parent and returns all items that
     * match the given predicate.
     *
     * @param parent
     * @param predicate
     * @return A set of navigation items.
     */
    @SuppressWarnings("unchecked")
    public static Set<? extends NavigationItem> findDescendants(NavigationItem parent, Predicate predicate) {
        Collection<Map<String, NavigationItem>> children = parent.getChildren().values();
        if (children.isEmpty()) {
            return SetUtils.EMPTY_SET;
        }
        Set<NavigationItem> selected = new HashSet<NavigationItem>();
        crawl(children, predicate, selected);
        return selected;
    }

    /**
     * Finds the first descendant matching the given predicate.
     *
     * @param parent
     * @param predicate
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends NavigationItem>T findDescendant(NavigationItem parent, Predicate predicate) {
        Collection<Map<String, NavigationItem>> children = parent.getChildren().values();
        if (children.isEmpty()) {
            return null;
        }
        return (T)depthSearch(children, predicate);
    }

    public static Set<? extends NavigationItem> sortByName(Collection<? extends NavigationItem> items) {
        Set<NavigationItem> set = new TreeSet<NavigationItem>(new NavigationItemNameComparator());
        set.addAll(items);
        return set;
    }

    public static Set<? extends NavigationItem> breadthSearch(Collection<? extends NavigationItem> items, Predicate predicate, int limit) {
        Set<NavigationItem> results = new HashSet<NavigationItem>();
        breadthCrawl(items, predicate, limit, results);
        return results;
    }


    private static void breadthCrawl(Collection<? extends NavigationItem> itemsToTraverse, Predicate predicate, int limit, Set<NavigationItem> selectedItems) {
        for (NavigationItem item : itemsToTraverse) {
            if (predicate.evaluate(item)) {
                selectedItems.add(item);
                if (limit > 0 && selectedItems.size() >= limit) {
                    return;
                }
            }
        }
        // get all the children
        Set<NavigationItem> children = new HashSet<NavigationItem>();
        for (NavigationItem item : itemsToTraverse) {
            Collection<Map<String, NavigationItem>> values = item.getChildren().values();
            for (Map<String, NavigationItem> map : values) {
                children.addAll(map.values());
            }
        }
        if (!children.isEmpty()) {
            breadthCrawl(children, predicate, limit, selectedItems);
        }
    }
    

    @SuppressWarnings("unchecked")
    private static <T extends NavigationItem>T depthSearch(Collection<Map<String,NavigationItem>> itemsToTraverse, Predicate predicate) {
        for (Map<String,NavigationItem> itemMap: itemsToTraverse) {
            Collection<NavigationItem> navItems = itemMap.values();
            for (NavigationItem baseNavigationItem : navItems) {
                if (predicate.evaluate(baseNavigationItem)) {
                    return (T) baseNavigationItem; 
                }
                if (!baseNavigationItem.getChildren().values().isEmpty()) {
                    T item = (T)depthSearch(baseNavigationItem.getChildren().values(), predicate);
                    if (item != null) {
                    	return item;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Crawls the descendants of the given collection of navigation items and
     * collects all items that match the given predicate.
     * @param itemsToTraverse
     * @param predicate
     * @param selectedItems All nav items matching the predicate.
     */
    private static void crawl(Collection<Map<String,NavigationItem>> itemsToTraverse, Predicate predicate, Set<NavigationItem> selectedItems) {
        for (Map<String,NavigationItem> itemMap: itemsToTraverse) {
            Collection<NavigationItem> navItems = itemMap.values();
            for (NavigationItem baseNavigationItem : navItems) {
                if (predicate == null) {
                    selectedItems.add(baseNavigationItem);
                } else if (predicate.evaluate(baseNavigationItem)) {
                    selectedItems.add(baseNavigationItem);
                }
                if (!baseNavigationItem.getChildren().values().isEmpty()) {
                    crawl(baseNavigationItem.getChildren().values(), predicate, selectedItems);
                }
            }
        }
    }

    /**
     * Returns the first parent ancestor that the given Predicate evaluates true.
     * @return A parent ancestor NavigationItem or null.
     */
    @SuppressWarnings("unchecked")
    private static <T extends NavigationItem>T ancestorSearch(NavigationItem child, Predicate predicate) {
        if (child.getParent() != null) {
            if (predicate.evaluate(child.getParent())) {
                return (T) child.getParent();
            }
            return (T)ancestorSearch(child.getParent(), predicate);
        }
        return null;
    }


    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Collection<? extends NavigationItem> childrenOf(Collection<? extends NavigationItem>group, Class type) {
        Collection selected = new ArrayList(group.size() * 10);
        for (NavigationItem item : group) {
            Map<String, ? extends NavigationItem> children = item.getChildren(type);
            if (!children.isEmpty()) {
                selected.addAll(children.values());
            }
        }
        return selected;
    }

    // search predicates............


    public static class NavigationItemNameComparator implements Comparator<NavigationItem> {
        public int compare(NavigationItem o1, NavigationItem o2) {
            if (o1 == null && o2 == null) {
                return 0;
            } else if (o1 == null) {
                return -1;
            } else if (o2 == null) {
                return 1;
            } else {
                return o1.getName().compareTo(o2.getName());
            }
        }
        
    }
}
