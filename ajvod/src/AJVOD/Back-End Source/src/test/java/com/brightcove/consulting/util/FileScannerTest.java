package com.brightcove.consulting.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

public class FileScannerTest implements ScanReceiver {

    private Collection<File> updatedFiles;
    private String rootDir;
    private FileScanner scanner;
    private File file1;

    @Before
    public void init() throws IOException {
        rootDir = new ClassPathResource("/channels").getFile().getAbsolutePath();
        scanner = new FileScanner();
        scanner.setRootPath(rootDir);

        scanner.register(this);

        // sanity check
        file1 = new File(rootDir + "/alj-arb/channel-config.xml");
        assertTrue(file1.exists());

        FileFilter filter = new RegexFileFilter("channel-config(.*)\\.xml");
        scanner.setFilter(filter);
    }

    /**
     * Given a directory containing two matching files.
     * @throws IOException 
     */
    @Test
    public void itShouldReturnBothFilesOnScan() {

        Collection<File> files = scanner.performScan();
        assertEquals(2, files.size());
    }

    /**
     * Given a directory containing two matching files, when a new file is added
     */
    @Test
    public void itShouldReturnTheNewFileOnScan() throws Exception {
        // perform an initial scan to load up the files
        scanner.performScan();
        // and sanity check
        assertEquals(2, scanner.getFiles().size());

        // create new sub directory and config file
        File subdir = new File(rootDir + "/test01");
        subdir.mkdirs();
        subdir.deleteOnExit();
        File newConfig = File.createTempFile("channel-config", ".xml", subdir);
        newConfig.deleteOnExit();

        try {
            Collection<File> files = scanner.performScan();
            assertEquals(1,files.size());
            assertEquals(3, scanner.getFiles().size());
        } catch (Exception e) {
            throw e;
        } finally {
            newConfig.delete();
            subdir.delete();
        }
        
    }


    /**
     * Given a directory containing matching files, when a config file is deleted
     */
    @Test
    public void itShouldReturnTheDeletedFileOnScan() throws IOException {
        // create new sub directory and config file
        File subdir = new File(rootDir + "/test01");
        subdir.mkdirs();
        subdir.deleteOnExit();
        File newConfig = File.createTempFile("channel-config", ".xml", subdir);
        newConfig.deleteOnExit();

        // first scan should contain all files
        Collection<File> files = scanner.performScan();
        assertEquals(3, files.size());

        newConfig.delete();

        files = scanner.performScan();
        assertEquals(1, files.size());
        File file = files.iterator().next();
        assertFalse(file.exists());
    }

    /**
     * Given a directory containing matching files, when a file is modified
     */
    @Test
    public void itShouldReturnTheModifiedFile() {
        scanner.performScan();
        File otherFile = new File(file1.getAbsolutePath());
        otherFile.setLastModified(System.currentTimeMillis());

        scanner.performScan();
        assertEquals(1,updatedFiles.size());
        assertEquals(otherFile, updatedFiles.iterator().next());
    }

    /**
     * Given a scan depth of n
     * @throws Exception 
     */
    @Test
    public void itShouldNotAttemptToScanDirectoriesBeyondTheGivenDepth() throws Exception {
        File subdir = new File(rootDir + "/test02/001/002/003");
        subdir.mkdirs();
        subdir.deleteOnExit();
        File newConfig = File.createTempFile("channel-config", ".xml", subdir);
        newConfig.deleteOnExit();

        try {
            Collection<File> files = scanner.performScan();
            assertEquals(2,files.size());
            assertEquals(2, scanner.getFiles().size());
        } catch (Exception e) {
            throw e;
        } finally {
            newConfig.delete();
            subdir.delete();
            FileUtils.deleteDirectory(new File(rootDir + "/test02"));
        }
    }

    /**
     * Callback from FileScanner.
     */
    @Override
    public void filesUpdated(Collection<File> files) {
        this.updatedFiles = files;
    }
}
