package diskIO;

public class Constants {
	
	/*
	 * Size of one page of R or S in the memory
	 */
	public static final int MEMORY_PAGE_SIZE = 10;
	
	/*
	 * Size of one block in relation R
	 */
	public static final int R_FILE_SIZE = 10;
	
	/*
	 * Number of elements in relation dump R
	 */
	public static final int R_SIZE = 208;
	
	/*
	 * Number of blocks in relation S
	 */
	public static final int S_FILE_SIZE = 10;
	
	/*
	 * Number of blocks in relation S
	 */
	public static final int NB_S_BLOCKS = 9;
	
	/*
	 * Number of buffers required to sort relation R and S
	 * Example : 3 buffers = 2 for reading from disk blocks
	 * and 1 for writing into disk blocks.
	 */
	public static final int NUMBER_OF_MEMORY_PAGES = 3;
}
