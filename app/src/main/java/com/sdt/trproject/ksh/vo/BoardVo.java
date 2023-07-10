package com.sdt.trproject.ksh.vo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class BoardVo implements Serializable {
    @SerializedName("index")
    @Expose
    private Integer index;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("content")
    @Expose
    private String content;
    @SerializedName("createDate")
    @Expose
    private String createDate;
    @SerializedName("formattedCreatedDate")
    @Expose
    private String formattedCreatedDate;

    @SerializedName("author")
    @Expose
    private Integer author;

    @SerializedName("search")
    @Expose
    private String search;
    @SerializedName("answered")
    @Expose
    private Integer answered;

//    public BoardVo(Integer index, String title, String content, String createDate, Integer author, Integer answered) {
//        this.index = index;
//        this.title = title;
//        this.content = content;
//        this.createDate = createDate;
//        this.author = author;
//        this.answered = answered;
//    }
//
//    public BoardVo(Integer index, String title, String content, String createDate, Integer author, String search) {
//        this.index = index;
//        this.title = title;
//        this.content = content;
//        this.createDate = createDate;
//        this.author = author;
//        this.search = search;
//    }
//
//    public BoardVo(String title, String content) {
//        this.title = title;
//        this.createDate = content;
//    }

    public String getFormattedCreatedDate() {
        return formattedCreatedDate;
    }

    public void setFormattedCreatedDate(String formattedCreatedDate) {
        this.formattedCreatedDate = formattedCreatedDate;
    }
    public Integer getAnswered() {
        return answered;
    }

    public void setAnswered(Integer answered) {
        this.answered = answered;
    }

    public BoardVo(String search) {
        this.search = search;

    }

    public BoardVo(int index) {
        this.index = index;

    }

    public BoardVo() {
    }


    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }


    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public Integer getAuthor() {
        return author;
    }

    public void setAuthor(Integer author) {
        this.author = author;
    }
}
