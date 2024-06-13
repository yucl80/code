package com.example;

import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class CreateWordDocumentWithImage {
    public static void main(String[] args) {
        // 创建一个新的空文档
        XWPFDocument document = new XWPFDocument();

        // 创建一个段落
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText("这是一个标题");
        run.setBold(true);
        run.setFontSize(20);

        // 添加一个新的段落
        XWPFParagraph paragraph2 = document.createParagraph();
        XWPFRun run2 = paragraph2.createRun();
        run2.setText("这是一个段落内容。");
        run2.setFontSize(12);

        // 插入图片
        try (FileInputStream imagFileInputStream = new FileInputStream(
                "D:\\yuchu\\Pictures\\屏幕截图 2023-08-30 104123.png")) {
            XWPFParagraph imagePara = document.createParagraph();
            imagePara.setBorderBottom(Borders.BASIC_BLACK_DASHES);
            imagePara.setBorderTop(Borders.BASIC_BLACK_DASHES);
            imagePara.setBorderRight(Borders.BASIC_BLACK_DASHES);
            imagePara.setBorderLeft(Borders.BASIC_BLACK_DASHES);
            XWPFHyperlinkRun hyperlinkRun = imagePara.createHyperlinkRun("http://www.google.com");
            hyperlinkRun.getCTHyperlink().setTooltip("此图片有链接 href://wwww.google.com, 按Ctrl+点击鼠标可以跳转到对应的网页");
            imagePara.setAlignment(ParagraphAlignment.CENTER);
            hyperlinkRun.addPicture(imagFileInputStream, Document.PICTURE_TYPE_JPEG, "image.jpg", Units.toEMU(400),
                    Units.toEMU(300));

        } catch (Exception e) {
            e.printStackTrace();
        }

        document.createParagraph();
        document.createParagraph();

        // 添加一个2行2列的表格
        XWPFTable table = document.createTable(2, 2);

        table.setWidth("100%");

        // 设置表格内容
        // table.getRow(0).getCell(0).setText("(1) 单元格 1,1asdfadfasfasdf \n\r (2)
        // adffasfasfasfasf");
        // table.getRow(0).getCell(1).setText("单元格 1,2");
        // table.getRow(1).getCell(0).setText("单元格 2,1");
        // table.getRow(1).getCell(1).setText("单元格 2,2");

        setCellTextWithLineBreaks(table.getRow(0).getCell(0), "单元格 1,1\n换行内容");
        setCellTextWithHyperlink(table.getRow(0).getCell(1), new String[] { "链路1", "链路图2" }, new String[] { "1", "2" });
        setCellTextWithLineBreaks(table.getRow(1).getCell(0), "单元格 2,1\n换行内容");
        setCellTextWithHyperlink(table.getRow(1).getCell(1), new String[] { "链路11" }, new String[] { "11" });

        for (int i = 0; i < 2; i++) {
            table.getRow(i).getCell(0).setWidth("40%");

        }
        for (int i = 0; i < 2; i++) {
            table.getRow(i).getCell(1).setWidth("60%");

        }

        // 将文档写入文件
        try (FileOutputStream out = new FileOutputStream("example_with_image.docx")) {
            document.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setCellTextWithHyperlink(XWPFTableCell cell, String[] titles, String[] ids) {
        cell.removeParagraph(0); // 移除默认的段落
        for (int i = 0; i < titles.length; i++) {
            XWPFParagraph para = cell.addParagraph();
            XWPFHyperlinkRun hyperlinkRun = para.createHyperlinkRun("http://www.google.com?id=" + ids[i]);
            hyperlinkRun.setText(i + ". " + titles[i]);
            hyperlinkRun.setColor("0000FF");
        }
    }

    private static void setCellTextWithLineBreaks(XWPFTableCell cell, String text) {
        cell.removeParagraph(0); // 移除默认的段落
        String[] lines = text.split("\n");
        for (String line : lines) {
            XWPFParagraph para = cell.addParagraph();
            XWPFRun run = para.createRun();
            run.setText(line);
            // run.addBreak(); // 添加换行
        }
    }
}
