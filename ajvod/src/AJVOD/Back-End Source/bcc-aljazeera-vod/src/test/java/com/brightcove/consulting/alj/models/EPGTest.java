package com.brightcove.consulting.alj.models;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.brightcove.consulting.alj.models.EPG.Schedule;
import com.brightcove.consulting.alj.models.EPG.ShowJson;


public class EPGTest {

	private static EPGFeed feed;

	private EPG epg;

	@BeforeClass
	public static void setUp() throws Exception {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("epg.json");
		ObjectMapper mapper = new ObjectMapper();
		feed = mapper.readValue(is, EPGFeed.class);
		is.close();
	}


	@Before
	public void setup() {
		epg = new EPG(feed);
		epg.setNumberOfHours(25);
		epg.setNumberOfDays(7);
		// 2013-09-09T00:00:00.000Z
		DateTime startDate = new DateTime(new Date(((long)1378684800) * 1000), DateTimeZone.UTC);
		epg.setStartTime(startDate.withZone(DateTimeZone.UTC));
		epg.load();
	}

	@Test
	public void testDailyIntervalsShouldOnlyContainIntervalsForThatDay() {
		Map<LocalDate, List<Interval>> dailyIntervals = epg.getDailyIntervals();
		Set<LocalDate> days = dailyIntervals.keySet();
		for (LocalDate day : days) {
			List<Interval> intervals = dailyIntervals.get(day);
			// there should only be 24 hours of intervals
			assertEquals(24, intervals.size());
			// start time is at
			for (Interval interval : intervals) {
				assertEquals(day, interval.getStart().toLocalDate());
			}
		}
	}

	@Test
	public void testDailyIntervalsShouldOnlyContainTheNumberOfDaysWithinTheGivenRange() {
		Map<LocalDate, List<Interval>> dailyIntervals = epg.getDailyIntervals();
		assertEquals(7, dailyIntervals.size());
	}
	
	@Test
	public void testTheScheduleShouldOnlyContainDaysInTheGivenRange() {
		assertEquals(7, epg.getSchedule().getDays().size());
	}

	@Test
	public void testGetLineupShouldReturnTheNumberOfShowsFromTheCurrentStartTime() throws JSONException {
		// 2013-09-09T00:00:00.000Z
		DateTime startDate = new DateTime(new Date(((long)1378684800) * 1000), DateTimeZone.UTC);

		epg = new EPG(feed);
		epg.setStartTime(startDate);
		List<ShowJson> shows = epg.getLineup(3);
		
		assertEquals(3, shows.size());
		for (ShowJson show : shows) {
			long date = show.getDate();
			DateTime startTime = new DateTime(date);
			DateTime endTime = startTime.plusMinutes(show.getMins());
			assertTrue((startTime.isAfter(startDate.getMillis()) || endTime.isAfter(startDate.getMillis())));
		}
	}

	@Test
	public void testTheScheduleShouldBeAdjustedForTheRequestedTimezone() throws JSONException {
		// 2013-09-09T00:00:00.000Z
		DateTime startDate = new DateTime(new Date(((long)1378684800) * 1000), DateTimeZone.UTC);
		// add a day, then convert to GTM-4
		startDate = startDate.plusDays(1);
		// 2013-09-09T20:00:00.000-04:00  (8pm)
		DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(-4, 0);
		startDate = startDate.withZone(zone);
		startDate = startDate.toLocalDate().toDateTimeAtStartOfDay(zone);
		epg = new EPG(feed);
		epg.setStartTime(startDate)
		   .setNumberOfDays(8)
		   .setNumberOfHours(24)
		   .load();

//		Schedule schedule = epg.getSchedule();
//		List<Map> days = schedule.getDays();
//		for (Map day : days) {
//			System.out.println(new JSONObject(day).toString(4));
//		}
	}

	public void testFoo() throws JSONException {
//		Schedule schedule = epg.getSchedule();
	}

}
