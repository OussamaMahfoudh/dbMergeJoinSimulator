package diskMergeJoin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static diskIO.Constants.MEMORY_PAGE_SIZE;

public class DiskMergeJoin {
    private List<String> inputBuffer;
    private int numberOfRsPages;

    public DiskMergeJoin() {
        inputBuffer = new ArrayList<>(MEMORY_PAGE_SIZE);
    }

    public static void main(String[] args) {

    }

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
    }

    public void updateBuffer(final String toPut) throws IllegalArgumentException, IOException {
        inputBuffer.add(toPut);
        if (inputBuffer.size() == MEMORY_PAGE_SIZE) {
            diskIO.FileIO.writeFile(diskIO.FileIO.getFileNameWithNumber("RS", numberOfRsPages),
                    inputBuffer.toArray(new String[inputBuffer.size()]), false);
            diskIO.FileIO.writeFile("RS", new String[]{diskIO.FileIO.getFileNameWithNumber("RS", numberOfRsPages)},
                    false);
            numberOfRsPages++;
            inputBuffer.clear();
        }
    }
}
