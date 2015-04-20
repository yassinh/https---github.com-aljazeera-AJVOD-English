package com.brightcove.consulting.alj.models;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.commons.io.filefilter.NameFileFilter;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import com.brightcove.consulting.util.FileScanner;

public class NavRepositoryTest {

    private NavRepository repository;
    private FileScanner channelScanner;
    private FileScanner topicScanner;

    @Before
    public void setUp() throws IOException {
        repository = new NavRepository();
        String rootDir = new ClassPathResource("/channels").getFile().getAbsolutePath();

        channelScanner = new FileScanner();
        channelScanner.setRootPath(rootDir);
        channelScanner.setFilter(new NameFileFilter("channel-config.xml"));
        repository.channelConfigScanner = channelScanner;
    }

    /**
     * Given valid configurations
     */
    @Test
    public void itShouldProvideAllChannelsAfterInitialization() {
        repository.init();
        assertEquals(2,repository.getChannels().size());
    }

    /**
     * Given a config file parent directory of alj-arb
     * and a channel configuration with a different key
     */
    public void itShouldEstablishTheKeyOfTheChannelAsTheDirectoryName() {
        repository.init();
        Channel channel = repository.getChannel("alj-arb");
        assertNotNull(channel);
        assertEquals("alj-arb", channel.getKey());
    }


    /**
     * Given a channel configuration with more than one top-level Channel
     * @throws IOException 
     */
    public void itShouldOnlyLoadTheFirstChannelPerConfiguration() throws IOException {
        String rootDir = new ClassPathResource("/bad-channels").getFile().getAbsolutePath();
        channelScanner.setRootPath(rootDir);
        topicScanner.setRootPath(rootDir);

        repository.init();
        assertNull(repository.getChannel("alj-arb2"));
        assertNotNull(repository.getChannel("alj-arb"));
        assertEquals("Aljazeera Arabic", repository.getChannel("alj-arb").getName());
    }

//    /**
//     * Given a valid configuration exists,
//     * and the NavRepository has been initialized,
//     * when the configuration is removed
//     * @throws IOException 
//     */
//    public void itShouldRemoveChannelsThatHaveHadTheirCorrespondingConfigurationFileRemoved() throws IOException {
//        
//        repository.init();
//    }
//
//    /**
//     * Given a repository that has already been initialized
//     */
//    public void itShouldAddAnyNewConfigurationsItFinds() {
////        repository.setChannelScanner(new FileScanner() {});
////        repository.setTopicScanner(new FileScanner() {});
//        
//    }

}
