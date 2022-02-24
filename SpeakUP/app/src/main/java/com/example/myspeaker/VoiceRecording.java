package com.example.myspeaker;

public class VoiceRecording {
    private int recordid;
    private String playText;
    private String filename;

    public int getRecordid() {
        return recordid;
    }

    public void setRecordid(int recordid) {
        this.recordid = recordid;
    }

    public String getPlayText() {
        return playText;
    }

    public void setPlayText(String playText) {
        this.playText = playText;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public String toString() {
        return "VoiceRecording [recordid=" + recordid + ", playText=" + playText
                + ", filename=" + filename + "]";
    }

    public VoiceRecording() {
        super();
        // TODO Auto-generated constructor stub
    }

    public VoiceRecording(String playText, String filename) {
        super();
        this.playText = playText;
        this.filename = filename;
    }


}
