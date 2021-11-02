package com.ecnu.bussystem.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JSONResult<T> {
    private String code;
    private String msg;
    private T data;

    public JSONResult(T data) {
        this.data = data;
    }

    public static JSONResult success() {
        JSONResult JSONResult = new JSONResult<>();
        JSONResult.setCode("0");
        JSONResult.setMsg("成功");
        return JSONResult;
    }

    public static <T> JSONResult<T> success(T data) {
        JSONResult<T> JSONResult = new JSONResult<>(data);
        JSONResult.setCode("0");
        JSONResult.setMsg("成功");
        return JSONResult;
    }

    public static JSONResult error(String code, String msg) {
        JSONResult JSONResult = new JSONResult();
        JSONResult.setCode(code);
        JSONResult.setMsg(msg);
        return JSONResult;
    }
}
