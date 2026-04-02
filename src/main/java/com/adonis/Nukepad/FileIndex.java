/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.adonis.Nukepad;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author croco
 */
public class FileIndex {
    private final CopyOnWriteArrayList<String> pathsList = new CopyOnWriteArrayList<>();
    private final Queue<File> directoryQueue = new LinkedList<>();
    private volatile boolean hasMore = true;

    public void init(File root) {
        pathsList.clear();
        directoryQueue.clear();
        directoryQueue.add(root);
        hasMore = true;
    }

    public void indexNextBatch(int batchSize) {
        int count = 0;
        while (count < batchSize && !directoryQueue.isEmpty()) {
            File dir = directoryQueue.poll();
            if (dir == null)
                break;

            String name = dir.getName().toLowerCase();
            if (name.equals("windows") ||
                    name.equals("$recycle.bin") ||
                    name.equals("system volume information") ||
                    name.equals("programdata")) {
                continue;
            }
            File[] children = dir.listFiles();
            if (children == null)
                continue;
            for (File f : children) {
                pathsList.add(f.getAbsolutePath());
                if (f.isDirectory())
                    directoryQueue.add(f);
                count++;
            }
        }
        hasMore = !directoryQueue.isEmpty();
        System.out.println("Batch done. Total so far:" + pathsList.size());
    }

    public String[] getSortedPaths() {
        String[] snapshot = pathsList.toArray(new String[0]);
        Arrays.sort(snapshot, (a, b) -> {
            String nameA = new File(a).getName().toLowerCase();
            String nameB = new File(b).getName().toLowerCase();
            return nameA.compareTo(nameB);
        });
        return snapshot;
    }

    public boolean hasMore() {
        return hasMore;
    }

    public int getIndexedCount() {
        return pathsList.size();
    }

}
