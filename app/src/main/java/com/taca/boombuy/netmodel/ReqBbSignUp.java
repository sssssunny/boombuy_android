package com.taca.boombuy.netmodel;

/**
 * Created by jimin on 2017-02-13.
 */

public class ReqBbSignUp {
    ReqHeader header;
    SignUpModel body;

    public ReqHeader getHeader() {
        return header;
    }

    public void setHeader(ReqHeader header) {
        this.header = header;
    }

    public SignUpModel getBody() {
        return body;
    }

    public void setBody(SignUpModel body) {
        this.body = body;
    }
}
