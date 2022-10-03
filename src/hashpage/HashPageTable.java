package hashpage;
import java.util.ArrayList;

class HashPageTable {
    HashPageTable(int memorySize, int pageSize) {
        mMemorySize = memorySize;
        mPageSize = pageSize;

        mPageNum = mMemorySize * 1024 / mPageSize;
        mPageItemSize = 
            Integer.toBinaryString(mPageNum/4 - 1).length() // Pid size
            +Integer.toBinaryString(mPageNum - 1).length() // Logic page id size
            +Integer.toBinaryString(mPageNum - 1).length() // Collision count size
                        + 1; // Idle mark size
        mPageItemSize = (int) Math.ceil(mPageItemSize / 8.0);
                                
        mPageTableSize = mPageNum * mPageItemSize;
        mHash = new Hash(mPageNum);
        
        mPageList = new ArrayList<HashPageItem>();
        for(int i=0;i<mPageNum;++i)
        {
            mPageList.add(new HashPageItem());
        }
    }

    public int getPageNum()
    {
        return mPageNum;
    }

    public int getOccupiedPageNum()
    {
        return mOccupiedPageNum;
    }

    public int getPageTableSize() {
        return mPageTableSize / 1024;
    }
    
    public int getOccupiedPageTableSize() {
        return mOccupiedPageTableSize;        
    }

    public HashPageItem getPage(int pid, int address)
    {
        int pageId = searchPage(pid, address);
        return mPageList.get(pageId);
    }

    public HashPageItem getPage(int index) {
        return mPageList.get(index);
    }

    int searchPage(int processId, int virtualAddress)
    {
        int logicPageId = virtualAddress / (mPageSize * 1024);
        int hashCode = mHash.getHash(processId, mPageSize, logicPageId);

        int n = mPageNum;
        int currPageId = hashCode;
        HashPageItem currPage = mPageList.get(currPageId);

        while ((currPage.getPid() != processId || currPage.getLogicPageId() != logicPageId)
                && currPage.getIdleMark() == false && n != 0) {
            n--;
            currPageId = (currPageId + 1) % mPageNum;
            currPage = mPageList.get(currPageId);
        }

        if (currPage.getIdleMark() == true) {
            addPage(currPageId ,processId, logicPageId, currPage, mPageList.get(hashCode));
        } else if (n == 0) {
            currPageId = -1;
        }

        return currPageId;
    }
    
    void addPage(int pageId, int processId, int logicPageId, HashPageItem currPage, HashPageItem hashCodePage)
    {
        hashCodePage.addCollisionCount();
        currPage.setPageId(pageId);
        currPage.setPid(processId);
        currPage.setLogicPageId(logicPageId);
        currPage.setOccupied();

        ++mOccupiedPageNum;
        mOccupiedPageTableSize += mPageItemSize;
    }

    private int mMemorySize = 256;
    private int mPageSize = 1;
    private int mPageItemSize;

    private int mPageNum;
    private int mOccupiedPageNum = 0;
    private int mPageTableSize = 0;
    private int mOccupiedPageTableSize = 0;
    Hash mHash;
    ArrayList<HashPageItem> mPageList;
}

class HashPageItem {
    HashPageItem()
    {

    }

    void setPageId(int pageId)
    {
        mPageId = pageId;
    }

    int getPageId()
    {
        return mPageId;
    }

    void setPid(int processId){
        mProcessId = processId;
    }

    int getPid() {
        return mProcessId;
    }
    
    void setLogicPageId(int logicPageId) {
        mLogicPageId = logicPageId;
    }
    
    int getLogicPageId() {
        return mLogicPageId;
    }
    
    void addCollisionCount() {
        ++mCollisionCount;
    }
    
    void decCollisionCount() {
        --mCollisionCount;
    }
    
    int getCollisionCount() {
        return mCollisionCount;
    }
    
    void setIdle() {
        mIdle = true;
    }
    
    void setOccupied() {
        mIdle = false;
    }

    Boolean getIdleMark() {
        return mIdle;
    }
    
    private int mPageId = 0;
    private int mProcessId = -1;
    private int mLogicPageId = -1;
    private int mCollisionCount = 0;
    private Boolean mIdle = true;
}

class Hash {
    Hash(int mod) {
        mMod = mod;
    }

    public int getHash(int pid, int pageSize, int pageId) {
        return (pid * pageSize * 1024 + pageId) % mMod;
    }
    
    private int mMod;
}
