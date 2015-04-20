package com.brightcove.consulting.alj.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

/**
 * Embodies an EPG json feed for a specific channel.
 *
 * @author ssayles
 *
 */
public class EPGFeed extends HashMap<String,Object> {

	private Channel channel;


	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	@SuppressWarnings("rawtypes")
	public void setItems(List<Map> items) {
		this.put("items", items);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Map> getItems() {
		return (List<Map>) this.get("items");
	}

	/**
	 * Get all items that have a start time within the given range, but start
	 * time inclusive and end time exclusive.
	 *
	 * @param startTime
	 * @param endTime
	 */
	@SuppressWarnings("rawtypes")
	public List<Map> getItemsInRange(long startTime, long endTime) {
		return getItemsInRange(startTime, endTime, DateTimeZone.UTC);
	}

	@SuppressWarnings("rawtypes")
	public List<Map> getItemsInRange(long startTime, long endTime, DateTimeZone timeZone) {
		List<Map> itemsInRange = new ArrayList<Map>();
		List<Map> items = getItems();
		if (items != null && !items.isEmpty()) {
			Interval interval = new Interval(startTime, endTime, timeZone);
			for (Map item : items) {
				long timestamp = (Integer)item.get("timestamp");
				 if (interval.contains(timestamp * 1000)) {
					 itemsInRange.add(item);
				 }
			}
		}
		return itemsInRange;
	}

}
