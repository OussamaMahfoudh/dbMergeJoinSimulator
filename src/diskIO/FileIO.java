package diskIO;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import labModel.LabModel;

public class FileIO {

	/**
	 * This method is used to read the R.txt file or the S.txt file with number
	 * of elements to be read
	 * 
	 * @param fileName
	 *            The name of the file to be read(either R.txt or S.txt)
	 * @param nbRead
	 *            The number of entries to be read
	 * 
	 * @return Returns an array of char containing the characters read in the
	 *         file
	 * @throws IOException
	 */
	public static String[] readFile(final String fileName, final int nbRead) throws IOException {
		if (fileName == null) {
			return null;
		} else {
			List<String> stringList = new ArrayList<String>();
			try (FileReader rd = new FileReader("testFiles/" + fileName + ".txt");
					BufferedReader br = new BufferedReader(rd)) {
				String line;
				int readCount = 0;
				while ((line = br.readLine()) != null && readCount < nbRead) {
					stringList.add(line);
					readCount++;
				}
			}
			return stringList.toArray(new String[stringList.size()]);
		}
	}

	/**
	 * This method is used to read a file into a String array with number of
	 * elements to be read and pointer in position where start reading
	 * 
	 * @param fileName
	 *            The name of the file to be read(either R.txt or S.txt)
	 * @param nbRead
	 *            The number of entries to be read
	 * @param startPosition
	 *            pointer where start reading the file
	 * @return Returns an array of char containing the characters read in the
	 *         file
	 * @throws IOException
	 */
	public static String[] readFile(final String fileName, final int nbRead, int startPosition) throws IOException {
		if (fileName == null) {
			return null;
		} else {
			List<String> stringList = new ArrayList<String>();
			try (FileReader rd = new FileReader("testFiles/" + fileName + ".txt");
					BufferedReader br = new BufferedReader(rd)) {
				String line;
				int readCount = 0;
				for (int i = 0; i < startPosition; i++) {
					br.readLine();
				}
				while ((line = br.readLine()) != null && readCount < nbRead) {
					stringList.add(line);
					readCount++;
				}
			}
			return stringList.toArray(new String[stringList.size()]);
		}
	}

	/**
	 * This method is used to read the R.txt file or the S.txt file
	 * 
	 * @param fileName
	 *            The name of the file to be read(either R.txt or S.txt)
	 */

	public static String[] readAllFile(final String fileName) throws IOException {
		if (fileName == null) {
			return null;
		} else {
			List<String> stringList = new ArrayList<String>();
			try (FileReader rd = new FileReader("testFiles/" + fileName + ".txt");
					BufferedReader br = new BufferedReader(rd)) {
				String line;
				while ((line = br.readLine()) != null) {
					stringList.add(line);
				}
			}
			return stringList.toArray(new String[stringList.size()]);
		}
	}

	/**
	 * This method is used to write an array of characters to a file
	 * 
	 * @param fileName
	 *            The name of the file to be written in
	 * @param stringsToWrite
	 *            The array of characters to be written
	 * @throws IOException
	 */
	public static void writeFile(final String fileName, final String[] stringsToWrite, final boolean append)
			throws IOException {
		try (FileWriter fw = new FileWriter(new File("testFiles/" + fileName + ".txt"), append);
				BufferedWriter br = new BufferedWriter(fw)) {
			br.write("");
			for (String character : stringsToWrite) {
				br.write(character);
				br.newLine();
			}
			br.flush();
		}
	}

	/**
	 * This method is used to get the right fileName with an integer number
	 * 
	 * @param fileName
	 *            The name of the file to be transformed
	 * @param fileNumber
	 *            The number to transform the fileName with
	 * @return Returns the fileName with the given number appended to it
	 * @throws IllegalArgumentException
	 */
	public static String getFileNameWithNumber(final String fileName, final int fileNumber)
			throws IllegalArgumentException {
		if (fileName == null || fileNumber < 0) {
			throw new IllegalArgumentException();
		}
		return fileName.concat(fileNumber < 10 ? "0" + fileNumber : "" + fileNumber);
	}

	public static void main(String[] args) {
		LabModel.main(null);
		String[] rDesc;
		String[] sDesc;
		List<String> rContent = new ArrayList<>();
		List<String> sContent = new ArrayList<>();
		try {
			rDesc = FileIO.readAllFile("R");
			sDesc = FileIO.readAllFile("S");
			for (String rFile : rDesc) {
				rContent.addAll(Arrays.asList(readAllFile(rFile)));
			}
			for (String sFile : sDesc) {
				sContent.addAll(Arrays.asList(readAllFile(sFile)));
			}
			System.out.println("************************* Unsorted R content : *************************");
			for (String string : rContent) {
				System.out.print(string + "-");
			}
			System.out.println("");
			System.out.println("************************* Unsorted S content : *************************");
			for (String string : sContent) {
				System.out.print(string + "-");
			}
			System.out.println();
			System.out.println("************************************************************************");
			System.out.println("");
			writeFile("R_ALL", rContent.toArray(new String[rContent.size()]), false);
			writeFile("S_ALL", sContent.toArray(new String[sContent.size()]), false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
