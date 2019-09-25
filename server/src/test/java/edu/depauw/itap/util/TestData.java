package edu.depauw.itap.util;

public class TestData {
        public static final String VALID_SOURCE = "public class Test {\n"
                        + "public static void main(String[] args) {\n"
                        + "    System.out.println(\"Hello World!\");\n" + " }\n" + "}\n";

        public static final String FOR_LOOP_SOURCE = "public class Test {\n"
                        + "public static void main(String[] args) {\n"
                        + "    for(int i = 0; i < 5; i++) {\n" + "        System.out.println(i);\n"
                        + "    }\n" + " }\n" + "}\n";

        public static final String INVALID_SOURCE = "public class Test {\n"
                        + "public static void main(String[] args) {\n"
                        + "    NOT_VALID_FUNCTION(\"Hello World!\");\n" + " }\n" + "}\n";

        public static final String VALID_SOURCE_WITH_PACKAGE =
                        "package test.moretest;\n" + VALID_SOURCE;
}
