package com.kitcode;

import antlr.Java8Lexer;
import antlr.Java8Parser;

import java.io.File;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.Writer;

import java.util.ArrayList;

import java.nio.charset.Charset;
import java.nio.file.Files;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

public class ASTGenerator {

    static ArrayList<String> LineNum = new ArrayList<String>();
    static ArrayList<String> Type = new ArrayList<String>();
    static ArrayList<String> Content = new ArrayList<String>();
    static int apiCount;
    
    private static String readFile(String fileName) throws IOException {
        File file = new File(fileName);
        byte[] encoded = Files.readAllBytes(file.toPath());
        return new String(encoded, Charset.forName("UTF-8"));
    }

    public static void main(String args[]) throws IOException{
        File root = new File("resource/example");
        //File root = new File("resource/java");
        File[] fs = root.listFiles();
        for (int i = 0; i < fs.length; i++) {
            System.out.println("Parsing " + fs[i]);
            parseFile(fs[i].toString());
        }
    }

    private static void parseFile(String fileName) throws IOException {
        String inputString = readFile(fileName);
        ANTLRInputStream input = new ANTLRInputStream(inputString);
        Java8Lexer lexer = new Java8Lexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Java8Parser parser = new Java8Parser(tokens);
        ParserRuleContext ctx = parser.compilationUnit();

        generateAST(ctx, false, 0);

        File outputFile = new File(fileName + ".dot");
        File invocationFile = new File(fileName + ".invoc");
        Writer out = new FileWriter(outputFile);
        Writer invocation = new FileWriter(invocationFile);

        apiCount = 0;
       
        out.write("digraph G {\n");
        printDOT(out, invocation);
        out.write("}\n");
        out.close();
        invocation.close();
        // System.out.println("digraph G {");
        // printDOT();
        // System.out.println("}");
    }

    private static void generateAST(RuleContext ctx, boolean verbose, int indentation) {
        boolean toBeIgnored = !verbose && ctx.getChildCount() == 1 && ctx.getChild(0) instanceof ParserRuleContext;

        if (!toBeIgnored) {
            String ruleName = Java8Parser.ruleNames[ctx.getRuleIndex()];
	    LineNum.add(Integer.toString(indentation));
            Type.add(ruleName);
            Content.add(ctx.getText());
	}
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree element = ctx.getChild(i);
            if (element instanceof RuleContext) {
                generateAST((RuleContext) element, verbose, indentation + (toBeIgnored ? 0 : 1));
            }
        }
    }
    
    private static void printDOT(Writer writer, Writer invocationWriter) throws IOException {
        printLabel(writer, invocationWriter);
        int pos = 0;
        for(int i=1; i<LineNum.size();i++){
            pos=getPos(Integer.parseInt(LineNum.get(i))-1, i);
            writer.write((Integer.parseInt(LineNum.get(i))-1)+Integer.toString(pos)+"->"+LineNum.get(i)+i+"\n");
        }
    }
    
    private static void printLabel(Writer writer, Writer invocationWriter) throws IOException {
        TreeMap treeMap = new TreeMap();　
        for(int i =0; i<LineNum.size(); i++){
            String label = Type.get(i);
            String content = Content.get(i).replace("\"","\\\"");
            writer.write(LineNum.get(i)+i+"[label=\""+label+"\\n "+content+" \"]\n");
            

            if (label == "methodDeclarator")
                invocationWriter.write("\n>" + content + "\n");
            if (label.indexOf("methodInvocation") != -1 && content.indexOf("System") == -1)
                //System.out.println(label);
                String apiName = content.split("\\(")[0];
                if(!treeMap.containsKey(apiName)) {
                    treeMap.put(apiName, apiCount)；
                }
                invocationWriter.write(apiName + "\n");
        }

        invocationFile.write("\n-----\n");
        Iterator tit = treeMap.entrySet().iterator();
        while (tit.hasNext()) {
            Map.Entry e = (Map.Entry) tit.next();
            invocationFile.write(e.getKey() + "--Value: "
                    + e.getValue());
        }
    }
    
    private static int getPos(int n, int limit){
        int pos = 0;
        for(int i=0; i<limit;i++){
            if(Integer.parseInt(LineNum.get(i))==n){
                pos = i;
            }
        }
        return pos;
    }
}
