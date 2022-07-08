package com.acme.onlineshop.web;

public enum RESTVersionURL {

    VERSION_01(URL.V_01),
    VERSION_02(URL.V_02),
    VERSION_03(URL.V_03),
    VERSION_04(URL.V_04),
    VERSION_05(URL.V_05),
    VERSION_06(URL.V_06),
    VERSION_07(URL.V_07),
    VERSION_08(URL.V_08),
    VERSION_09(URL.V_09),
    VERSION_10(URL.V_10);

    public final static class URL {
        public final static String V_01 = "/v1";
        public final static String V_02 = "/v2";
        public final static String V_03 = "/v3";
        public final static String V_04 = "/v4";
        public final static String V_05 = "/v5";
        public final static String V_06 = "/v6";
        public final static String V_07 = "/v7";
        public final static String V_08 = "/v8";
        public final static String V_09 = "/v9";
        public final static String V_10 = "/v10";
    }

    public final String url;

    RESTVersionURL(String url) {
        this.url = url;
    }
}
