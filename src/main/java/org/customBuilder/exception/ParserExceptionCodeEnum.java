package org.customBuilder.exception;

public enum ParserExceptionCodeEnum implements ParserExceptionCode {
    SPECIFIED(000_000_000_000L, "系统发生异常,请稍后重试");

    private final Long code;

    private final String message;

    ParserExceptionCodeEnum(Long code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Long getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
