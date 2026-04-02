/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.adonis.Nukepad;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author croco
 */
public class BinarySearcher {
    public static List<String> searchByPrefix(String[] sortedPaths, String prefix) {
        List<String> results = new ArrayList<>();
        if (sortedPaths == null || prefix == null || prefix.isEmpty())
            return results;

        String lowerPrefix = prefix.toLowerCase();
        int lo = 0;
        int hi = sortedPaths.length - 1;
        int first = -1;

        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            String name = fileName(sortedPaths[mid]).toLowerCase();
            if (name.startsWith(lowerPrefix)) {
                first = mid;
                hi = mid - 1;

            } else if (name.compareTo(lowerPrefix) < 0) {
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }

        }
        if (first == -1)
            return results;
        for (int i = first; i < sortedPaths.length; i++) {
            if (fileName(sortedPaths[i]).toLowerCase().startsWith(lowerPrefix)) {
                results.add(sortedPaths[i]);
            } else
                break;
        }
        return results;
    }

    private static String fileName(String path) {
        return new java.io.File(path).getName();
    }

}
