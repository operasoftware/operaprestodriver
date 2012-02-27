package com.opera.core.systems.util;

import com.opera.core.systems.OperaDriver;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import java.util.ArrayList;

public class FileSendKeys {
    private interface MyUser32 extends StdCallLibrary {
        public static final int WM_SETTEXT = 0xc;
        public static final int WM_LBUTTONDOWN= 0x201;        
        public static final int WM_LBUTTONUP = 0x202;
        public static final int IDOK = 1;
        public final MyUser32 INSTANCE = Platform.isWindows() ? ((MyUser32) Native.loadLibrary("user32", MyUser32.class)):null;
        public int SendMessageW(HWND hwnd, int i, int wparam, char[] lparam);
        public HWND GetDlgItem(HWND hDlg, int nIDDlgItem);
    }
    
            
    private static String b2s(char b[]) {
        // Converts C string to Java String
        int len = 0;
        while (b[len] != 0)
            ++len;
        return new String(b, 0, len);
    }
    
    private int operaPID =  0;
    
    public FileSendKeys(OperaDriver operaDriver){
        operaPID = operaDriver.utils().getPID();
    }
            
    private HWND FindOperaOpenDialog(final int pid) {
        final ArrayList<HWND> dialogHandle = new ArrayList<HWND>();
        User32.INSTANCE.EnumWindows(new User32.WNDENUMPROC() {
            @Override
            public boolean callback(HWND hwnd, Pointer pntr) {
                IntByReference windowPid = new IntByReference();
                char[] windowClass = new char[100];
                User32.INSTANCE.GetWindowThreadProcessId(hwnd, windowPid);
                if (pid == windowPid.getValue()) {
                    User32.INSTANCE.GetClassName(hwnd, windowClass, 100);
                    String windowClass_ = b2s(windowClass);
                    boolean b = windowClass_.equals("#32770");
                    if (windowClass_.equals("#32770")) {
                        dialogHandle.add(hwnd);
                    }
                }
                return true;
            }},
        Pointer.NULL);
        switch (dialogHandle.size()) {
            case 0: throw new RuntimeException("Dialog not found");
            case 1: return dialogHandle.get(0);
            default: throw new RuntimeException("Too many dialogs found");
        }
    }

    public void SendKeysFileDialog(String text) {
        if (MyUser32.INSTANCE == null) {
            return;
        }
        HWND dialogHandle = FindOperaOpenDialog(operaPID);
        HWND editHandle = null;
        ArrayList<HWND> comboBoxes = GetChildWindowsByClass(dialogHandle, "ComboBox");
        for (int i=0; i < comboBoxes.size(); i++) {
            ArrayList<HWND> edits = GetChildWindowsByClass(comboBoxes.get(i), "Edit");
                if (edits.size() > 0) {
                    if (edits.size() == 1) {
                        editHandle = edits.get(0);
                        break;
                    } else {
                        throw new RuntimeException("Too many edits found");
                    }
                }
            }
        if (editHandle == null) {
            throw new RuntimeException("Failed to find input element");
        } 
        char[] c_text  = (text + "\0").toCharArray();
        MyUser32.INSTANCE.SendMessageW(editHandle, MyUser32.WM_SETTEXT, 0, c_text);
        
        HWND openButton = MyUser32.INSTANCE.GetDlgItem(dialogHandle, 1);
        if (openButton == null) {
            throw new RuntimeException("Open button not found");
        }
        if (MyUser32.INSTANCE.SendMessageW(openButton, MyUser32.WM_LBUTTONDOWN, 0, null) + 
           MyUser32.INSTANCE.SendMessageW(openButton, MyUser32.WM_LBUTTONUP, 0, null) != 0) {
           throw new RuntimeException("Couldn't click open button");
        }
    }
    
    private ArrayList<HWND> GetChildWindowsByClass(HWND parent,  final String className) {
    final ArrayList<HWND> handles = new ArrayList<HWND>();
    User32.INSTANCE.EnumChildWindows(parent, new User32.WNDENUMPROC() {
            @Override
            public boolean callback(HWND hwnd, Pointer pntr) {
                char[] windowClass = new char[100];
                User32.INSTANCE.GetClassName(hwnd, windowClass, 100);
                if (b2s(windowClass).equals(className)) {
                       handles.add(hwnd);
                    }
                return true;
            }},
        Pointer.NULL);    
    return handles;
    }
}           
