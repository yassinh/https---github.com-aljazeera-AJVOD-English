package com.brightcove.consulting.util;

import java.io.File;
import java.util.Collection;

public interface ScanReceiver {

    void filesUpdated(Collection<File> files);

}
