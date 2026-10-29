package com.library.lms.ai;

public final class ChatContext {

    private static final ThreadLocal<String> CURRENT_MEMBER = new InheritableThreadLocal<>();

    private ChatContext() {
    }

    public static void setMemberRef(String memberRef) {
        CURRENT_MEMBER.set(memberRef);
    }

    public static String memberRef() {
        return CURRENT_MEMBER.get();
    }

    public static void clear() {
        CURRENT_MEMBER.remove();
    }
}
