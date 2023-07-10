package com.sdt.trproject.ksh.vo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AnswerVo {
    @SerializedName("index")
    @Expose
    int index;
    @SerializedName("enquiryIndex")
    @Expose
    int enquiryIndex;
    @SerializedName("answer")
    @Expose
    String answer;
    @SerializedName("formattedCreatedDate")
    @Expose
    String createdDate;
    @SerializedName("formattedUpdatedDate")
    @Expose
    String updatedDate;
    @SerializedName("author")
    @Expose
    Integer author;
    @SerializedName("isDeleted")
    @Expose
    Integer isDeleted;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getEnquiryIndex() {
        return enquiryIndex;
    }

    public void setEnquiryIndex(int enquiryIndex) {
        this.enquiryIndex = enquiryIndex;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Integer getAuthor() {
        return author;
    }

    public void setAuthor(Integer author) {
        this.author = author;
    }

    public int getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(int isDeleted) {
        this.isDeleted = isDeleted;
    }
}
