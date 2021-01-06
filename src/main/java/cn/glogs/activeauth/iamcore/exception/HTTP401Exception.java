package cn.glogs.activeauth.iamcore.exception;

public class HTTP401Exception extends HTTPException {
    public HTTP401Exception(Throwable cause) {
        super(cause.getMessage(), cause);
    }

    public HTTP401Exception(String s) {
        super(s);
    }
}
