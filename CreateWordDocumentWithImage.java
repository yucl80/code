package com.example;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xddf.usermodel.chart.AxisCrossBetween;
import org.apache.poi.xddf.usermodel.chart.AxisCrosses;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.AxisTickMark;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHeight;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTShd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTString;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTrPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTVerticalJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STShd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STVerticalJc;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Base64;
import java.util.List;

import javax.imageio.ImageIO;

public class CreateWordDocumentWithImage {
    public static void main(String[] args) throws InvalidFormatException, IOException {

        // 创建一个新的空文档
        XWPFDocument document = new XWPFDocument();

        XWPFHeader head = document.createHeader(HeaderFooterType.DEFAULT);
        head.createParagraph()
                .createRun()
                .setText("This is document header");

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

        document.createParagraph();

        createStyledTable(document);
        document.createParagraph();

        int numRows = 3;
        int numCols = 3;
        XWPFTable table3 = document.createTable(numRows, numCols);
        table3.setWidth("100%");

        // 合并单元格示例
        mergeCellsVertically(table3, 0, 0, 2);
        mergeCellsHorizontally(table3, 1, 1, 2);

        // 设置表格内容
        setCellText(table3.getRow(0).getCell(0), "合并单元格 0,0 - 2,0");
        setCellText(table3.getRow(1).getCell(1), "合并单元格 1,1 - 1,2");

        document.createParagraph();

        createChartBar(document);

        XWPFFooter foot = document.createFooter(HeaderFooterType.DEFAULT);
        foot.createParagraph()
                .createRun()
                .setText("This is document footer");

        // 将文档写入文件
        try (FileOutputStream out = new FileOutputStream("example_with_image.docx")) {
            document.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createChartBar(XWPFDocument document) {
        try {
            XWPFParagraph chartParagraph = document.createParagraph();

            // 创建图表
            XWPFChart chart = document.createChart(chartParagraph.createRun(), Units.toEMU(500), Units.toEMU(300));

            // 创建图表数据
            String[] categories = new String[] { "Category 1", "Category 2", "Category 3" };
            Double[] values = new Double[] { 10.0, 20.0, 30.0 };

            // 创建数据轴
            XDDFValueAxis bottomAxis = chart.createValueAxis(AxisPosition.BOTTOM);
            bottomAxis.setTitle("Value");
            bottomAxis.setCrosses(AxisCrosses.AUTO_ZERO);

            XDDFCategoryAxis leftAxis = chart.createCategoryAxis(AxisPosition.LEFT);
            leftAxis.setTitle("Category");

            // 设置分类轴为主要刻度线，数值轴为次要刻度线
            bottomAxis.setMajorTickMark(AxisTickMark.OUT);
            bottomAxis.setMinorTickMark(AxisTickMark.NONE);
            bottomAxis.setVisible(true);

            leftAxis.setMajorTickMark(AxisTickMark.OUT);
            leftAxis.setMinorTickMark(AxisTickMark.NONE);
            leftAxis.setVisible(true);

            // 创建数据源
            XDDFDataSource<String> cat = XDDFDataSourcesFactory.fromArray(categories);
            XDDFNumericalDataSource<Double> val = XDDFDataSourcesFactory.fromArray(values);

            // 创建柱状图数据
            XDDFChartData data = chart.createData(ChartTypes.RADAR, leftAxis, bottomAxis);
            XDDFChartData.Series series = data.addSeries(cat, val);
            series.setTitle("Sample Data", null);

            // 绘制图表
            chart.plot(data);

            // 绘制图表
            chart.plot(data);
        } catch (Exception e) {
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

    private static void fromBase64(XWPFDocument document) throws IOException, InvalidFormatException {
        String base64String = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQE...";
        String[] parts = base64String.split(";base64,");
        String imageType = parts[0].split(":")[1];
        String imageDataString = parts[1];

        // 将base64字符串解码为字节数组
        byte[] imageData = Base64.getDecoder().decode(imageDataString);

        // 创建一个输入流从字节数组读取图像
        ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
        BufferedImage bImage = ImageIO.read(bis);
        bis.close();

        // 创建一个段落插入图片
        XWPFParagraph imagePara = document.createParagraph();
        XWPFRun imageRun = imagePara.createRun();

        // 在文档中插入图片
        int format;
        if (imageType.contains("jpeg")) {
            format = XWPFDocument.PICTURE_TYPE_JPEG;
        } else if (imageType.contains("png")) {
            format = XWPFDocument.PICTURE_TYPE_PNG;
        } else if (imageType.contains("gif")) {
            format = XWPFDocument.PICTURE_TYPE_GIF;
        } else {
            throw new IllegalArgumentException("Unsupported image type");
        }
        int imageWidth = bImage.getWidth();
        int imageHeight = bImage.getHeight();
        imageRun.addPicture(new ByteArrayInputStream(imageData), format, "image.png", Units.toEMU(imageWidth),
                Units.toEMU(imageHeight));
    }

    public static void createStyledTable(XWPFDocument doc) {
        // Create a new document from scratch
        try {
            // -- OR --
            // open an existing empty document with styles already defined
            // XWPFDocument doc = new XWPFDocument(new
            // FileInputStream("base_document.docx"));

            // Create a new table with 6 rows and 3 columns
            int nRows = 6;
            int nCols = 3;
            XWPFTable table = doc.createTable(nRows, nCols);
            table.setWidth("100%");

            // Set the table style. If the style is not defined, the table style
            // will become "Normal".
            CTTblPr tblPr = table.getCTTbl().getTblPr();
            CTString styleStr = tblPr.addNewTblStyle();
            styleStr.setVal("StyledTable");

            // Get a list of the rows in the table
            List<XWPFTableRow> rows = table.getRows();
            int rowCt = 0;
            int colCt = 0;
            for (XWPFTableRow row : rows) {
                // get table row properties (trPr)
                CTTrPr trPr = row.getCtRow().addNewTrPr();
                // set row height; units = twentieth of a point, 360 = 0.25"
                CTHeight ht = trPr.addNewTrHeight();
                ht.setVal(BigInteger.valueOf(360));

                // get the cells in this row
                List<XWPFTableCell> cells = row.getTableCells();
                // add content to each cell
                for (XWPFTableCell cell : cells) {
                    // get a table cell properties element (tcPr)
                    CTTcPr tcpr = cell.getCTTc().addNewTcPr();
                    // set vertical alignment to "center"
                    CTVerticalJc va = tcpr.addNewVAlign();
                    va.setVal(STVerticalJc.CENTER);

                    // create cell color element
                    CTShd ctshd = tcpr.addNewShd();
                    ctshd.setColor("auto");
                    ctshd.setVal(STShd.CLEAR);
                    if (rowCt == 0) {
                        // header row
                        ctshd.setFill("A7BFDE");
                    } else if (rowCt % 2 == 0) {
                        // even row
                        ctshd.setFill("D3DFEE");
                    } else {
                        // odd row
                        ctshd.setFill("EDF2F8");
                    }

                    // get 1st paragraph in cell's paragraph list
                    XWPFParagraph para = cell.getParagraphs().get(0);
                    // create a run to contain the content
                    XWPFRun rh = para.createRun();
                    // style cell as desired
                    if (colCt == nCols - 1) {
                        // last column is 10pt Courier
                        rh.setFontSize(10);
                        rh.setFontFamily("Courier");
                    }
                    if (rowCt == 0) {
                        // header row
                        rh.setText("header row, col " + colCt);
                        rh.setBold(true);
                        para.setAlignment(ParagraphAlignment.CENTER);
                    } else {
                        // other rows
                        rh.setText("row " + rowCt + ", col " + colCt);
                        para.setAlignment(ParagraphAlignment.LEFT);
                    }
                    colCt++;
                } // for cell
                colCt = 0;
                rowCt++;
            } // for row
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // 合并指定列的单元格（垂直合并）
    private static void mergeCellsVertically(XWPFTable table, int col, int fromRow, int toRow) {
        for (int rowIndex = fromRow; rowIndex <= toRow; rowIndex++) {
            XWPFTableCell cell = table.getRow(rowIndex).getCell(col);
            if (rowIndex == fromRow) {
                // The first merged cell is set with RESTART merge value
                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.RESTART);
            } else {
                // Cells which join (merge) the first are set with CONTINUE
                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.CONTINUE);
            }
        }
    }

    // 合并指定行的单元格（水平合并）
    private static void mergeCellsHorizontally(XWPFTable table, int row, int fromCol, int toCol) {
        for (int colIndex = fromCol; colIndex <= toCol; colIndex++) {
            XWPFTableCell cell = table.getRow(row).getCell(colIndex);
            if (colIndex == fromCol) {
                // The first merged cell is set with RESTART merge value
                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.RESTART);
            } else {
                // Cells which join (merge) the first are set with CONTINUE
                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
            }
        }
    }

    // 设置单元格文本
    private static void setCellText(XWPFTableCell cell, String text) {
        cell.setText(text);
    }
}
