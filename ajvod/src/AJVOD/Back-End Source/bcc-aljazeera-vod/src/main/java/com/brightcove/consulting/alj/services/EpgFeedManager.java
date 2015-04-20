package com.brightcove.consulting.alj.services;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.brightcove.consulting.alj.models.Channel;
import com.brightcove.consulting.alj.models.EPGFeed;
import com.brightcove.consulting.alj.models.NavRepository;

/**
 * Handles fetching EPG data based on channel configuration.
 * 
 * @author ssayles
 */
public class EpgFeedManager {

	private static final Logger logger = LoggerFactory.getLogger(EpgFeedManager.class);


	@Autowired
	private NavRepository navRepository;

	int numWorkerThreads;


	/** Map of channel ids to EPG data. */
	private Map<String,EPGFeed> feeds = new ConcurrentHashMap<String,EPGFeed>();

	private ExecutorService workerPool = null;
	

	public EpgFeedManager() {
	}

	@PostConstruct
	public void init() {
		workerPool = Executors.newFixedThreadPool(numWorkerThreads);
		pullFeeds();
	}

	@PreDestroy
	public void shutdown() {
		if (workerPool != null) {
			workerPool.shutdown();
		}
	}

	public int getNumWorkerThreads() {
		return numWorkerThreads;
	}

	public void setNumWorkerThreads(int numWorkerThreads) {
		this.numWorkerThreads = numWorkerThreads;
	}

	// called from timer
	@SuppressWarnings("unchecked")
	public void pullFeeds() {
		Map<String, Channel> channelMap = navRepository.getChannels();

		// if a channel was removed, then remove the feed data
		for (String key : feeds.keySet()) {
			if (!channelMap.containsKey(key)) {
				feeds.remove(key);
			}
		}

		// select only those channels with epg feed url
		Collection<Channel> channels = channelMap.values();
		channels = CollectionUtils.select(channels, new Predicate() {
			public boolean evaluate(Object object) {
				Channel channel = (Channel) object;
				return StringUtils.isNotEmpty(channel.getEpgFeedUrl());
			}
		});

		if (channels.isEmpty()) {
			return;
		} else {
		}

		// now for each channel feed we have, submit a job
		for (Channel channel : channels) {
			FeedJob feedJob = new FeedJob(channel);
			workerPool.submit(feedJob);
		}
	}


	public EPGFeed getFeed(String channelId) {
		return this.feeds.get(channelId);
	}

	@SuppressWarnings("rawtypes")
	public void putFeed(Channel channel, EPGFeed newFeed) {

		// keep the items from the previous day, from the start of the previous
		// day to the start of the new feed
		EPGFeed existingFeed = feeds.get(channel.getKey());
		if (existingFeed != null) {

			// get the start of the new feed.
			List<Map> items = newFeed.getItems();
			if (!items.isEmpty()) {
				Map item = items.get(0);
				long startTime = (long)((Integer) item.get("timestamp"));

				// the first time stamp of the new feed is effectively the end date of the previous feed.
				DateTime endDate = new DateTime((long)startTime * 1000, DateTimeZone.UTC);
				DateTime startOfPreviousDay = endDate.toLocalDate().minusDays(1).toDateTimeAtStartOfDay(DateTimeZone.UTC);

				// these are the items to keep from the previous feed
				List<Map> previousItems = existingFeed.getItemsInRange(
						startOfPreviousDay.getMillis(), endDate.getMillis());
				if (!previousItems.isEmpty()) {
					newFeed.getItems().addAll(0, previousItems);
				}
				feeds.put(channel.getKey(), newFeed);
			} else {
				logger.warn("New feed response has empty items: " + channel.getEpgFeedUrl());
			}
		} else {
			feeds.put(channel.getKey(), newFeed);
		}
	}


	/**
	 * ExecutorService job that fetches and stores EPG data.  
	 */
	private class FeedJob implements Callable<EPGFeed> {

		private Channel channel;

		public FeedJob(Channel channel) {
			this.channel = channel;
		}

		@Override
		public EPGFeed call() throws Exception {
			URL url = new URL(channel.getEpgFeedUrl());
			try {
				ObjectMapper mapper = new ObjectMapper();
				EPGFeed epg = mapper.readValue(url, EPGFeed.class);
				epg.setChannel(channel);
				logger.debug("Complete EPG data fetch for " + channel.getKey());
				putFeed(channel, epg);
				return epg;
			} catch (Exception e) {
				logger.error("Exception occurred while fetching feed '" +
						channel.getEpgFeedUrl() + "' for channel '"+ channel.getKey() +"': ", e);
				return null;
			}
		}
	}

	public static void main(String[] args) {
		DateTime date = new DateTime();
		System.out.println(date.getZone() + " " + ((date.getZone().getOffset(date.getMillis())/1000) /60 /60) );
		System.out.println(new DateTime(1378767600000l));
	}
}
