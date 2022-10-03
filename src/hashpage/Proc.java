package hashpage;
public class Proc {
    public Proc(int pid, int procSize, int pageSize)
    {
        mPid = pid;
        mProcSize = procSize;
        mPageNum = procSize / pageSize;
    }

    public int getPid()
    {
        return mPid;
    }

    public int getProcSize()
    {
        return mProcSize;
    }

    public int getPageNum()
    {
        return mPageNum;
    }

    private int mPid;
    private int mProcSize;
    private int mPageNum;
}
