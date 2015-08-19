package com.rstudio.notii_pro.item;

public class NoteItem {
	private String title, text, date;
	private int color, id = 0;
	private long remind = 0;
	private boolean bold;
	private boolean isCheckList;
	public NoteItem(){
		this.title = "";
		this.text = "";
		this.date = "";
		this.bold = false;
		this.isCheckList = false;
	}
	public NoteItem(String title, String text, String date, int color, boolean bold) {
		this.title = title;
		this.text = text;
		this.date = date;
		this.color = color;
		this.bold = bold;
	}
	public NoteItem(int id, String title, String text, String date, int color, boolean bold) {
		this.title = title;
		this.text = text;
		this.date = date;
		this.color = color;
		this.bold = bold;
		this.id = id;
	}
    public NoteItem(String input) {
        String x = null;
        int step = 1;
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == ' ' || i == input.length() - 1) {
                switch (step) {
                    case 1:
                        id = Integer.parseInt(x);
                        break;
                    case 2:
                        title = x;
                        break;
                    case 3:
                        text = x;
                        break;
                    case 4:
                        date = x;
                        break;
                    case 5:
                        color = Integer.parseInt(x);
                        break;
                    case 6:
                        bold = Boolean.parseBoolean(x);
                        break;
                    case 7:
                        remind = Long.parseLong(x);
                        break;
                }
                x = null;
                step ++;
            }
            else {
                x = x + input.charAt(i);
            }
        }
    }
    public String writeNoteData() {
        String output = id + " " + title + " " + text + " " + date + " " + color + " " + bold + " " + remind;
        return output;
    }
	public void setRemind(long remind){
		this.remind = remind;
	}
	public long getRemind(){
		return this.remind;
	}
	public void setId(int id){
		this.id = id;
	}
	public int getId(){
		return this.id;
	}
	public void setTitle(String title){
		this.title = title;
	}	
	public void setDate(String date){
		this.date = date;
	}
	public void setText(String text){
		this.text = text;
	}
	public String getTitle(){
		return this.title;
	}
	public String getText(){
		return this.text;
	}
	public String getDate(){
		return this.date;
	}
	public void setColor(int color){
		this.color = color;
	}	
	public int getColor(){
		return this.color;
	}
	public void setBold(boolean bold){
		this.bold = bold;
	}
	public boolean getBold(){
		return this.bold;
	}
	public boolean isCheckList() {
		return isCheckList;
	}
	public void setIsCheckList(boolean isCheckList) {
		this.isCheckList = isCheckList;
	}
	public boolean isBold() {
		return bold;
	}
}
