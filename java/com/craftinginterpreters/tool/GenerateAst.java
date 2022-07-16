package com.craftinginterpreters.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
                "Binary   : Expr left, Token operator, Expr right",
                "Grouping : Expr expression",
                "Literal  : Object value",
                "Unary    : Token operator, Expr right"
        ));
    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8);

        writer.println("package com.craftinginterpreters.lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + "{");

        defineVisitor(writer, baseName, types);

        // The AST classes.
        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
            writer.println();
        }

        // The base accept() method.
        writer.println();
        writer.println("    abstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.close();
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        final int TAB = 4;
        int spaceCount = TAB;
        writer.println(" ".repeat(spaceCount) + "interface Visitor<R> {");

        spaceCount += TAB;
        for (String type: types) {
            String typeName = type.split(":")[0].trim();
            writer.println(" ".repeat(spaceCount) + "R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
        }

        spaceCount -= TAB;
        writer.println(" ".repeat(spaceCount) + "}");
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        final int TAB = 4;
        int spaceCount = TAB;
        writer.println(" ".repeat(spaceCount) + "static class " + className + " extends " + baseName + "{");

        spaceCount += TAB;
        // Constructor.
        writer.println(" ".repeat(spaceCount) + className + "(" + fieldList + ") {");

        spaceCount += TAB;
        // Store parameters in fields.
        String[] fields = fieldList.split(", ");
        for (String field: fields) {
            String name = field.split(" ")[1];
            writer.println(" ".repeat(spaceCount) + "this." + name + " = " + name + ";");
        }

        spaceCount -= TAB;
        writer.println(" ".repeat(spaceCount) + "}");

        // Visitor pattern
        writer.println();
        writer.println(" ".repeat(spaceCount) + "@Override");
        writer.println(" ".repeat(spaceCount) + "<R> R accept(Visitor<R> visitor) {");
        spaceCount += TAB;
        writer.println(" ".repeat(spaceCount) + "return visitor.visit" + className + baseName + "(this);");
        spaceCount -= TAB;
        writer.println(" ".repeat(spaceCount) + "}");

        // Fields.
        writer.println();
        for (String field : fields) {
            writer.println(" ".repeat(spaceCount) + "final " + field + ";");
        }

        spaceCount -= TAB;
        writer.println(" ".repeat(spaceCount) + "}");
    }
}
