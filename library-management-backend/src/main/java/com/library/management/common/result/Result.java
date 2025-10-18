package com.library.management.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 统一返回结果封装类
 *
 * 作用：规范所有API接口的响应格式
 *
 * 使用场景：
 * 1. Controller层所有接口都应该返回Result对象
 * 2. 通过静态方法快速创建成功/失败响应
 *
 * @param <T> 泛型，表示返回的数据类型
 */
@Data
@NoArgsConstructor  // Lombok注解：生成无参构造函数
@AllArgsConstructor  // Lombok注解：生成包含所有字段的构造函数
public class Result<T> implements Serializable {

    /**
     * 序列化版本号
     * 说明：实现Serializable接口后需要这个字段，用于版本控制
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private Integer code;
    private String message;
    private T data;
    private LocalDateTime timestamp;


    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setTimestamp(LocalDateTime.now());
        return result;
    }


    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }

    public static <T> Result<T> success(String message, T data)
    {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }


    public static <T> Result<T> fail(String message) {
        Result<T> result = new Result<>();
        result.setCode(400);
        result.setMessage(message);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }


    public static <T> Result<T> fail(Integer code, String
            message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }
}
