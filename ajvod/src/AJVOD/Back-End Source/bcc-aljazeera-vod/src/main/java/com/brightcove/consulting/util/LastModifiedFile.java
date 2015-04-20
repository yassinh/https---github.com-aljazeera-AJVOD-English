package com.brightcove.consulting.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * A File wrapper that will cache the lastModified date.
 * 
 * @author ssayles
 */
@SuppressWarnings("serial")
public class LastModifiedFile extends File {

    private File file;
    private long lastModified;

    public LastModifiedFile(File file) {
        super(file.getAbsolutePath());
        this.file = file;
        lastModified = file.lastModified();
    }


    public long lastModified() {
        return this.lastModified;
    }

    public boolean setLastModified(long time) {
        this.lastModified = time;
        return file.setLastModified(time);
    }

    //.... delegate methods and super constructors .................

    public LastModifiedFile(File parent, String child) {
        super(parent, child);
        file = new File(parent, child);
        lastModified = file.lastModified();
    }

    public LastModifiedFile(String parent, String child) {
        super(parent, child);
        file = new File(parent, child);
        lastModified = file.lastModified();
    }

    public LastModifiedFile(String pathname) {
        super(pathname);
        file = new File(pathname);
        lastModified = file.lastModified();
    }

    public LastModifiedFile(URI uri) {
        super(uri);
        file = new File(uri);
        lastModified = file.lastModified();
    }

    public boolean canExecute() {
        return file.canExecute();
    }

    public boolean canRead() {
        return file.canRead();
    }

    public boolean canWrite() {
        return file.canWrite();
    }

    public int compareTo(File pathname) {
        return file.compareTo(pathname);
    }

    public boolean createNewFile() throws IOException {
        return file.createNewFile();
    }

    public boolean delete() {
        return file.delete();
    }

    public void deleteOnExit() {
        file.deleteOnExit();
    }

    public boolean equals(Object obj) {
        return file.equals(obj);
    }

    public boolean exists() {
        return file.exists();
    }

    public File getAbsoluteFile() {
        return file.getAbsoluteFile();
    }

    public String getAbsolutePath() {
        return file.getAbsolutePath();
    }

    public File getCanonicalFile() throws IOException {
        return file.getCanonicalFile();
    }

    public String getCanonicalPath() throws IOException {
        return file.getCanonicalPath();
    }

    public long getFreeSpace() {
        return file.getFreeSpace();
    }

    public String getName() {
        return file.getName();
    }

    public String getParent() {
        return file.getParent();
    }

    public File getParentFile() {
        return file.getParentFile();
    }

    public String getPath() {
        return file.getPath();
    }

    public long getTotalSpace() {
        return file.getTotalSpace();
    }

    public long getUsableSpace() {
        return file.getUsableSpace();
    }

    public int hashCode() {
        return file.hashCode();
    }

    public boolean isAbsolute() {
        return file.isAbsolute();
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }

    public boolean isFile() {
        return file.isFile();
    }

    public boolean isHidden() {
        return file.isHidden();
    }


    public long length() {
        return file.length();
    }

    public String[] list() {
        return file.list();
    }

    public String[] list(FilenameFilter filter) {
        return file.list(filter);
    }

    public File[] listFiles() {
        return file.listFiles();
    }

    public File[] listFiles(FileFilter filter) {
        return file.listFiles(filter);
    }

    public File[] listFiles(FilenameFilter filter) {
        return file.listFiles(filter);
    }

    public boolean mkdir() {
        return file.mkdir();
    }

    public boolean mkdirs() {
        return file.mkdirs();
    }

    public boolean renameTo(File dest) {
        return file.renameTo(dest);
    }

    public boolean setExecutable(boolean executable, boolean ownerOnly) {
        return file.setExecutable(executable, ownerOnly);
    }

    public boolean setExecutable(boolean executable) {
        return file.setExecutable(executable);
    }


    public boolean setReadOnly() {
        return file.setReadOnly();
    }

    public boolean setReadable(boolean readable, boolean ownerOnly) {
        return file.setReadable(readable, ownerOnly);
    }

    public boolean setReadable(boolean readable) {
        return file.setReadable(readable);
    }

    public boolean setWritable(boolean writable, boolean ownerOnly) {
        return file.setWritable(writable, ownerOnly);
    }

    public boolean setWritable(boolean writable) {
        return file.setWritable(writable);
    }

    public String toString() {
        return file.toString();
    }

    public URI toURI() {
        return file.toURI();
    }

    @Deprecated
    public URL toURL() throws MalformedURLException {
        return file.toURL();
    }


}
