package demo.directory.checker;

import java.util.Map;

public class IndexerVerifierDemo {

    public static void main(String[] args) {

        String inputDir = "c:\\ishinka\\cec.dev\\idefix_15.2.2\\jdk1.7.0_60";

        // 1. create index
        Map<String,String> index = directory.checker.createIndex(inputDir);

        System.out.println("index has: " + index.size() + " files");

        // 2. validate index
        boolean valid = directory.checker.isIndexValid(index, inputDir);

        System.out.println("valid: " + valid);

    }
}
