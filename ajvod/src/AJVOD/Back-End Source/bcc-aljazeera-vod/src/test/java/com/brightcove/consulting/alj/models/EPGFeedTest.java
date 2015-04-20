package com.brightcove.consulting.alj.models;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.json.JSONException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class EPGFeedTest {

	private static EPGFeed feed;

	@BeforeClass
	public static void setUp() throws Exception {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("epg.json");
		ObjectMapper mapper = new ObjectMapper();
		feed = mapper.readValue(is, EPGFeed.class);
		is.close();
	}


	@Before
	public void setup() {
		// 2013-09-09T00:00:00.000Z
		DateTime startDate = new DateTime(new Date(((long)1378684800) * 1000), DateTimeZone.UTC);
	}

	@Test
	public void test() {
//      ...
//		{"timestamp":1378702800,"duration":1800,"title":"News","description":"Live, breaking and in-depth news from Asia, Africa, Europe and the Americas. ","showName":"News","thumbnail":{"330x220":"http:\/\/feeds-custom.aljazeera.net\/en\/images\/programmes\/news_330x220.jpg","440x293":"http:\/\/feeds-custom.aljazeera.net\/en\/images\/programmes\/news_440x293.jpg"}},
//		{"timestamp":1378704600,"duration":1800,"title":"News","description":"Live, breaking and in-depth news from Asia, Africa, Europe and the Americas. ","showName":"News","thumbnail":{"330x220":"http:\/\/feeds-custom.aljazeera.net\/en\/images\/programmes\/news_330x220.jpg","440x293":"http:\/\/feeds-custom.aljazeera.net\/en\/images\/programmes\/news_440x293.jpg"}},
//		{"timestamp":1378706400,"duration":1800,"title":"","description":"A look at some of the finest moments of Sir David Frost's interviews on Al Jazeera, featuring highlights with some of his best known guests.","showName":"Unknown","thumbnail":{"330x220":"http:\/\/feeds-custom.aljazeera.net\/en\/images\/programmes\/default_330x220.jpg","440x293":"http:\/\/feeds-custom.aljazeera.net\/en\/images\/programmes\/default_440x293.jpg"}},
//		{"timestamp":1378708200,"duration":1800,"title":"101 East  : Freedom From Hate","description":"101 East returns to Myanmar to investigate the depth of religious hatred that is plaguing the country and asks if freedom from hate is still possible.","showName":"101 East","thumbnail":{"330x220":"http:\/\/feeds-custom.aljazeera.net\/en\/images\/programmes\/101east_330x220.jpg","440x293":"http:\/\/feeds-custom.aljazeera.net\/en\/images\/programmes\/101east_440x293.jpg"}},
//		{"timestamp":1378710000,"duration":1920,"title":"News","description":"Live, breaking and in-depth news from Asia, Africa, Europe and the Americas. ","showName":"News","thumbnail":{"330x220":"http:\/\/feeds-custom.aljazeera.net\/en\/images\/programmes\/news_330x220.jpg","440x293":"http:\/\/feeds-custom.aljazeera.net\/en\/images\/programmes\/news_440x293.jpg"}},
//      ...

		// start 1378702800
		// end 1378710000
		List<Map> itemsInRange = feed.getItemsInRange((long)1378702800 * 1000, (long)1378710000 * 1000);
		assertEquals(4, itemsInRange.size());
	}


}
