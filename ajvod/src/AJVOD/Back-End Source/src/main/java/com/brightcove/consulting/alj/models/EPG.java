package com.brightcove.consulting.alj.models;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import com.brightcove.consulting.alj.utils.NavUtils;

/**
 * An EPG represents EPG feed data in a format that accommodates front end
 * requirements.
 *
 * @author ssayles
 */
public class EPG {

	private EPGFeed feed;

	/** starting time rounded down to the nearest half hour. */
	private DateTime startTime;

	/** Defines how many hours from the start time feed data will be included. */
	private int hours;

	private int days;

	private Locale locale = Locale.getDefault();

	private Map<LocalDate, List<ShowJson>> schedule = new LinkedHashMap<LocalDate, List<ShowJson>>();

	private Map<LocalDate, List<Interval>> dayIntervals = new LinkedHashMap<LocalDate, List<Interval>>();

	private ResourceBundle terms = null;

	@SuppressWarnings("unchecked")
	private Map<Channel, Map<String,String>> urlCache = MapUtils.lazyMap(new HashMap<Channel, Map<String,String>>(), new Factory() {
		public Object create() {
			return new HashMap<String,String>();
		}
	});

	public EPG(EPGFeed feed) {
		this.feed = feed;
	}

	public EPG setStartTime(DateTime startTime) {
		this.startTime = startTime;
		return this;
	}

	public EPG setNumberOfHours(int hours) {
		this.hours = hours;
		return this;
	}

	public EPG setNumberOfDays(int days) {
		this.days = days;
		return this;
	}

	public EPG setLocale(Locale locale) {
		this.locale = locale;
		return this;
	}


	@SuppressWarnings("rawtypes")
	public List<ShowJson> getLineup(int episodes) {
		List<Map> items = feed.getItems();
		List<ShowJson> shows = new ArrayList<ShowJson>();
		for (Map item : items) {
			DateTime startDate = getStartDate(item);
			DateTime endDate = getEndDate(startDate, item);
			startDate = startDate.withZone(startTime.getZone());
			if (startDate.equals(startTime) || startDate.isAfter(startTime.getMillis()) || endDate.isAfter(startTime.getMillis())) {
				shows.add(new ShowJson(this, item, startTime.getZone()));
				if (shows.size() >= episodes) {
					break;
				}
			}
		}
		return shows;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void load() {

		loadIntervals();

		List<Map> items = feed.getItems();


		LocalDate currentDay = null;
		// last show processed so we can keep track of it
		Map previousItem = null;

		for (Map item : items) {
			DateTime startDate = getStartDate(item);
			DateTime endDate = getEndDate(startDate, item);

			// convert to target time zone
			startDate = startDate.withZone(this.startTime.getZone());
			endDate = endDate.withZone(this.startTime.getZone());

			// the day the show starts on
			LocalDate startDay = startDate.toLocalDate();

			// see if the day is in the set of intervals we need
			List<Interval> intervals = dayIntervals.get(startDay);

			if (intervals != null) {

				// track if we're starting a new day
				boolean newDay = false;
				if (!startDay.equals(currentDay)) {
					newDay = true;
					currentDay = startDay;
				}

				for (Interval interval : intervals) {
					// if we're starting a new day, check to see if the previous
					// show ends within our interval.  If so, add it
					if (newDay && previousItem != null) {
						DateTime prevStartDate = getStartDate(previousItem);
						DateTime prevEndDate = getEndDate(prevStartDate, previousItem);
						prevEndDate = prevEndDate.withZone(this.startTime.getZone());
						if (interval.contains(prevEndDate.getMillis() - 1)) {
							// this is from the cached feed data so it's important that
							// we don't update the existing show item
							// we're also making a duplicate entry in the schedule
							Map clonedItem = cloneItem(previousItem);
							clonedItem.put("startFrom", interval.getStartMillis());
							addToSchedule(interval, clonedItem);
						}
						newDay = false; // no longer need to check for previous overlapping interval the next time.
					}
					if (interval.contains(startDate.getMillis())) {
						addToSchedule(interval, item);
						break;
					}
				}
			}
			previousItem = item;
		}
	}

	public Schedule getSchedule() {
		return new Schedule(this, schedule);
	}
	
	public Locale getLocale() {
		return this.locale;
	}

	private ResourceBundle getBundle() {
		if (terms == null) {
			if (getLocale() == null) {
				terms = ResourceBundle.getBundle("localization/terms");
			} else {
				terms = ResourceBundle.getBundle("localization/terms", getLocale());
			}
		}
		return terms;
	}

	@SuppressWarnings("rawtypes")
	private DateTime getStartDate(Map item) {
		long startTime = (Integer)item.get("timestamp");
		DateTime startDate = new DateTime((long)startTime * 1000, DateTimeZone.UTC);
		return startDate;
	}

	@SuppressWarnings("rawtypes")
	private DateTime getEndDate(DateTime startDate, Map item) {
		int duration = (Integer)item.get("duration");
		DateTime endDate = startDate.plusSeconds(duration);
		return endDate;
	}

	private String dateFormat(String name) {
		ResourceBundle bundle = getBundle();
		if (bundle != null) {
			return bundle.getString("epg.dateformat." + name);
		}
		return StringUtils.EMPTY;
	}

	/**
	 * Sets up the grid of days and intervals for the requested time range.
	 */
	private void loadIntervals() {
		// for the number of days, setup intervals for each hour
		for (int i = 0; i < this.days; i++) {
			DateTime dayTime = startTime.plusDays(i);
			LocalDate day = dayTime.toLocalDate();

			List<Interval> intervals = new ArrayList<Interval>();

			// setup the schedule as we go through
			List<ShowJson> shows = new ArrayList<ShowJson>();
			schedule.put(day, shows);

			// for each hour, create an interval
			for (int h = 0; h < this.hours; h++) {
				DateTime startHour = dayTime.plusHours(h);
				// if the start time is not on the same day, then we went past
				// the current day so stop.
				if (!startHour.toLocalDate().equals(day)) {
					break;
				}
				DateTime endHour = dayTime.plusHours(h+1);
				Interval interval = new Interval(startHour, endHour);
				intervals.add(interval);
			}
			dayIntervals.put(day, intervals);
		}
	}

	/*
	 * TODO: convert to target timezone
	 */
	@SuppressWarnings("rawtypes")
	private void addToSchedule(Interval interval, Map item) {
		LocalDate day = interval.getStart().toLocalDate();
		List<ShowJson> shows = schedule.get(day);

		shows.add(new ShowJson(this, item, this.startTime.getZone()));
	}

	protected Map<LocalDate, List<Interval>> getDailyIntervals() {
		return dayIntervals;
	}

	@SuppressWarnings("rawtypes")
	private Map cloneItem(Map item) {
	    ObjectOutputStream oos = null;
	    ObjectInputStream ois = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(item);
            oos.flush();
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ois = new ObjectInputStream(bis);
            Map clone = (Map) ois.readObject();
            return clone;
        } catch (IOException e) {
//            logger.error("IOException occurred while cloning item " + item);
        } catch (ClassNotFoundException e) {
//            logger.error("IOException occurred while cloning item " + item);
        } finally {
            if (oos != null) {
                try {oos.close();} catch (IOException e) {}
            }
            if (ois != null) {
                try {ois.close();} catch (IOException e) {}
            }
        }
        return null;
	}

	public static class Schedule {

		private Map<LocalDate, List<ShowJson>> data;
		private EPG epg;
		private ArrayList<Map<String, String>> times;

		public Schedule(EPG epg, Map<LocalDate, List<ShowJson>> data) {
			this.data = data;
			this.epg = epg;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public List<Map<String, String>> getTimes() {
			if (this.times != null) {
				return this.times;
			}

			if (data == null || data.isEmpty()) {
				return ListUtils.EMPTY_LIST;
			}
			Map<LocalDate, List<Interval>> dayIntervals = epg.getDailyIntervals();
			Entry<LocalDate, List<Interval>> entry = dayIntervals.entrySet().iterator().next();
			List<Interval> intervals = entry.getValue();

			this.times = new ArrayList<Map<String,String>>();
			for (Interval interval : intervals) {
				String time = interval.getStart().toString(
							epg.dateFormat("interval"), epg.getLocale());
				Map map = new HashMap();
				map.put("time", time);
				times.add(map);
			}

			return times;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public List<Map> getDays() {
			List<Map> days = new ArrayList<Map>();

			Set<LocalDate> keys = data.keySet();

			for (LocalDate date : keys) {
				Map day = new HashMap();
				day.put("date", date.toString(
							epg.dateFormat("day"), epg.getLocale()));
				day.put("shows", data.get(date));
				days.add(day);
			}

			return days;
		}
	}


    public static class ShowJson {
		@SuppressWarnings("rawtypes")
		private Map data;
		private DateTimeZone targetZone;
		private Channel channel;
		private EPG epg;
		private Integer duration;

		@SuppressWarnings("rawtypes")
		public ShowJson(EPG epg, Map data, DateTimeZone targetZone) {
			this.epg = epg;
			this.channel = epg.feed.getChannel();
			this.data = data;
			this.targetZone = targetZone;
		}

		public String getDescription() {
			return (String) data.get("description");
		}
		public String getStartTime() {
			return startTime().toString();
		}
		public int getDuration() {
			return (Integer) data.get("duration");
		}

		public int getMins() {
			if (this.duration != null) {
				return duration;
			}
			Integer duration = (Integer) data.get("duration");
			if (duration == null) {
				return 0;
			}

			// if the actual start time of the slot we're in begins later,
			// make our mins reflect that.
			if (data.get("startFrom") != null) {
				long startMillis = (Long) data.get("startFrom");
				DateTime startDate = epg.getStartDate(data);
				DateTime endDate = epg.getEndDate(startDate, data);
				endDate = endDate.withZone(targetZone);
				int difference = (int) (endDate.getMillis() - startMillis - 1);
				duration = difference / 1000;
			}

			duration = (int) Math.round((double)duration / 60);
			this.duration = duration;

			return duration;
		}
		
		public String getTimeDuration() {
			Integer duration = (Integer) data.get("duration");
			if (duration == null) {
				return "0";
			}

			// if the actual start time of the slot we're in begins later,
			// make our mins reflect that.
			if (data.get("startFrom") != null) {
				long startMillis = (Long) data.get("startFrom");
				DateTime startDate = epg.getStartDate(data);
				DateTime endDate = epg.getEndDate(startDate, data);
				endDate = endDate.withZone(targetZone);
				int difference = (int) (endDate.getMillis() - startMillis - 1);
				duration = difference / 1000;
			}

			double timeDuration = Math.round((double)duration / 60);
			
			int minutes = (int) Math.floor(timeDuration);
			int seconds = (int) (timeDuration - minutes) * (60/100);
			
			String minutesValue = String.valueOf(minutes);
			if(minutes < 10)
				minutesValue = "0" + minutesValue;
			
			String secondsValue = String.valueOf(seconds);
			if(seconds < 10)
				secondsValue = "0" + secondsValue;

			return minutesValue + ":" + secondsValue;
		}
		
		@SuppressWarnings("unchecked")
		public Map<String, Object> getThumbnails() {
			return (Map<String, Object>) data.get("thumbnail");
		}

		@SuppressWarnings("unchecked")
		public String getThumbnailUrl() {
			Map<String, Object> thumbnails = (Map<String, Object>) data.get("thumbnail");
			if(thumbnails == null)
				return null;
			
			String thumbnailKey = (String)thumbnails.keySet().toArray()[0];
			
			return (String) thumbnails.get(thumbnailKey);
		}
		
		public String getShowUrl() {
			Map<String, String> channelUrls = epg.urlCache.get(channel);
			String showName = getShow();
			String url = channelUrls.get(showName);
			if (!StringUtils.isEmpty(url)) {
				return (String) url;
			}

			if (StringUtils.equalsIgnoreCase("News", showName)) {
				url = "/channels/" + channel.getKey() + "/news";
			} else {
				Show show = NavUtils.findDescendant(channel, new ShowPredicate(showName));
				if (show != null) {
					url = "/channels/"+ channel.getKey() +"/shows/" + show.getKey();
					channelUrls.put(showName, url);
				} else {
					// the show doesn't exist in the config, for now
					// let's just refer to an unknown url
					url = "/channels/"+ channel.getKey() +"/shows/" + showName;
					channelUrls.put("showName", url);
				}
			}
			
			return (String) url;
		}

		public String getTitle() {
			return (String) data.get("title");
		}

		public String getShow() {
			return (String) data.get("showName");
		}

		public long getDate() {
			 return startTime().toDate().getTime();
		}

		public String getDateString() {
			SimpleDateFormat sdf = new SimpleDateFormat(epg.dateFormat("watchlive.timeline"), epg.getLocale());
			String dateTime = sdf.format(new Date(getDate()));
			
		    return dateTime;
		}
		
		public String getSlot() {
			DateTime startTime = startTime();
			String start = startTime.toString(
					epg.dateFormat("slot"), epg.getLocale());
			String end = startTime.plusMinutes(
					getMins()).toString(epg.dateFormat("slot"), epg.getLocale());
			return start + " - " + end;
		}

		private DateTime startTime() {
			int startTime = (Integer)data.get("timestamp");
			DateTime dateTime = new DateTime((long)startTime * 1000, DateTimeZone.UTC);
			dateTime = dateTime.withZone(targetZone);
			return dateTime;
		}
		
	}

    private static class ShowPredicate implements Predicate {
    	private String name;

		public ShowPredicate(String name) {
			this.name = name;
    	}

		public boolean evaluate(Object object) {
			if (object instanceof Show) {
				Show show = (Show)object;
				if (StringUtils.equalsIgnoreCase(this.name, show.getName())) {
					return true;
				}
			}
			return false;
		}
    	
    }

}
