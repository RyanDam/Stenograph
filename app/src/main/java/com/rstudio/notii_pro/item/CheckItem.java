package com.rstudio.notii_pro.item;

/**
 * Created by Ryan on 8/11/15.
 */
public class CheckItem {

    private boolean isCheck;
    private String text;
    private int ID;
    private int keyNote;
    private int keyOrder;

    public int getID() {
        return ID;
    }

    public CheckItem(int ID, boolean isCheck, String text, int keyNote, int keyOrder) {
        this.isCheck = isCheck;
        this.text = text;
        this.ID = ID;
        this.keyNote = keyNote;
        this.keyOrder = keyOrder;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getKeyNote() {
        return keyNote;
    }

    public void setKeyNote(int keyNote) {
        this.keyNote = keyNote;
    }

    public int getKeyOrder() {
        return keyOrder;
    }

    public void setKeyOrder(int keyOrder) {
        this.keyOrder = keyOrder;
    }

    public CheckItem() {
        isCheck = false;
        text = null;
    }

    public CheckItem(boolean isCheck, String text, int key, int order) {
        this.isCheck = isCheck;
        this.text = text;
        this.ID = -1;
        this.keyNote = key;
        this.keyOrder = order;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setIsCheck(boolean isCheck) {
        this.isCheck = isCheck;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void toggle() {
        isCheck = !isCheck;
    }
}
