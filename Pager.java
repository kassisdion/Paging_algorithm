import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

class Pager {

    private String mPages;
    private PAGE_METHOD mPageMethod;
    private int mNumberOfFrame;
    private int mPageFaults;
    private int mPageHit;
    private int mPointer;
    private int mReference[];
    private int mMem_layout[][];
    private PrintWriter mPrintWriter;
    enum PAGE_METHOD {
        FIFO,
        OPT,
        LRU,
        CLOCK
    }

    /*
    ** Constructor
     */
    Pager(final String referenceString, final PAGE_METHOD pageMethod, final int numberOfFrame) {
        mPages = referenceString;
        mPageMethod = pageMethod;
        mNumberOfFrame = numberOfFrame;

        mPageFaults = 0;
        mPageHit = 0;
        mPointer = 0;
        mReference = new int[mPages.length()];
        mMem_layout = new int[mPages.length()][mNumberOfFrame];

        for(int i = 0; i < mPages.length(); i++) {
            mReference[i] = Integer.parseInt(Character.toString(mPages.charAt(i)));
        }
    }

    /*
    ** Public method
     */
    //executes algorithm
    void run() {

        openLogFile();
        writeLog("START: type " + mPageMethod + "/Paging on " + mPages + "\n");

        switch (this.mPageMethod) {
            case FIFO:
                fifo();
                break;
            case OPT:
                opt();
                break;
            case LRU:
                lru();
                break;
            case CLOCK:
                clock();
            default:
        }

        writeLog(
                "**************** RESULTS ********************************************" +
                "\n" + "The number of Hits: " + mPageHit +
                "\n" + "Hit Ratio: " + (float)mPageHit/mPages.length()+
                "\n" + "The number of Faults: " + mPageFaults +
                "\n" + "*********************************************************************");

        closeLogFile();
    }

    /*
    ** private method
     */
    private void fifo() {
        int buffer[] = new int[mNumberOfFrame];

        for (int j = 0; j < mNumberOfFrame; j++) {
            buffer[j] = -1;
        }

        for(int i = 0; i < mPages.length(); i++) {
            int search = -1;
            for(int j = 0; j < mNumberOfFrame; j++) {
                if(buffer[j] == mReference[i]) {
                    search = j;
                    mPageHit++;
                    break;
                }
            }
            if(search == -1) {
                buffer[mPointer] = mReference[i];
                mPageFaults++;
                mPointer++;
                if(mPointer == mNumberOfFrame)
                    mPointer = 0;
            }
            System.arraycopy(buffer, 0, mMem_layout[i], 0, mNumberOfFrame);
        }

        writeLog("**************** MEMORY ********************************************");
        for(int i = 0; i < mNumberOfFrame; i++) {
            for(int j = 0; j < mPages.length(); j++) {
                writeLog("%3d ", mMem_layout[j][i]);
            }
            writeLog("\n");
        }
        writeLog("*********************************************************************");
    }

    private void opt() {
        int buffer[];
        boolean isFull;

        buffer = new int[mNumberOfFrame];
        isFull = false;

        for (int j = 0; j < mNumberOfFrame; j++) {
            buffer[j] = -1;
        }

        for(int i = 0; i < mPages.length(); i++) {
            int search = -1;
            for(int j = 0; j < mNumberOfFrame; j++) {
                if(buffer[j] == mReference[i]) {
                    search = j;
                    mPageHit++;
                    break;
                }
            }
            if(search == -1) {
                if(isFull) {
                    int index[] = new int[mNumberOfFrame];
                    boolean index_flag[] = new boolean[mNumberOfFrame];
                    for(int j = i + 1; j < mPages.length(); j++) {
                        for(int k = 0; k < mNumberOfFrame; k++) {
                            if((mReference[j] == buffer[k]) && (!index_flag[k])) {
                                index[k] = j;
                                index_flag[k] = true;
                                break;
                            }
                        }
                    }
                    int max = index[0];
                    mPointer = 0;
                    if(max == 0) {
                        max = 200;
                    }
                    for(int j = 0; j < mNumberOfFrame; j++) {
                        if(index[j] == 0) {
                            index[j] = 200;
                        }
                        if(index[j] > max) {
                            max = index[j];
                            mPointer = j;
                        }
                    }
                }
                buffer[mPointer] = mReference[i];
                mPageFaults++;
                if(!isFull) {
                    mPointer++;
                    if(mPointer == mNumberOfFrame) {
                        mPointer = 0;
                        isFull = true;
                    }
                }
            }
            System.arraycopy(buffer, 0, mMem_layout[i], 0, mNumberOfFrame);
        }

        writeLog("**************** MEMORY ********************************************");
        for(int i = 0; i < mNumberOfFrame; i++) {
            for(int j = 0; j < mPages.length(); j++) {
                writeLog("%3d ", mMem_layout[j][i]);
            }
            writeLog("\n");
        }
        writeLog("**************** MEMORY ********************************************");
    }

    private void lru() {
        ArrayList<Integer> stack;
        int buffer[];
        boolean isFull;

        stack = new ArrayList<>();
        buffer = new int[mNumberOfFrame];
        isFull = false;

        for (int j = 0; j < mNumberOfFrame; j++) {
            buffer[j] = -1;
        }

        for(int i = 0; i < mPages.length(); i++) {
            if(stack.contains(mReference[i])) {
                stack.remove(stack.indexOf(mReference[i]));
            }
            stack.add(mReference[i]);
            int search = -1;
            for(int j = 0; j < mNumberOfFrame; j++) {
                if(buffer[j] == mReference[i]) {
                    search = j;
                    mPageHit++;
                    break;
                }
            }
            if(search == -1) {
                if(isFull) {
                    int min_loc = mPages.length();
                    for(int j = 0; j < mNumberOfFrame; j++) {
                        if(stack.contains(buffer[j])) {
                            int temp = stack.indexOf(buffer[j]);
                            if(temp < min_loc) {
                                min_loc = temp;
                                mPointer = j;
                            }
                        }
                    }
                }
                buffer[mPointer] = mReference[i];
                mPageFaults++;
                mPointer++;
                if(mPointer == mNumberOfFrame) {
                    mPointer = 0;
                    isFull = true;
                }
            }
            System.arraycopy(buffer, 0, mMem_layout[i], 0, mNumberOfFrame);
        }

        writeLog("**************** MEMORY ********************************************");
        for(int i = 0; i < mNumberOfFrame; i++) {
            for(int j = 0; j < mPages.length(); j++) {
                writeLog("%3d ", mMem_layout[j][i]);
            }
            writeLog("\n");
        }
        writeLog("*********************************************************************");
    }

    private void clock() {
        int buffer[][];
        int used_layout[][];

        buffer = new int[mNumberOfFrame][2];
        used_layout = new int[mPages.length()][mNumberOfFrame];

        for (int j = 0; j < mNumberOfFrame; j++) {
            buffer[j][0] = -1;
            buffer[j][1] = 0;
        }


        for(int i = 0; i < mPages.length(); i++) {
            int search = -1;
            for(int j = 0; j < mNumberOfFrame; j++) {
                if(buffer[j][0] == mReference[i]) {
                    search = j;
                    mPageHit++;
                    buffer[j][1] = 1;
                    break;
                }
            }
            if(search == -1) {
                while(buffer[mPointer][1] == 1) {
                    buffer[mPointer][1] = 0;
                    mPointer++;
                    if(mPointer == mNumberOfFrame) {
                        mPointer = 0;
                    }
                }
                buffer[mPointer][0] = mReference[i];
                buffer[mPointer][1] = 1;
                mPageFaults++;
                mPointer++;
                if(mPointer == mNumberOfFrame) {
                    mPointer = 0;
                }
            }
            for(int j = 0; j < mNumberOfFrame; j++) {
                mMem_layout[i][j] = buffer[j][0];
                used_layout[i][j] = buffer[j][1];
            }
        }

        writeLog("**************** MEMORY ********************************************");
        for(int i = 0; i < mNumberOfFrame; i++) {
            for(int j = 0; j < mPages.length(); j++) {
                writeLog("%3d %d ", mMem_layout[j][i], used_layout[j][i]);
            }
            writeLog("\n");
        }
        writeLog("*********************************************************************");
    }

    /*
    ** Utils
     */
    private void closeLogFile() {
        if (mPrintWriter != null) {
            mPrintWriter.close();
        }
    }

    private void openLogFile() {
        String fileName = "finalresult_paging";

        File file = new File(fileName);
        try {

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            mPrintWriter = new PrintWriter(new FileWriter(file.getAbsoluteFile(), false));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeLog(String var1, Object... var2) {
        System.out.printf(var1, var2);
        mPrintWriter.printf(var1, var2);
    }

    /*
    ** MAIN
    */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        //Display menu option
        String menu = "Memory Paging Simulation Menu:\n\n";
        menu += "1 – Simulate FIFO\n";
        menu += "2 – Simulate OPT\n";
        menu += "3 – Simulate LRU\n";
        menu += "4 – Simulate CLOCK\n> ";
        System.out.print(menu);

        //get the selected option
        if (!scanner.hasNextInt()) {
            throw new IllegalArgumentException("Invalid input : valid selections are 0-3");
        }
        int option = scanner.nextInt();
        if (option < 1 || option > 4) {
            throw new IllegalArgumentException("Invalid input : Valid selections are 0-3");
        }

        //Get number of frames
        System.out.println("Enter the number of frames:");
        if (!scanner.hasNextInt()) {
            throw new IllegalArgumentException("Invalid input : valid selections are [1-9]*");
        }
        int numberOfFrames = scanner.nextInt();
        if (numberOfFrames < 1) {
            throw new IllegalArgumentException("Invalid input : valid selections are [1-9]*");
        }

        //Get pages
        System.out.println("Enter number of pages");
        if (!scanner.hasNextInt()) {
            throw new IllegalArgumentException("Invalid input : valid selections are [1-9]*");
        }
        int referenceStringSize = scanner.nextInt();
        if (referenceStringSize < 1) {
            throw new IllegalArgumentException("Invalid input : valid selections are [1-9]*");
        }
        StringBuilder referenceStringBuilder = new StringBuilder(referenceStringSize);
        String VFRAMES = "01234567";
        Random random = new Random();
        for (int i = 0; i < referenceStringSize; i++) {
            referenceStringBuilder.append(random.nextInt(VFRAMES.length()));
        }
        String referenceString = referenceStringBuilder.toString();
        System.out.println("Reference String : " + referenceString);

        //Start paging algorithm
        switch (option) {
            case 1: //FIFO
                Pager fifo = new Pager(referenceString, Pager.PAGE_METHOD.FIFO, numberOfFrames);
                fifo.run();
                break;
            case 2: //OPT
                Pager opt = new Pager(referenceString, Pager.PAGE_METHOD.OPT, numberOfFrames);
                opt.run();
                break;
            case 3: //LRU
                Pager lru = new Pager(referenceString, Pager.PAGE_METHOD.LRU, numberOfFrames);
                lru.run();
                break;
            case 4: //CLOCK
                Pager clock = new Pager(referenceString, Pager.PAGE_METHOD.CLOCK, numberOfFrames);
                clock.run();
                break;
            default:
                break;
        }
    }
}
