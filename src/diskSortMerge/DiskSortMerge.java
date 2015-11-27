package diskSortMerge;

import diskIO.FileIO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static diskIO.Constants.*;
import static diskIO.FileIO.getFileNameWithNumber;

public class DiskSortMerge {

    private List<List<String>> outputBuffers;
    private List<String> inputBuffer;
    private File tempFile;
    private File tempDescFile;
    private List<Boolean> bufferChecks;
    private int[] readChecks;

    public DiskSortMerge() {
        inputBuffer = new ArrayList<>(MEMORY_PAGE_SIZE);
        outputBuffers = new ArrayList<>(NUMBER_OF_MEMORY_PAGES - 1);
        bufferChecks = new ArrayList<>(NUMBER_OF_MEMORY_PAGES - 1);
        for (int i = 0; i < NUMBER_OF_MEMORY_PAGES - 1; i++) {
            outputBuffers.add(new ArrayList<String>(MEMORY_PAGE_SIZE));
        }
        for (int i = 0; i < NUMBER_OF_MEMORY_PAGES - 1; i++) {
            bufferChecks.add(false);
        }
        //bufferChecks = new boolean[NUMBER_OF_MEMORY_PAGES - 1];
        readChecks = new int[NUMBER_OF_MEMORY_PAGES - 1];
        try {
            tempDescFile = File.createTempFile("tempDesc", ".txt", new File("testFiles/"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        FileIO.main(null);
        DiskSortMerge dsm = new DiskSortMerge();
        try {
            dsm.diskSort("R");
            dsm.diskSort("S");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void diskSort(final String relationName) throws IOException {
        String[] fileDescriptorContent;
        int minRef = -1;
        int numberOfReadPages = diskIO.FileIO.readAllFile(relationName).length;
        for (int i = 0; i < numberOfReadPages; i++) {
            page_Sort(getFileNameWithNumber(relationName, i));
        }
        while (numberOfReadPages > 1) {
            fileDescriptorContent = diskIO.FileIO.readAllFile(relationName);
            for (int i = 0; i < numberOfReadPages; i = (i + NUMBER_OF_MEMORY_PAGES - 1) > numberOfReadPages
                    ? i += (numberOfReadPages - i) : i + NUMBER_OF_MEMORY_PAGES - 1) {
                outputBuffers.clear();
                bufferChecks.clear();
                for (int j = 0; j < Math.min(NUMBER_OF_MEMORY_PAGES - 1, numberOfReadPages - i); j++) {
                    outputBuffers.add(new ArrayList<String>(MEMORY_PAGE_SIZE));
                    outputBuffers.get(j).addAll(
                            Arrays.asList(diskIO.FileIO.readFile(fileDescriptorContent[(i + j)],
                                    MEMORY_PAGE_SIZE)));
                    bufferChecks.add(false);
                    readChecks[j]++;
                }
                boolean finishRun = false;
                while (!finishRun) {
                    minRef = getMin(outputBuffers);
                    if (minRef >= 0) {
                        inputBuffer.add(outputBuffers.get(minRef).get(0));
                        outputBuffers.get(minRef).remove(0);
                        checkOutPutBuffers(i, relationName);
                        checkInputBufferIsFull();
                        finishRun = checkFinish();
                    } else {
                        finishRun = true;
                        dumpBuffer();
                    }
                }
                forceRename(tempFile, new File("./testFiles/" + fileDescriptorContent[i] + ".txt"));
                diskIO.FileIO.writeFile(tempDescFile.getName().substring(0, tempDescFile.getName().length() - 4),
                        new String[]{fileDescriptorContent[i]},
                        true);
                tempFile = null;
                for (int m = 0; m < readChecks.length; m++) {
                    readChecks[m] = 0;
                }
            }
            forceRename(tempDescFile, new File("./testFiles/" + relationName + ".txt"));
            numberOfReadPages = diskIO.FileIO.readAllFile(relationName).length;
        }
        System.out.println("*****************************************************");
        System.out.println("Sorting complete for relation " + relationName);
        System.out.println("Sorting result content will be found in file " + diskIO.FileIO.readAllFile(relationName)[0] + ".txt");
        System.out.println("*****************************************************");
    }

    private void dumpBuffer() throws IOException {
        if (tempFile == null) {
            tempFile = File.createTempFile("temp", ".txt", new File("testFiles/"));
        }
        FileIO.writeFile(tempFile.getName().substring(0, tempFile.getName().length() - 4), inputBuffer.toArray(new String[inputBuffer.size()]), true);
        inputBuffer.clear();
    }

    private boolean checkFinish() {
        for (boolean finish : bufferChecks) {
            if (!finish) {
                return false;
            }
        }
        return true;
    }

    public void page_Sort(final String fileName) throws IOException {
        final String[] blocContents = diskIO.FileIO.readFile(fileName, R_FILE_SIZE);
        final List<String> contentList = new ArrayList<String>(Arrays.asList(blocContents));
        Collections.sort(contentList);
        diskIO.FileIO.writeFile(fileName, contentList.toArray(new String[contentList.size()]), false);
    }

    public int getMin(List<List<String>> buffers) {
        if (!buffers.isEmpty()) {
            int min = 0;
            for (int i = 0; i < buffers.size(); i++) {
                if (buffers.get(i) != null && buffers.get(i).get(0).compareTo(buffers.get(min).get(0)) <= 0) {
                    min = i;
                }
            }
            return min;
        }
        return -1;
    }

    private void checkInputBufferIsFull() throws IOException {
        if (inputBuffer.size() == MEMORY_PAGE_SIZE || checkFinish()) {
            if (tempFile == null) {
                tempFile = File.createTempFile("temp", ".txt", new File("testFiles/"));
            }
            FileIO.writeFile(tempFile.getName().substring(0, tempFile.getName().length() - 4), inputBuffer.toArray(new String[inputBuffer.size()]), true);
            inputBuffer.clear();
        }
    }

    private void checkOutPutBuffers(final int offset, final String relationName) throws IllegalArgumentException, IOException {
        String[] fileDescriptorContent = diskIO.FileIO.readAllFile(relationName);
        List<String> auxBuffer = null;
        List<String> readingList = null;
        for (int j = 0; j < outputBuffers.size(); j++) {
            auxBuffer = outputBuffers.get(j);
            if (auxBuffer.isEmpty()) {
                readingList = Arrays.asList(diskIO.FileIO.readFile(fileDescriptorContent[(j + offset)],
                        MEMORY_PAGE_SIZE, readChecks[j] * MEMORY_PAGE_SIZE));
                if (readingList.isEmpty()) {
                    outputBuffers.remove(j);
                    bufferChecks.set(j, true);
                } else {
                    readChecks[j]++;
                    outputBuffers.get(j).addAll(readingList);
                }
            }
        }
    }

    public void forceRename(File source, File target) throws IOException {
        if (target.exists()) {
            target.delete();
            source.renameTo(target);
        }
    }

}
