package com.reliaquest.api.dto;

public class ServerResponse<T> {
    public T data;
    public String status;

    public T getData() { 
        return data; 
    }
    public void setData(T data) { 
        this.data = data; 
    }
    public String getStatus() {
         return status; 
    }
    public void setStatus(String status) { 
        this.status = status; 
    }
}


