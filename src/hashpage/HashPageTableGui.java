package hashpage;

import java.util.ArrayList;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowListener;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.Font;

public class HashPageTableGui {
    class MemButtonListener implements ActionListener {
        MemButtonListener(JRadioButton memButton, int memButtonSize) {
            mMemButton = memButton;
            mMemButtonSize = memButtonSize;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (mMemButton.isSelected()) {
                mMemSize = mMemButtonSize;
                mMemSelected = true;

                if (mMemSelected && mPageSelected) {
                    mConfirmButton.setEnabled(true);
                }
            }
        }

        private JRadioButton mMemButton;
        private int mMemButtonSize;
    }

    class PageButtonListener implements ActionListener {
        PageButtonListener(JRadioButton pageButton, int pageButtonSize) {
            mPageButton = pageButton;
            mPageButtonSize = pageButtonSize;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (mPageButton.isSelected()) {
                mPageSize = mPageButtonSize;
                mPageSelected = true;

                if (mMemSelected && mPageSelected) {
                    mConfirmButton.setEnabled(true);
                }
            }
        }

        private JRadioButton mPageButton;
        private int mPageButtonSize;
    }

    class ConfirmButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            initPageTable();
            initProcs();

            mProcViewPanel.flush();
            for (int i = 0; i < mProcs.size(); ++i) {
                mProcViewPanel.setOccupied(i);
            }
            mProcViewPanel.repaint();

            mPageNum.setText("页框数:" + String.valueOf(mPageTable.getPageNum()));
            mOccupiedPageNum.setText("已分配页框数:" + String.valueOf(mPageTable.getOccupiedPageNum()));
            mHashPageTableSize
                    .setText("倒置页表所占空间:" + String.valueOf(mPageTable.getPageTableSize()) + "KB");
            mOccupiedHashPageTableSize
                    .setText("倒置页表已用空间:" + String.valueOf(mPageTable.getOccupiedPageTableSize()) + "B");
            mQueryTableButton.setEnabled(true);
        }
    }

    class QueryTableButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            mQueryTableDialog.setVisible(true);
        }
    }

    class QueryButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                mQueryPage = Integer.valueOf(mQueryText.getText());
            } catch (NumberFormatException ex) {
                return;
            }

            if (mQueryPage >= mPageTable.getPageNum()) {
                return;
            }

            HashPageItem pageItem = mPageTable.getPage(mQueryPage);
            mQueryPid.setText("进程号:" + String.valueOf(pageItem.getPid()));
            mQueryLogicPageId.setText("逻辑页号:" + String.valueOf(pageItem.getLogicPageId()));
            mQueryCollisionCount.setText("冲突计数:" + String.valueOf(pageItem.getCollisionCount()));
            mQueryIdle.setText("空闲标志:" + String.valueOf(pageItem.getIdleMark()));
        }
    }

    class QueryWindowListener implements WindowListener {
        @Override
        public void windowClosing(WindowEvent e) {
            e.getWindow().setVisible(false);
            e.getWindow().dispose();

            mQueryText.setText(null);
            mQueryPid.setText("进程号:");
            mQueryLogicPageId.setText("逻辑页号:");
            mQueryCollisionCount.setText("冲突计数:");
            mQueryIdle.setText("空闲标志:");
        }

        @Override
        public void windowClosed(WindowEvent e) {

        }

        @Override
        public void windowOpened(WindowEvent e) {

        }

        @Override
        public void windowDeiconified(WindowEvent e) {

        }

        @Override
        public void windowActivated(WindowEvent e) {

        }

        @Override
        public void windowDeactivated(WindowEvent e) {

        }

        @Override
        public void windowIconified(WindowEvent e) {

        }
    }

    class ProcButtonListener implements ActionListener {
        public String to16BitHexString(int num)
        {
            String hexStr = Integer.toHexString(num);

            while (hexStr.length() < 4) {
                hexStr = '0' + hexStr;
            }

            return hexStr;
        }

        public String to32BitHexString(int num)
        {
            String hexStr = Integer.toHexString(num);

            while (hexStr.length() < 8) {
                hexStr = '0' + hexStr;
            }

            return hexStr;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int pos = mProcViewPanel.getClickedPos();

            if (pos != -1) {
                Random rand = new Random(System.currentTimeMillis());
                int address = rand.nextInt(0, mProcs.get(pos).getProcSize() * 1024);
                int offset = address % (mPageSize * 1024);
                HashPageItem pageItem = mPageTable.getPage(mProcs.get(pos).getPid(), address);

                mPid.setText("进程号:" + String.valueOf(pageItem.getPid()));
                mVirtualAddress.setText("虚拟地址:0x" + to16BitHexString(address));
                mLogicPageId.setText("逻辑页号:" + String.valueOf(pageItem.getLogicPageId()));
                mOffset.setText("页内偏移:0x" + to16BitHexString(offset));
                mPhysicalPageId.setText("物理页框:" + String.valueOf(pageItem.getPageId()));
                mPhysicalAddress
                        .setText("物理地址:0x" + to32BitHexString(pageItem.getPageId() * mPageSize * 1024 + offset));
                mCollision.setText("冲突计数:" + String.valueOf(pageItem.getCollisionCount()));
                mIdle.setText("空闲标志:" + String.valueOf(pageItem.getIdleMark()));
            }
        }
    }

    class ProcWindowListener implements WindowListener {
        @Override
        public void windowClosing(WindowEvent e) {
            mProcViewPanel.setMouseClicked(-1);
            mProcViewPanel.repaint();

            e.getWindow().setVisible(false);
            e.getWindow().dispose();

            mPid.setText("进程号:");
            mVirtualAddress.setText("虚拟地址:");
            mLogicPageId.setText("逻辑页号:");
            mOffset.setText("页内偏移:");
            mPhysicalPageId.setText("物理页框:");
            mPhysicalAddress.setText("物理地址:");
            mCollision.setText("冲突计数:");
            mIdle.setText("空闲标志:");

            mPageNum.setText("页框数:" + String.valueOf(mPageTable.getPageNum()));
            mOccupiedPageNum.setText("已分配页框数:" + String.valueOf(mPageTable.getOccupiedPageNum()));
            mHashPageTableSize
                    .setText("倒置页表所占空间:" + String.valueOf(mPageTable.getPageTableSize() + "KB"));
            mOccupiedHashPageTableSize
                    .setText("倒置页表已用空间:" + String.valueOf(mPageTable.getOccupiedPageTableSize()) + "B");
        }

        @Override
        public void windowClosed(WindowEvent e) {

        }

        @Override
        public void windowOpened(WindowEvent e) {

        }

        @Override
        public void windowDeiconified(WindowEvent e) {

        }

        @Override
        public void windowActivated(WindowEvent e) {

        }

        @Override
        public void windowDeactivated(WindowEvent e) {

        }

        @Override
        public void windowIconified(WindowEvent e) {

        }
    }

    class GridPanel extends JPanel implements MouseListener, MouseMotionListener {
        public GridPanel() {
            for (int i = 0; i < 36; ++i) {
                mGridOccupied.add(false);
            }
        }

        public int getClickedPos() {
            return mMouseClickedGridPos;
        }

        public void paint(Graphics g) {
            super.paint(g);
            Graphics2D g2d = (Graphics2D) g.create();

            int width = getWidth() / 6;
            for (int i = 0; i < 36; ++i) {
                int row = i % 6;
                int col = i / 6;

                if (mGridOccupied.get(i) == true) {
                    if (mMouseClickedGridPos == i) {
                        g2d.setColor(mClickedColor);
                    } else if (mMouseHoverGridPos == i) {
                        g2d.setColor(mHoverColor);
                    } else {
                        g2d.setColor(mNormalColor);
                    }

                    g2d.fillRoundRect(row * width + 5, col * width + 5, width - 10, width - 10, 10, 10);

                    int pid = mProcs.get(i).getPid();
                    int procSize = mProcs.get(i).getProcSize();

                    g2d.setFont(mTextFont);
                    g2d.setColor(mProcColor);
                    g2d.drawString("进程 " + String.valueOf(pid), row * width + 10, col * width + 4 * width / 10);
                    g2d.drawString(String.valueOf(procSize) + "KB", row * width + 10, col * width + 8 * width / 10);
                }
                else {
                    g2d.setColor(mNullColor);
                    g2d.fillRoundRect(row * width + 5, col * width + 5, width - 10, width - 10, 10, 10);
                }

                g2d.setColor(Color.BLACK);
                g2d.drawRoundRect(row * width + 5, col * width + 5, width - 10, width - 10, 10, 10);

            }

            g2d.dispose();
        }

        public void flush() {
            for (int i = 0; i < 36; ++i) {
                mGridOccupied.set(i, false);
            }
        }

        public void setOccupied(int index) {
            mGridOccupied.set(index, true);
        }

        private Boolean inGrid(int x, int y, int row, int col) {
            int width = getWidth() / 6;

            if (x >= col * width + 5 && x <= (col + 1) * width - 5 && y >= row * width + 5
                    && y <= (row + 1) * width - 5) {
                return true;
            }

            return false;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            int width = getWidth() / 6;
            int row = e.getY() / width;
            int col = e.getX() / width;

            if (inGrid(e.getX(), e.getY(), row, col)) {
                mMouseClickedGridPos = row * 6 + col;
            } else {
                mMouseClickedGridPos = -1;
            }

            repaint();
            mProcDialog.setVisible(true);
        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (mProcDialog.isVisible() == false) {
                mMouseClickedGridPos = -1;
                mMouseHoverGridPos = -1;
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {

        }

        @Override
        public void mouseMoved(MouseEvent e) {
            int width = getWidth() / 6;
            int row = e.getY() / width;
            int col = e.getX() / width;

            if (inGrid(e.getX(), e.getY(), row, col)) {
                mMouseHoverGridPos = row * 6 + col;
            } else {
                mMouseHoverGridPos = -1;
            }

            repaint();
        }

        public void setMouseClicked(int value) {
            mMouseClickedGridPos = value;
        }

        private ArrayList<Boolean> mGridOccupied = new ArrayList<Boolean>(36);
        private int mMouseClickedGridPos = -1;
        private int mMouseHoverGridPos = -1;
    }

    public void init(int width, int height) {
        mFrame = new JFrame();
        mFrame.setSize(1024, 768);
        mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mFrame.setLayout(null);
        mFrame.getContentPane().setBackground(mBgColor);

        initTitlePanel();
        initSelPanel();
        initProcViewPanel();
        initMemViewPanel();

        mFrame.setVisible(true);
    }

    void initTitlePanel() {
        mTitlePanel = new JPanel();
        mTitlePanel.setLayout(null);
        mTitlePanel.setBounds(0, 0, mFrame.getWidth(), mFrame.getHeight() / 10);
        mTitlePanel.setBackground(mBgColor);

        Font titleFont = new Font(mTextFont.getFamily(), mTextFont.getStyle(), 35);

        JLabel titleLabel = new JLabel("基于杂凑技术的反置页表方法页式内存管理的模拟实现", JLabel.CENTER);
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(mFontColor);
        titleLabel.setBounds(0, 0, mFrame.getWidth(), mFrame.getHeight() / 10);
        titleLabel.setVerticalTextPosition(JLabel.CENTER);
        titleLabel.setHorizontalTextPosition(JLabel.CENTER);

        mTitlePanel.add(titleLabel);
        mFrame.add(mTitlePanel);
    }

    void initProcViewPanel() {
        mProcViewPanel = new GridPanel();
        mProcViewPanel.setBounds(mFrame.getWidth() / 15, mTitlePanel.getHeight() + mFrame.getHeight() / 20,
                600, 600);
        mProcViewPanel.addMouseListener(mProcViewPanel);
        mProcViewPanel.addMouseMotionListener(mProcViewPanel);
        mProcViewPanel.setBackground(mBgColor);

        mProcDialog = new JDialog();
        mProcDialog.getContentPane().setBackground(mBgColor);
        mProcDialog.setBounds(mFrame.getWidth() / 2 - 170,
                mFrame.getHeight() / 2 - 130, 340, 260);
        mProcDialog.setLayout(null);
        mProcDialog.addWindowListener(new ProcWindowListener());
        mProcDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JButton procButton = new JButton("访问内存");
        procButton.setFont(mTextFont);
        procButton.setForeground(mButtonFontColor);
        procButton.setBounds(mProcDialog.getWidth() / 2 - 50, mProcDialog.getHeight() - 80, 100, 30);
        procButton.addActionListener(new ProcButtonListener());

        int labelWidth = mProcDialog.getWidth() / 2;
        int labelHeight = procButton.getY() / 4;

        mPid = new JLabel("进程号:");
        mPid.setFont(mTextFont);
        mPid.setForeground(mFontColor);
        mPid.setBackground(mBgColor);
        mPid.setBounds(5, 0, labelWidth-10, labelHeight);

        mVirtualAddress = new JLabel("虚拟地址:");
        mVirtualAddress.setFont(mTextFont);
        mVirtualAddress.setForeground(mFontColor);
        mVirtualAddress.setBackground(mBgColor);
        mVirtualAddress.setBounds(labelWidth+5, 0, labelWidth-10, labelHeight);

        mLogicPageId = new JLabel("逻辑页号:");
        mLogicPageId.setFont(mTextFont);
        mLogicPageId.setForeground(mFontColor);
        mLogicPageId.setBackground(mBgColor);
        mLogicPageId.setBounds(5, labelHeight, labelWidth-10, labelHeight);

        mOffset = new JLabel("页内偏移:");
        mOffset.setFont(mTextFont);
        mOffset.setForeground(mFontColor);
        mOffset.setBackground(mBgColor);
        mOffset.setBounds(labelWidth+5, labelHeight, labelWidth-10, labelHeight);

        mPhysicalPageId = new JLabel("物理页框:");
        mPhysicalPageId.setFont(mTextFont);
        mPhysicalPageId.setForeground(mFontColor);
        mPhysicalPageId.setBackground(mBgColor);
        mPhysicalPageId.setBounds(5, labelHeight * 2, labelWidth-10, labelHeight);

        mPhysicalAddress = new JLabel("物理地址:");
        mPhysicalAddress.setFont(mTextFont);
        mPhysicalAddress.setForeground(mFontColor);
        mPhysicalAddress.setBackground(mBgColor);
        mPhysicalAddress.setBounds(labelWidth+5, labelHeight * 2, labelWidth-10, labelHeight);

        mCollision = new JLabel("冲突计数");
        mCollision.setFont(mTextFont);
        mCollision.setForeground(mFontColor);
        mCollision.setBackground(mBgColor);
        mCollision.setBounds(5, labelHeight * 3, labelWidth-10, labelHeight);

        mIdle = new JLabel("空闲标志:");
        mIdle.setFont(mTextFont);
        mIdle.setForeground(mFontColor);
        mIdle.setBackground(mBgColor);
        mIdle.setBounds(labelWidth+5, labelHeight * 3, labelWidth-10, labelHeight);

        mProcDialog.add(procButton);
        mProcDialog.add(mPid);
        mProcDialog.add(mVirtualAddress);
        mProcDialog.add(mLogicPageId);
        mProcDialog.add(mOffset);
        mProcDialog.add(mPhysicalPageId);
        mProcDialog.add(mPhysicalAddress);
        mProcDialog.add(mCollision);
        mProcDialog.add(mIdle);

        mFrame.add(mProcViewPanel);
    }

    void initSelPanel() {
        mSelPanel = new JPanel();
        mSelPanel.setBounds(2 * mFrame.getWidth() / 3, mTitlePanel.getHeight() + mFrame.getHeight() / 20,
                mFrame.getWidth() / 3 - 20,
                mFrame.getHeight() / 4);
        mSelPanel.setLayout(null);
        mSelPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        mSelPanel.setBackground(mFrameColor);

        mConfirmButton = new JButton("生成进程");
        mConfirmButton.setFont(mTextFont);
        mConfirmButton.setForeground(mButtonFontColor);
        mConfirmButton.addActionListener(new ConfirmButtonListener());
        mConfirmButton.setEnabled(false);

        ButtonGroup memSelBox = new ButtonGroup();

        JRadioButton memSel256 = new JRadioButton("256MB");
        memSel256.setFont(mTextFont);
        memSel256.setForeground(mFrameFontColor);
        memSel256.addActionListener(new MemButtonListener(memSel256, 256));
        memSel256.setBackground(mFrameColor);

        JRadioButton memSel512 = new JRadioButton("512MB");
        memSel512.setFont(mTextFont);
        memSel512.setForeground(mFrameFontColor);
        memSel512.addActionListener(new MemButtonListener(memSel512, 512));
        memSel512.setBackground(mFrameColor);

        memSelBox.add(memSel256);
        memSelBox.add(memSel512);

        ButtonGroup pageSelBox = new ButtonGroup();

        JRadioButton pageSel1 = new JRadioButton("1KB");
        pageSel1.setFont(mTextFont);
        pageSel1.setForeground(mFrameFontColor);
        pageSel1.addActionListener(new PageButtonListener(pageSel1, 1));
        pageSel1.setBackground(mFrameColor);

        JRadioButton pageSel2 = new JRadioButton("2KB");
        pageSel2.setFont(mTextFont);
        pageSel2.setForeground(mFrameFontColor);
        pageSel2.addActionListener(new PageButtonListener(pageSel2, 2));
        pageSel2.setBackground(mFrameColor);

        JRadioButton pageSel4 = new JRadioButton("4KB");
        pageSel4.setFont(mTextFont);
        pageSel4.setForeground(mFrameFontColor);
        pageSel4.addActionListener(new PageButtonListener(pageSel4, 4));
        pageSel4.setBackground(mFrameColor);

        pageSelBox.add(pageSel1);
        pageSelBox.add(pageSel2);
        pageSelBox.add(pageSel4);

        JLabel memSelLabel = new JLabel("请选择物理内存空间大小", JLabel.CENTER);
        memSelLabel.setFont(mTextFont);
        memSelLabel.setForeground(mFrameFontColor);
        memSelLabel.setHorizontalTextPosition(JLabel.CENTER);
        memSelLabel.setVerticalTextPosition(JLabel.CENTER);
        memSelLabel.setBackground(mFrameColor);
        memSelLabel.setBounds(5, 5, mSelPanel.getWidth() - 10, mSelPanel.getHeight() / 5 - 10);

        JPanel memSelPanel = new JPanel();
        memSelPanel.add(memSel256);
        memSelPanel.add(memSel512);
        memSelPanel.setBounds(5, mSelPanel.getHeight() / 5 + 5, mSelPanel.getWidth() - 10,
                mSelPanel.getHeight() / 5 - 10);
        memSelPanel.setBackground(mFrameColor);

        JLabel pageSelLabel = new JLabel("请选择页框大小", JLabel.CENTER);
        pageSelLabel.setFont(mTextFont);
        pageSelLabel.setForeground(mFrameFontColor);
        pageSelLabel.setVerticalTextPosition(JLabel.CENTER);
        pageSelLabel.setHorizontalTextPosition(JLabel.CENTER);
        pageSelLabel.setBounds(5, 2 * mSelPanel.getHeight() / 5 + 5, mSelPanel.getWidth() - 10,
                mSelPanel.getHeight() / 5 - 10);
        pageSelLabel.setBackground(mFrameColor);

        JPanel pageSelPanel = new JPanel();
        pageSelPanel.add(pageSel1);
        pageSelPanel.add(pageSel2);
        pageSelPanel.add(pageSel4);
        pageSelPanel.setBounds(5, 3 * mSelPanel.getHeight() / 5 + 5, mSelPanel.getWidth() - 10,
                mSelPanel.getHeight() / 5 - 10);
        pageSelPanel.setBackground(mFrameColor);

        mConfirmButton.setBounds(mSelPanel.getWidth() / 2 - 50, mSelPanel.getHeight() - 40, 100, 30);

        mSelPanel.add(memSelLabel);
        mSelPanel.add(memSelPanel);
        mSelPanel.add(pageSelLabel);
        mSelPanel.add(pageSelPanel);
        mSelPanel.add(mConfirmButton);

        mFrame.add(mSelPanel);
    }

    void initMemViewPanel() {
        mMemViewPanel = new JPanel();
        mMemViewPanel.setBounds(mSelPanel.getX(), mSelPanel.getY() + mSelPanel.getHeight() + 20, mSelPanel.getWidth(),
                mProcViewPanel.getHeight() - mSelPanel.getHeight() - 20);
        mMemViewPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        mMemViewPanel.setLayout(null);
        mMemViewPanel.setBackground(mFrameColor);

        int labelWidth = mMemViewPanel.getWidth();
        int labelHeight = mMemViewPanel.getHeight() / 5;

        mPageNum = new JLabel("页框数:");
        mPageNum.setFont(mTextFont);
        mPageNum.setForeground(mFrameFontColor);
        mPageNum.setBounds(5, 0, labelWidth, labelHeight);

        mOccupiedPageNum = new JLabel("已分配页框数:");
        mOccupiedPageNum.setFont(mTextFont);
        mOccupiedPageNum.setForeground(mFrameFontColor);
        mOccupiedPageNum.setBounds(5, labelHeight, labelWidth, labelHeight);

        mHashPageTableSize = new JLabel("倒置页表所占空间:");
        mHashPageTableSize.setFont(mTextFont);
        mHashPageTableSize.setForeground(mFrameFontColor);
        mHashPageTableSize.setBounds(5, labelHeight * 2, labelWidth, labelHeight);

        mOccupiedHashPageTableSize = new JLabel("倒置页表已用空间:");
        mOccupiedHashPageTableSize.setFont(mTextFont);
        mOccupiedHashPageTableSize.setForeground(mFrameFontColor);
        mOccupiedHashPageTableSize.setBounds(5, labelHeight * 3, labelWidth, labelHeight);

        mQueryTableButton = new JButton("查询倒置页表");
        mQueryTableButton.setFont(mTextFont);
        mQueryTableButton.setForeground(mButtonFontColor);
        mQueryTableButton.setBounds(mMemViewPanel.getWidth() / 2 - 60, mMemViewPanel.getHeight() - 35, 120, 30);
        mQueryTableButton.setEnabled(false);
        mQueryTableButton.addActionListener(new QueryTableButtonListener());

        mMemViewPanel.add(mPageNum);
        mMemViewPanel.add(mOccupiedPageNum);
        mMemViewPanel.add(mHashPageTableSize);
        mMemViewPanel.add(mOccupiedHashPageTableSize);
        mMemViewPanel.add(mQueryTableButton);

        mQueryTableDialog = new JDialog();
        mQueryTableDialog.getContentPane().setBackground(mBgColor);
        mQueryTableDialog.setBounds(mFrame.getWidth() / 2 - 150,
                mFrame.getHeight() / 2 - 120, 300, 240);
        mQueryTableDialog.setLayout(null);
        mQueryTableDialog.addWindowListener(new QueryWindowListener());

        int width = mQueryTableDialog.getWidth() / 2;
        int height = mQueryTableDialog.getHeight() / 4;

        mQueryText = new JTextField(1);
        mQueryText.setBounds(20, 20, width - 40, height - 40);

        mQueryButton = new JButton("查询");
        mQueryButton.setFont(mTextFont);
        mQueryButton.setForeground(mButtonFontColor);
        mQueryButton.setBounds(width + width / 2 - 40, 20, 80, height - 40);
        mQueryButton.addActionListener(new QueryButtonListener());

        mQueryPid = new JLabel("进程号:");
        mQueryPid.setFont(mTextFont);
        mQueryPid.setForeground(mFontColor);
        mQueryPid.setBackground(mBgColor);
        mQueryPid.setBounds(5, height, width-10, height);

        mQueryLogicPageId = new JLabel("逻辑页号:");
        mQueryLogicPageId.setFont(mTextFont);
        mQueryLogicPageId.setForeground(mFontColor);
        mQueryLogicPageId.setBackground(mBgColor);
        mQueryLogicPageId.setBounds(width+5, height, width-10, height);

        mQueryCollisionCount = new JLabel("冲突计数:");
        mQueryCollisionCount.setFont(mTextFont);
        mQueryCollisionCount.setForeground(mFontColor);
        mQueryCollisionCount.setBackground(mBgColor);
        mQueryCollisionCount.setBounds(5, height * 2, width-10, height);

        mQueryIdle = new JLabel("空闲标志:");
        mQueryIdle.setFont(mTextFont);
        mQueryIdle.setForeground(mFontColor);
        mQueryIdle.setBackground(mBgColor);
        mQueryIdle.setBounds(width + 5, height * 2, width-10, height);

        mQueryTableDialog.add(mQueryText);
        mQueryTableDialog.add(mQueryButton);
        mQueryTableDialog.add(mQueryPid);
        mQueryTableDialog.add(mQueryLogicPageId);
        mQueryTableDialog.add(mQueryCollisionCount);
        mQueryTableDialog.add(mQueryIdle);

        mFrame.add(mMemViewPanel);
    }

    void initPageTable() {
        mPageTable = new HashPageTable(mMemSize, mPageSize);
    }

    void initProcs() {
        Random rand = new Random(System.currentTimeMillis());
        mProcs = new ArrayList<Proc>(36);
        int pageCnt = 0;

        for (int pid = 0; pid < rand.nextInt(4, 36); ++pid) {
            Proc proc = new Proc(rand.nextInt(mPageTable.getPageNum() / 4),
                    mPageSize * rand.nextInt(4, 64 / mPageSize + 1), mPageSize);
            pageCnt += proc.getPageNum();

            if (pageCnt <= mPageTable.getPageNum()) {
                mProcs.add(proc);
            } else {
                break;
            }
        }
    }

    JFrame mFrame;
    Font mTextFont = new Font("Noto Sans CJK SC", Font.BOLD, 14);

    Color mBgColor = new Color(255, 255, 255);
    Color mFontColor = new Color(35, 35, 90);
    Color mFrameColor = new Color(0x57, 0x66, 0x90);
    Color mFrameFontColor = new Color(240, 255, 255);
    Color mButtonFontColor = new Color(88, 122, 204);

    Color mNormalColor = new Color(0x9a, 0xc8, 0xe2);
    Color mHoverColor = new Color(0xe7, 0x99, 0xb0);
    Color mClickedColor = new Color(0xdb, 0x7d, 0x74);
    Color mNullColor = new Color(230, 230, 230);
    Color mProcColor = new Color(35, 35, 90);

    JPanel mTitlePanel;
    GridPanel mProcViewPanel;
    JPanel mSelPanel;
    JButton mConfirmButton;

    JPanel mMemViewPanel;
    JLabel mPageNum;
    JLabel mOccupiedPageNum;
    JLabel mHashPageTableSize;
    JLabel mOccupiedHashPageTableSize;
    JButton mQueryTableButton;

    JDialog mQueryTableDialog;
    JTextField mQueryText;
    JButton mQueryButton;
    JLabel mQueryPid;
    JLabel mQueryLogicPageId;
    JLabel mQueryCollisionCount;
    JLabel mQueryIdle;
    int mQueryPage;

    JDialog mProcDialog;
    JPanel mPageItemPanel;
    JLabel mPid;
    JLabel mVirtualAddress;
    JLabel mLogicPageId;
    JLabel mOffset;
    JLabel mPhysicalPageId;
    JLabel mPhysicalAddress;
    JLabel mCollision;
    JLabel mIdle;

    int mOldX;
    int mOldY;

    int mMemSize;
    int mPageSize;
    Boolean mMemSelected = false;
    Boolean mPageSelected = false;

    HashPageTable mPageTable;
    ArrayList<Proc> mProcs;
}
