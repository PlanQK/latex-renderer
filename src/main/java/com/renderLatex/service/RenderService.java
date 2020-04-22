package com.renderLatex.service;

import com.renderLatex.entities.LatexContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Paths;

@Service
public class RenderService {
    private String latexDocClass = "\\documentclass[border=0.50001bp,convert={convertexe={imgconvert},outext=.png}]{standalone} \n";
    private final String docStart = "\\begin{document} \n";
    private final String docEnd = " \\end{document} \n";
    private String packages = "";
    private String content = "";

    @Autowired
    public RenderService(){

    }

    public void renderAsPdf(LatexContent latexContent){
        this.content = latexContent.getContent();
        this.packages = "";
        latexContent.getLatexPackages().forEach(item -> this.packages += (item + " \n "));
        createTexDoc();
        renderTex();
    }

    public void renderAsSvg(LatexContent latexContent, boolean zipped){
        this.content = latexContent.getContent();
        this.packages = "";
        latexContent.getLatexPackages().forEach(item -> this.packages += (item + " \n "));
        createTexDoc();
        renderTex();
        convertToSvg(zipped);
    }

    public void renderAsPng(LatexContent latexContent){
        this.content = latexContent.getContent();
        this.packages = "";
        latexContent.getLatexPackages().forEach(item -> this.packages += (item + " \n "));
        createTexDoc();
        renderTex();
    }

    public void createTexDoc(){
        try ( FileWriter fileWriter = new FileWriter("renderFile.tex")) {
            fileWriter.write(this.latexDocClass + this.packages + this.docStart + this.content + this.docEnd);
            fileWriter.close();
        } catch (IOException e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }

    public void renderAsFullPdf(LatexContent latexContent){
        this.latexDocClass = "\\documentclass{article} \n";
        this.packages = "";
        this.content = latexContent.getContent();
        latexContent.getLatexPackages().forEach(item -> this.packages += (item + " \n "));
        createTexDoc();
        renderTex();
    }

    public void renderTex(){
        try {
            ProcessBuilder processBuilderRenderTex = new ProcessBuilder();
            processBuilderRenderTex.command("cmd.exe", "/c", "pdflatex -shell-escape renderFile.tex");
            Process processTex = processBuilderRenderTex.start();

            StringBuilder output = new StringBuilder();

            BufferedReader readerTex = new BufferedReader(
                    new InputStreamReader(processTex.getInputStream()));

            String line;
            while ((line = readerTex.readLine()) != null) {
                output.append(line + "\n");
            }

            int exitValTex = processTex.waitFor();
            if (exitValTex == 0) {
                System.out.println("Successfully rendered Tex");
                System.out.println(output);
            } else {
                System.out.println("Tex could not be rendered");
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }

    public void convertToSvg(boolean zipped){
        try {
            String userDirectory = Paths.get("").toAbsolutePath().toString();

            ProcessBuilder processBuilderRenderSvg = new ProcessBuilder();
//            processBuilderRenderSvg.command("cmd.exe", "/c", "inkscape --without-gui --file=renderFile.pdf --export-plain-svg=renderFile.svg"); works but svg not pretty
            if(zipped){
                processBuilderRenderSvg.command("cmd.exe", "/c", "pdf2svg2.bat \"" + userDirectory + "\\renderFile.pdf\" \"" + userDirectory +"\" -z");
            }else{
                processBuilderRenderSvg.command("cmd.exe", "/c", "pdf2svg2.bat \"" + userDirectory + "\\renderFile.pdf\" \"" + userDirectory +"\"");
            }
            Process processSvg = processBuilderRenderSvg.start();

            BufferedReader readerSvg = new BufferedReader(
                    new InputStreamReader(processSvg.getInputStream()));

            StringBuilder outputSvg = new StringBuilder();
            String lineSvg;
            while ((lineSvg = readerSvg.readLine()) != null) {
                outputSvg.append(lineSvg + "\n");
            }
            int exitValSvg = processSvg.waitFor();
            if (exitValSvg == 0) {
                System.out.println("Successfully rendered Svg");
                System.out.println(outputSvg);
            } else {
                System.out.println("Svg could not be rendered");
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }
}
