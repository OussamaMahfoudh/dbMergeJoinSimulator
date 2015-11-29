package diskSortMerge;

import static diskIO.Constants.MEMORY_PAGE_SIZE;
import static diskIO.Constants.NUMBER_OF_MEMORY_PAGES;
import static diskIO.Constants.R_FILE_SIZE;
import static diskIO.FileIO.getFileNameWithNumber;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import diskIO.FileIO;

public class DiskSortMerge {

	private List<List<String>> outputBuffers;
	private List<String> inputBuffer;
	private File tempFile;
	private File tempDescFile;
	private List<Boolean> bufferChecks;
	private int[] readChecks;
	private int numberOfDiskBlocReadings;
	private int numberOfDiskBlocWritings;

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
		// bufferChecks = new boolean[NUMBER_OF_MEMORY_PAGES - 1];
		readChecks = new int[NUMBER_OF_MEMORY_PAGES - 1];
		try {
			tempDescFile = File.createTempFile("tempDesc", ".txt", new File("testFiles/"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method given a relation name (Example : R) reads the relation
	 * descriptor gets all the relation disk blocks and sorts them using a
	 * constant number of memory page buffers Each iteration reduces the number
	 * of disk blocks by half The method exits when the a final unique disk
	 * block is reached in which all the relation content are written sorted.
	 * 
	 * @param relationName
	 *            : The relation name to be sorted
	 * @throws IOException
	 */
	public void diskSort(final String relationName) throws IOException {
		String[] fileDescriptorContent;
		int numberOfRuns = 0;
		int minRef = -1;
		int numberOfReadPages = diskIO.FileIO.readAllFile(relationName).length;
		for (int i = 0; i < numberOfReadPages; i++) {
			page_Sort(getFileNameWithNumber(relationName, i));
		}
		while (numberOfReadPages > 1) {
			System.out.println("****************************************");
			System.out.println("Executing run number " + (++numberOfRuns));
			fileDescriptorContent = diskIO.FileIO.readAllFile(relationName);
			System.out.println("Sorting disk blocks found in " + relationName + " descriptor:");
			for (String fileName : fileDescriptorContent) {
				System.out.println("-"+fileName);
			}
			for (int i = 0; i < numberOfReadPages; i = (i + NUMBER_OF_MEMORY_PAGES - 1) > numberOfReadPages
					? i += (numberOfReadPages - i) : i + NUMBER_OF_MEMORY_PAGES - 1) {
				outputBuffers.clear();
				bufferChecks.clear();
				for (int j = 0; j < Math.min(NUMBER_OF_MEMORY_PAGES - 1, numberOfReadPages - i); j++) {
					numberOfDiskBlocReadings++;
					outputBuffers.add(new ArrayList<String>(MEMORY_PAGE_SIZE));
					outputBuffers.get(j).addAll(
							Arrays.asList(diskIO.FileIO.readFile(fileDescriptorContent[(i + j)], MEMORY_PAGE_SIZE)));
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
						new String[] { fileDescriptorContent[i] }, true);
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
		System.out.println(
				"Sorting result content will be found in file " + diskIO.FileIO.readAllFile(relationName)[0] + ".txt");
		System.out.println("*****************************************************");
		System.out.println("*************************Benchmarks***********************");
		System.out.println("Total number of disk bloc readings = " + numberOfDiskBlocReadings);
		System.out.println("Total number of disk bloc writings = " + numberOfDiskBlocWritings);
		System.out.println("*************************Benchmarks***********************");
	}

	/**
	 * This method will dump the content of the buffer in the current file
	 * getting written on It is useful to invoke this method when the inner loop
	 * of the sorting algorithm could exit without saving the content of the
	 * buffer.
	 * 
	 * @throws IOException
	 */
	private void dumpBuffer() throws IOException {
		if (tempFile == null) {
			tempFile = File.createTempFile("temp", ".txt", new File("testFiles/"));
		}
		FileIO.writeFile(tempFile.getName().substring(0, tempFile.getName().length() - 4),
				inputBuffer.toArray(new String[inputBuffer.size()]), true);
		inputBuffer.clear();
		numberOfDiskBlocWritings++;
	}

	/**
	 * This method checks if the inner loop is finished or not It checks the
	 * bufferChecks array if all its entries are true it means that all the disk
	 * blocks in a certain iteration have been read and the resulting file can
	 * be written to disk
	 * 
	 * @return
	 */
	private boolean checkFinish() {
		for (boolean finish : bufferChecks) {
			if (!finish) {
				return false;
			}
		}
		return true;
	}

	/**
	 * This method given a fileName (disk block name) will read it and sort it
	 * and write the result in the same file
	 * 
	 * @param fileName
	 *            : The file to be sorted
	 * @throws IOException
	 */
	public void page_Sort(final String fileName) throws IOException {
		final String[] blocContents = diskIO.FileIO.readFile(fileName, R_FILE_SIZE);
		final List<String> contentList = new ArrayList<String>(Arrays.asList(blocContents));
		Collections.sort(contentList);
		diskIO.FileIO.writeFile(fileName, contentList.toArray(new String[contentList.size()]), false);
	}

	/**
	 * This method given a number of buffers will look for the smallest value of
	 * the first elements of each buffer and return the position of the buffer
	 * containing the smallest first element in the list of buffers.
	 * 
	 * @param buffers
	 * @return
	 */
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

	/**
	 * This method checks constantly if the input buffer in which we write the
	 * sorting result is full or not. If it is then this method will write its
	 * content in a temporary file which then will be renamed to the name of
	 * disk block
	 * 
	 * @throws IOException
	 */
	private void checkInputBufferIsFull() throws IOException {
		if (inputBuffer.size() == MEMORY_PAGE_SIZE || checkFinish()) {
			if (tempFile == null) {
				tempFile = File.createTempFile("temp", ".txt", new File("testFiles/"));
			}
			FileIO.writeFile(tempFile.getName().substring(0, tempFile.getName().length() - 4),
					inputBuffer.toArray(new String[inputBuffer.size()]), true);
			inputBuffer.clear();
			numberOfDiskBlocWritings++;
		}
	}

	/**
	 * This method checks if the buffers that read from the disk blocks are
	 * empty or not if one is empty it reads its corresponding file and gets a
	 * number of lines equal to the size of the sort buffer if there are no more
	 * elements to read then the methods sets the corresponding bufferCheck to
	 * true to signal that this buffer will no longer be used in the sort
	 * algorithm. When there are no more buffers to use in the sorting algorithm
	 * it means that the iteration is done and we should move to the next set of
	 * disk blocks.
	 * 
	 * @param offset
	 * @param relationName
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	private void checkOutPutBuffers(final int offset, final String relationName)
			throws IllegalArgumentException, IOException {
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
					numberOfDiskBlocReadings++;// Here we only count a reading
												// from the disk if the result
												// of
					readChecks[j]++; // diskIO.FileIO.readFile is not empty
					outputBuffers.get(j).addAll(readingList);
				}
			}
		}
	}

	/**
	 * This method is used to rename a file if the file exists
	 * 
	 * @param source
	 *            source file
	 * @param target
	 *            target file
	 * @throws IOException
	 */

	public void forceRename(File source, File target) throws IOException {
		if (target.exists()) {
			target.delete();
			source.renameTo(target);
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

}
