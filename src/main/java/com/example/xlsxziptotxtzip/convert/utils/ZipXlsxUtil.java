package com.example.xlsxziptotxtzip.convert.utils;

import lombok.experimental.UtilityClass;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utility methods for working with ZIP entries that contain XLSX files.
 */
@UtilityClass
public class ZipXlsxUtil {

    private static final int BUFFER_SIZE = 8192;

    /**
     * Checks if the given entry is a non-directory XLSX file.
     *
     * @param entry the ZIP entry
     * @return true if it is a regular .xlsx file, false otherwise
     */
    public boolean isXlsxFile(final ZipEntry entry) {
        if (entry == null || entry.isDirectory()) {
            return false;
        }
        String name = entry.getName();
        return name != null && name.toLowerCase().endsWith(".xlsx");
    }

    /**
     * Reads the entire content of the current entry from the {@link ZipInputStream}
     * into a byte array.
     *
     * @param zis the ZIP input stream, already positioned at an entry
     * @return the bytes of that entry
     * @throws IOException if an IO error occurs
     */
    public byte[] readEntryBytes(final ZipInputStream zis) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int len;
        while ((len = zis.read(buffer)) > 0) {
            baos.write(buffer, 0, len);
        }
        return baos.toByteArray();
    }

}
