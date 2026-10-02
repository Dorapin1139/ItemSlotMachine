package com.darkblade12.itemslotmachine.design;

// 設置する範囲に空きがないとき(想定内の失敗)に投げる。呼び出し側はスタックトレースを出さない
public class DesignSpaceException extends DesignBuildException {
    private static final long serialVersionUID = 1L;

    public DesignSpaceException(String message) {
        super(message);
    }
}
