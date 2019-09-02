package com.wissen.services;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static com.wissen.domains.urls.ListOfUrls.urls;

@Service
public class DataServiceWithMultithreading {

	public String dataFor() {

		File file = new File(this.getClass().getResource("/profiles.xlsx").getFile());

		FileInputStream fIP = null;
		try {
			fIP = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		XSSFWorkbook workbook = null;
		try {
			workbook = new XSSFWorkbook(fIP);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Sheet sheet = workbook.getSheetAt(0);

		Workbook domainwise = new XSSFWorkbook();
		Sheet totalScoreavgRank = domainwise.createSheet();

		Row headerRow = totalScoreavgRank.createRow(0);
		Cell headerCells = headerRow.createCell(0);
		headerCells.setCellValue("Name");

		headerRow.createCell(1).setCellValue("Algorithms Score");
		headerRow.createCell(2).setCellValue("Algorithms Rank");

		headerRow.createCell(3).setCellValue("DataStructure Score");
		headerRow.createCell(4).setCellValue("DataStructure Rank");

		headerRow.createCell(5).setCellValue("Mathematics Score");
		headerRow.createCell(6).setCellValue("Mathematics Rank");

		headerRow.createCell(7).setCellValue("AI Score");
		headerRow.createCell(8).setCellValue("AI Rank");

		headerRow.createCell(9).setCellValue("C++ Score");
		headerRow.createCell(10).setCellValue("C++ Rank");

		headerRow.createCell(11).setCellValue("Java Score");
		headerRow.createCell(12).setCellValue("Java Rank");

		headerRow.createCell(13).setCellValue("Python Score");
		headerRow.createCell(14).setCellValue("Python Rank");

		headerRow.createCell(15).setCellValue("Ruby Score");
		headerRow.createCell(16).setCellValue("Ruby Rank");

		headerRow.createCell(17).setCellValue("SQL Score");
		headerRow.createCell(18).setCellValue("SQL Rank");

		headerRow.createCell(19).setCellValue("Databases Score");
		headerRow.createCell(20).setCellValue("Databases Rank");

		headerRow.createCell(21).setCellValue("DistributedSystem Score");
		headerRow.createCell(22).setCellValue("DistributedSystem Rank");

		headerRow.createCell(23).setCellValue("Linux Shell Score");
		headerRow.createCell(24).setCellValue("Linux Shell Rank");

		headerRow.createCell(25).setCellValue("FP Score");
		headerRow.createCell(26).setCellValue("FP Rank");

		headerRow.createCell(27).setCellValue("Security Score");
		headerRow.createCell(28).setCellValue("Security Rank");

		headerRow.createCell(29).setCellValue("Total Score");
		headerRow.createCell(30).setCellValue("Average Rank");

		int rowNum = 1;
		ExecutorService sevice = Executors.newFixedThreadPool(10);
		for (Row row : sheet) {
			if (rowNum > 67) {
				break;
			}
			String hacker = row.getCell(0).getStringCellValue();
			String profile = row.getCell(1).getStringCellValue();
			Row boardRow = totalScoreavgRank.createRow(rowNum++);
			
			sevice.execute(new FetchAndPutDataExcelSheet(hacker, profile, boardRow.getRowNum(), totalScoreavgRank));
		}

		while (FetchAndPutDataExcelSheet.numberOfPeopleFinished < 68) {
			
		}
		
		for (int i = 0; i <= 30; i++)
			totalScoreavgRank.autoSizeColumn(i);

		FileOutputStream opFile = null;
		try {
			opFile = new FileOutputStream("domainwise_TotalScore_AverageRank.xlsx");
			domainwise.write(opFile);
			opFile.close();
			domainwise.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "done";
	}
}

class FetchAndPutDataExcelSheet implements Runnable {

	public static int numberOfPeopleFinished = 1;
	String hacker;
	String profile;
	Sheet sheetname;
	int rowNum;

	public FetchAndPutDataExcelSheet(String hacker, String profile, int rowNum, Sheet sheetname) {
		// TODO Auto-generated constructor stub
		this.hacker = hacker;
		this.profile = profile;
		this.rowNum = rowNum;
		this.sheetname = sheetname;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		RestTemplate restTemplate = new RestTemplate();
		int column = 1;
		double totalScore = 0.0, averageRank = 0.0;
		Row boardRow = sheetname.getRow(rowNum);
		boardRow.createCell(0).setCellValue(hacker);
		for (String domain : urls) {
			String url = "https://www.hackerrank.com/rest/contests/master/tracks/" + domain
					+ "/leaderboard/find_hacker?type=practice&offset=0&" + "limit=20&level=1&elo_version=true&hacker="
					+ profile;
			System.out.println(url);
			ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
			double score = 0.0;
			int rank = 0;
			StringTokenizer multipleTokens = new StringTokenizer(response.toString(), ",:{}\"[]");
			while (multipleTokens.hasMoreTokens()) {
				String currentToken = multipleTokens.nextToken();
				if (currentToken.equals("score")) {
					score = Double.parseDouble(multipleTokens.nextToken());
					totalScore += score;
					multipleTokens.nextToken();
					rank = Integer.parseInt(multipleTokens.nextToken());
					averageRank += rank;
					boardRow.createCell(column++).setCellValue(score);
					boardRow.createCell(column++).setCellValue(rank);
					break;
				}
			}
		}
		boardRow.createCell(column++).setCellValue(totalScore);
		boardRow.createCell(column++).setCellValue(averageRank / urls.size());
		incrementPeople();
	}

	synchronized void incrementPeople() {
		numberOfPeopleFinished++;
	}

}