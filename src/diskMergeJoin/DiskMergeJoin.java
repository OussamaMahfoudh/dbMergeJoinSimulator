package diskMergeJoin;

import static diskIO.Constants.MEMORY_PAGE_SIZE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DiskMergeJoin {
	private List<String> inputBuffer;
	private int numberOfRsPages;

	public DiskMergeJoin() {
		inputBuffer = new ArrayList<>(MEMORY_PAGE_SIZE);
	}

	/**
	 * This method is used to Merge relation R and relation S
	 * 
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	public void doMerge() throws IllegalArgumentException, IOException {
		String rDesc;
		String sDesc;
		String[] rContent = null;
		String[] sContent = null;
		try {
			rDesc = diskIO.FileIO.readAllFile("R")[0];
			sDesc = diskIO.FileIO.readAllFile("S")[0];
			rContent = diskIO.FileIO.readAllFile(rDesc);
			sContent = diskIO.FileIO.readAllFile(sDesc);
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (String r : rContent) {
			for (String s : sContent) {
				if (r.equals(s)) {
					updateBuffer(r);
				}
			}
		}
		if (inputBuffer.size() <= MEMORY_PAGE_SIZE) {
			diskIO.FileIO.writeFile(
					diskIO.FileIO.getFileNameWithNumber("RS", numberOfRsPages),
					inputBuffer.toArray(new String[inputBuffer.size()]), true);
			diskIO.FileIO.writeFile("RS", new String[] { diskIO.FileIO
					.getFileNameWithNumber("RS", numberOfRsPages) }, true);
		}
		
		System.out.println("");
		System.out.println("**********************************************");
		System.out.println("Disk Join complete, please check the result RS");
		System.out.println("**********************************************");
	}

	/**
	 * This method put the result element of join in the buffer then write it in
	 * the result file
	 * 
	 * @param toPut
	 *            element to be put in Join result
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */

	public void updateBuffer(final String toPut)
			throws IllegalArgumentException, IOException {
		inputBuffer.add(toPut);
		if (inputBuffer.size() == MEMORY_PAGE_SIZE) {
			diskIO.FileIO.writeFile(
					diskIO.FileIO.getFileNameWithNumber("RS", numberOfRsPages),
					inputBuffer.toArray(new String[inputBuffer.size()]), true);
			diskIO.FileIO.writeFile("RS", new String[] { diskIO.FileIO
					.getFileNameWithNumber("RS", numberOfRsPages) }, true);
			numberOfRsPages++;
			inputBuffer.clear();
		}
	}

	public static void main(String[] args) {
		diskSortMerge.DiskSortMerge.main(args);

		DiskMergeJoin dmj = new DiskMergeJoin();
		try {
			dmj.doMerge();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
