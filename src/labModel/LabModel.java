/**
 * TP n°: 6
 * <p/>
 * Titre du TP : Disk Nested Loop Join
 * <p/>
 * Date : 18/11/2015
 * <p/>
 * Nom  : MAHFOUDH
 * Prenom : Mohamed Oussama
 * <p/>
 * email : mohamedoussama.mahfoudh@gmail.com
 * <p/>
 * Remarques :
 */

package labModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static diskIO.Constants.R_FILE_SIZE;
import static diskIO.FileIO.getFileNameWithNumber;
import static diskIO.FileIO.writeFile;

public class LabModel {

    final String[] AZ = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S",
            "T", "U", "V", "W", "X", "Y", "Z"};

    final String[] AH = {"A", "B", "C", "D", "E", "F", "G", "H"};

    /**
     * This generic method takes two parameters : The list to chunk down and the
     * size of the small chunks It chunks the given list into smaller ones and
     * then returns the list of the chunks.
     *
     * @param bigList
     *            : The list to chunk down
     * @param n:
     *            The size of the smaller pieces
     * @return : List of chunks
     */
    public static ArrayList<String[]> chunks(final List<String> bigList, final int n) {
        ArrayList<String[]> chunks = new ArrayList<String[]>();
        for (int i = 0; i < bigList.size(); i += n) {
            String[] chunk = bigList.subList(i, (i + n) > bigList.size() ? i + (bigList.size() - i) : (i + n))
                    .toArray(new String[Math.min(bigList.size() - i, n)]);
            chunks.add(chunk);
        }
        return chunks;
    }

    public static void main(String[] args) {
        new LabModel().go();
    }

    /**
     * This method performs a Cartesian product between the AZ and AH arrays
     * contents. It then chunks the result of the product into smaller arrays of
     * 10 elements each The contents of these smaller arrays is saved into Files
     * and a descriptor containing the names of the files is created and
     * returned.
     *
     * @return The descriptor of The R 'Relationship'
     * @throws IllegalArgumentException
     * @throws IOException
     */
    public List<String> generateRblocks(boolean doWrite) throws IllegalArgumentException, IOException {
        final List<String> cartesianList = new ArrayList<String>();
        List<String[]> cartesianChunks = new ArrayList<String[]>();
        List<String> rFileDescriptor = new ArrayList<String>();
        String chunkFileName = null;
        for (String a : AZ) {
            for (String b : AH) {
                cartesianList.add(a + b);
            }
        }
        Collections.shuffle(cartesianList);
        cartesianChunks = LabModel.chunks(cartesianList, R_FILE_SIZE);
        for (int i = 0; i < cartesianChunks.size(); i++) {

            chunkFileName = getFileNameWithNumber("R", i);
            rFileDescriptor.add(chunkFileName);
            if (doWrite) {
                writeFile(chunkFileName, cartesianChunks.get(i), false);
            }
        }
        Collections.shuffle(rFileDescriptor);
        if (doWrite) {
            writeFile("R", rFileDescriptor.toArray(new String[rFileDescriptor.size()]), false);
        }
        return rFileDescriptor;
    }

    /**
     * This method creates a list of the fileNames of the S 'RelationShip' then
     * shuffles them and put the result in the S descriptor list.
     *
     * @return The descriptor of the 'S' relationship
     * @throws IOException
     */
    public List<String> makeAndSaveSDescriptor(boolean doWrite) throws IOException {
        final ArrayList<String> sDescriptor = new ArrayList<>(
                Arrays.asList("S00", "S01", "S02", "S03", "S04", "S05", "S06", "S07", "S08"));
        Collections.shuffle(sDescriptor);
        if (doWrite) {
            writeFile("S", sDescriptor.toArray(new String[sDescriptor.size()]), false);
        }
        return sDescriptor;
    }

    private void go() {
        try {
            generateRblocks(true);
            makeAndSaveSDescriptor(true);
        } catch (IllegalArgumentException | IOException e) {
            e.printStackTrace();
        }
    }
}
