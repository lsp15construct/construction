package com.construction.construction.model;

public class KmBucketSummary {
    private String line;
    private double bucketStartKm;
    private double bucketEndKm;
    private int totalCount;
    private int installedCount;
    private int notInstalledCount;

    // 🧱 Constructors
    public KmBucketSummary(String line, double bucketStartKm, double bucketEndKm, int totalCount, int installedCount, int notInstalledCount) {
        this.line = line;
        this.bucketStartKm = bucketStartKm;
        this.bucketEndKm = bucketEndKm;
        this.totalCount = totalCount;
        this.installedCount = installedCount;
        this.notInstalledCount = notInstalledCount;
    }

    // 🔧 Getters and Setters
    // (Generate these using your IDE)

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public double getBucketStartKm() {
        return bucketStartKm;
    }

    public void setBucketStartKm(double bucketStartKm) {
        this.bucketStartKm = bucketStartKm;
    }

    public double getBucketEndKm() {
        return bucketEndKm;
    }

    public void setBucketEndKm(double bucketEndKm) {
        this.bucketEndKm = bucketEndKm;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getInstalledCount() {
        return installedCount;
    }

    public void setInstalledCount(int installedCount) {
        this.installedCount = installedCount;
    }

    public int getNotInstalledCount() {
        return notInstalledCount;
    }

    public void setNotInstalledCount(int notInstalledCount) {
        this.notInstalledCount = notInstalledCount;
    }
}

