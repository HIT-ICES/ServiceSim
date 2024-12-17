package org.customBuilder.exception;

public class ParserException extends RuntimeException {
    private static final long serialVersionUID = -7864604160297181941L;

    private final Long code;

    public ParserException(final ParserExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
        this.code = exceptionCode.getCode();
    }

    public ParserException(final String message) {
        super(message);
        this.code = ParserExceptionCodeEnum.SPECIFIED.getCode();
    }

    public Long getCode() {
        return code;
    }

}
