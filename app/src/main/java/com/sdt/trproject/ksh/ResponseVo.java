package com.sdt.trproject.ksh;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.sdt.trproject.ksh.vo.BoardVo;

import java.util.List;


public class ResponseVo<T> {
   @SerializedName("result")
   @Expose
   private String result;
   @SerializedName("message")
   @Expose
   private String message;
   @SerializedName("data")
   @Expose
   private List<T> dataList;


   public ResponseVo(String result) {
      this.result = result;
   }

   public ResponseVo(String result, List<T> dataList) {
      this.result = result;
      this.dataList = dataList;
   }

   public ResponseVo(String result, String message, List<T> dataList) {
      this.result = result;
      this.message = message;
      this.dataList = dataList;
   }

   public String getMessage() {
      return message;
   }

   public void setMessage(String message) {
      this.message = message;
   }

   public String getResult() {
      return result;
   }

   public void setResult(String result) {
      this.result = result;
   }

   public List<T> getDataList() {
      return dataList;
   }

   public void setDataList(List<T> dataList) {
      this.dataList = dataList;
   }
}
