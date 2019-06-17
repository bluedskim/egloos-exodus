package org.dskim.egloosExodus;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class GetFolderSizeDemo {
    private static final Logger logger = LogManager.getLogger(GetFolderSizeDemo.class);

    public static void main(String args[]) {
        long folderSize = FileUtils.sizeOfDirectory(new File("/home/bluedskim/IdeaProjects/egloosexodus/blogRootDir/adrush"));
        logger.debug("folderSize={}, {}", folderSize, folderSize /1024 /1024);

        File file = new File("/");
        long totalSpace = file.getTotalSpace(); //total disk space in bytes.
        long usableSpace = file.getUsableSpace(); ///unallocated / free disk space in bytes.
        long freeSpace = file.getFreeSpace(); //unallocated / free disk space in bytes.

        System.out.println(" === Partition Detail ===");

        System.out.println(" === bytes ===");
        System.out.println("Total size : " + totalSpace + " bytes");
        System.out.println("Space free : " + usableSpace + " bytes");
        System.out.println("Space free : " + freeSpace + " bytes");

        System.out.println(" === mega bytes ===");
        System.out.println("Total size : " + totalSpace /1024 /1024 + " mb");
        System.out.println("Space free : " + usableSpace /1024 /1024 + " mb");
        System.out.println("Space free : " + freeSpace /1024 /1024 + " mb");

        System.out.println(" === giga bytes ===");
        System.out.println("Total size : " + totalSpace /1024 /1024 /1024 + " gb");
        System.out.println("Space free : " + usableSpace /1024 /1024 /1024 + " gb");
        System.out.println("Space free : " + freeSpace /1024 /1024 /1024 + " gb");
    }
}
