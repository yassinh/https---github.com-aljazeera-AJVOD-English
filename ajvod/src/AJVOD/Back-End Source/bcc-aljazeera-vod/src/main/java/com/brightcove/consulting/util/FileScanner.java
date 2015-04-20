package com.brightcove.consulting.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.filefilter.DelegateFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Scans for files matching a given filter and can provide files that have been
 * added, modified, or deleted.  Users may
 * initiate the directory scanning by calling {@link #performScan()}.
 * 
 * <p>
 * Before use, the following properties should be set:
 * <ul>
 * <li><b>{@link #filter}</b> A {@link FileFilter} that is used to filter the
 *   scanned files for matches.</li>
 * <li><b>{@link #rootPath}</b> The top level directory to scan for files.</li>
 * <li><b>{@link #depth}</b> The maximum depth to traverse sub-directories. This
 *   is defaulted to 3.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Instances of <code>FileScanner</code> will maintain all files that have been 
 * scanned and compare to the timestamp on subsequent scans.  The initial scan 
 * will return all matching files.  All subsequent scans will return only those 
 * files that have been added, modified, or deleted.  The file updates are passed
 * to all registered {@link ScanReceiver}s via {@link ScanReceiver#filesUpdated(Collection)}.
 * Each receiver can test if the file was removed by checking <code>file.exists()</code>.
 * </p>
 * 
 * <p>
 * To perform a full re-scan of files call {@link #clear()} and then {@link #performScan()}
 * will return all matching files.
 * </p>
 *
 * @author ssayles
 */
public class FileScanner {

    protected final Log logger = LogFactory.getLog(getClass());

    private FileFilter filter;
    
    private int depth = 3;
    
    private String rootPath;

    private List<ScanReceiver> receivers = new ArrayList<ScanReceiver>();

    private volatile List<File> files = new ArrayList<File>();

    public FileScanner() {
    }

    public void register(ScanReceiver receiver) {
        receivers.add(receiver);
    }

    public synchronized void clear() {
        synchronized(files) {
            files.clear();
        }
    }

    /**
     * Returns all files that have been scanned.
     */
    public List<File> getFiles() {
        return Collections.unmodifiableList(files);
    }

    public FileFilter getFilter() {
        return filter;
    }

    public void setFilter(FileFilter filter) {
        this.filter = filter;
    }



    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }


    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
        File rootDir = new File(rootPath);
        if (!rootDir.isDirectory()) {
            throw new IllegalArgumentException("The given rootPath '" + rootPath + "' is not a directory.");
        }
        if (!rootDir.canRead()) {
            throw new IllegalArgumentException("The given rootPath '" + rootPath + "' is not readable.");
        }
        
    }

    /**
     * Scans all files and returns any new, deleted, or modified files.  This 
     * executes synchronously.
     *
     * @return A collection of all newly modified files.
     */
    public synchronized Collection<File> performScan() {
        logger.debug("Performing directory scan in " + this.rootPath);
        UpdateWalker directoryWalker = new UpdateWalker(this.filter, this.depth, getFiles());
        Collection<File> modifiedFiles = directoryWalker.getFiles();

        synchronized(files) {
            this.files = directoryWalker.getScannedFiles();
        }

        if (!modifiedFiles.isEmpty()) {
            for (ScanReceiver receiver : receivers) {
                receiver.filesUpdated(modifiedFiles);
            }
        }

        return modifiedFiles;
    }

    /**
     * DirectoryWalker that will return any new, updated, or removed files based
     * on comparison of a given list of files.
     */
    @SuppressWarnings("rawtypes")
    public class UpdateWalker extends DirectoryWalker {

        /** All files scanned by this instance */
        private List<File> scannedFiles = new ArrayList<File>();

        /** The original list of files to compare against. */
        final private List<File> originalFiles;


        public UpdateWalker(FileFilter filter, int depthLimit, List<File> originalFiles) {
            super(filter, depthLimit);

            // make sure we include directories to recurse through
            // TODO: see if this can be handled another way, possibly by using
            //  the other constructor that takes a directory filter in addtion
            OrFileFilter orFilter = new OrFileFilter();
            orFilter.addFileFilter(new DelegateFileFilter(filter));
            orFilter.addFileFilter(DirectoryFileFilter.INSTANCE);
            setFilter(orFilter);

            this.originalFiles = originalFiles;
        }
 
        @Override
        @SuppressWarnings("unchecked")
        protected void handleFile(File file, int depth, Collection results) {
            file = new LastModifiedFile(file);

            // keep track of what files we've scanned so we know what has
            // been deleted
            scannedFiles.add(file);

            if (originalFiles.contains(file)) {
                // see if the file has been updated
                int index = originalFiles.indexOf(file);
                long origLastModified = originalFiles.get(index).lastModified();
                if (file.lastModified() > origLastModified) {
                    results.add(file);
                }
            } else {
                // this is a new file
                results.add(file);
            }
        }
        
        @Override
        protected File[] filterDirectoryContents(File directory, int depth, File[] files) throws IOException {
            return directory.listFiles(filter);
        }

        
        /**
         * Performs a directory scan and returned all updated files (including
         * deleted files).
         *
         * @return A list of updated Files.
         */
        @SuppressWarnings("unchecked")
        public Collection<File> getFiles() {
            String name = rootPath.toString();
            try {
                File root = new File(rootPath);
                Collection<File> updatedFiles = new ArrayList<File>();
                walk(root, updatedFiles);

                Collection<File> removedFiles = CollectionUtils.subtract(originalFiles, scannedFiles);
                updatedFiles.addAll(removedFiles);

                return updatedFiles;
            } catch (IOException e) {
                logger.error("Exception occurred while attempting to scan for files under " + name, e);
                return null;
            }
        }

        public List<File> getScannedFiles() {
            return scannedFiles;
        }
    }

}
