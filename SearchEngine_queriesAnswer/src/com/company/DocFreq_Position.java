package com.company;

public class DocFreq_Position {
    int DocumentFreq;
    Long position;

    public int getDocumentFreq() {
        return DocumentFreq;
    }

    public void setDocumentFreq(int documentFreq) {
        DocumentFreq = documentFreq;
    }

    public Long getPosition() {
        return position;
    }

    public void setPosition(Long position) {
        this.position = position;
    }

    public DocFreq_Position(int documentFreq, Long position) {
        DocumentFreq = documentFreq;
        this.position = position;
    }

    public DocFreq_Position() {
    }
}
