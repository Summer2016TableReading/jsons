package main;


import javax.json.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.canova.api.io.data.DoubleWritable;
import org.canova.api.io.data.IntWritable;
import org.canova.api.writable.Writable;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.apache.poi.sl.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFFactory;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
public class App {
	
	public static void main(String[] args){
	File directory = new File("papers");
	
	File[] papers = directory.listFiles();
	Pattern p = Pattern.compile("table\\d+");
	Pattern x = Pattern.compile("table(\\d{1,3})");
	HashMap<String,ArrayList<Integer>> sentencetable=new HashMap<String,ArrayList<Integer>>();
	
	ArrayList<String> tableSentences = new ArrayList<String>();
	ArrayList<Integer> labels = new ArrayList<Integer>();
	
	int num_papers = 0;
	FileWriter file;
	PrintWriter pw;
	try {
		file = new FileWriter("tablesentences.txt");
		pw=new PrintWriter(file);
		
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	
	for(File f: papers){
		if(f.getName().endsWith(".html") && num_papers<13718){
			try {
				
				Document d = Jsoup.parse(f, null);
				String[] sentences = d.text().split("\\. ");
				
				for(String s: sentences){
					
					
					Matcher m = p.matcher(s.replaceAll("\\W","").toLowerCase());
					if(m.find()){
						pw.println(s);
						tableSentences.add(s.toLowerCase().replaceAll("table\\W+\\d+",""));
						labels.add(1);
						ArrayList<Integer> stuff = new ArrayList<Integer>();
						stuff.add(Integer.parseInt(f.getName().replaceAll("PMC", "").replaceAll(".html", "")));
						Matcher t = x.matcher(s.replaceAll("\\W","").toLowerCase());
						if (t.find()) {
							
							  stuff.add(Integer.parseInt(t.group(1)));  // The matched substring  
						}
						pw.println(s);
						sentencetable.put(s.toLowerCase().replaceAll("table\\W+\\d+",""),stuff );
					}
					
				}
				pw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}	
			num_papers++;
			
			if(num_papers % 500 == 0){
				System.out.println(num_papers + " papers processed...");
			}
		}
	}
	
	File negatives = new File("unhighlightedsentences (1).txt");
	Scanner scan;
	ArrayList<String> neg = new ArrayList<String>();
	System.out.println("SIZE" + sentencetable.size());
	try {
		scan = new Scanner(negatives);
		int nSentences = 0;
		while(scan.hasNext()){
			if(nSentences < 5000){
				String wow = scan.nextLine().toLowerCase();
				tableSentences.add(wow);
				labels.add(0);
				neg.add(wow.substring(0, 7));
				neg.add(wow.substring(8));
				
				//System.out.println(wow.substring(0, 8));
			}
		}
		
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	}
	 HashMap<String,ArrayList<String>> data = new HashMap<String,ArrayList<String>>();
	try{
		ArrayList<String> yo = new ArrayList<String>();
		InputStream inp = new FileInputStream("output.xlsx");
		XSSFWorkbook wb = new XSSFWorkbook(inp);
	    XSSFSheet sheet = wb.getSheetAt(0);
	    Pattern iop = Pattern.compile("PMC\\d+");
	    //System.out.println("DATA FROM THE EXCEL FILE");
	    for(int i=0;i<sheet.getLastRowNum();i++){
	    	Matcher m = iop.matcher(sheet.getRow(i).getCell(0).getStringCellValue());
	    	String hello="";
	    	while(m.find()){
	    	 hello = m.group();
	    	}
	    	ArrayList<String> a = new ArrayList<String>();
	    	String hi =sheet.getRow(i).getCell(0).getStringCellValue();
	    	
	    	String b=hello.replace("PMC", "") +hi.substring(hi.length()-6,hi.length()-5);
	    	//String b =hi.substring(3,10) +hi.substring(hi.length()-6,hi.length()-5);
	    	//System.out.println(b);
	    	XSSFRow row =sheet.getRow(i);
	    	
	    	if(row.getCell(1)!=null){
	    	a.add(row.getCell(1).getStringCellValue());
	    	
	    	}
	    	else{
	    		a.add("");
	    	}
	    	if(row.getCell(2)!=null){
	    	a.add(row.getCell(2).getStringCellValue());
	    	//System.out.println(row.getCell(2));
	    	}
	    	else{
	    		a.add("");
	    	}
	    	
	    	//System.out.print("PMC INFO " + b + "DATA" + a.get(0));
        
	    	data.put(b,a);
	    	if(hello.replace("PMC", "").equals("1847718")) {
	    	yo.add(b);
	    	}
	    	
	    	
	    }
	    
	    System.out.println("Size:" + data.keySet().size());
	    for (String s : yo)
	    {
	    	System.out.println(s);
	    }
	    
	    wb.close();
	    
		
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
finally{
	
}
	
	final HashMap<String, Integer> ngrams = new HashMap<String, Integer>();
	HashMap<String, Integer> sentenceNGrams = new HashMap<String,Integer>();
	for(String s: tableSentences){
		HashSet<String> phrasesFound = new HashSet<String>();
		String[] words = s.replaceAll("\\W+"," ").split("\\W");
		ArrayList<String> corrected = new ArrayList<String>();
		for(String word: words){
			if(word.length() > 0){
				if(checkNumber(word)){
					corrected.add(word);
				} else {
					corrected.add("numval");
				}
			}
		}
		for(int l = 1; l < 3; l++){
			for(int i = 0; i < corrected.size() - l; i++){
				String phrase = getNGram(corrected, l, i);
				if(ngrams.containsKey(phrase)){
					ngrams.put(phrase, ngrams.get(phrase) + 1);
				} else {
					ngrams.put(phrase, 1);
				}
				phrasesFound.add(phrase);
			}
		}
		for(String phrase: phrasesFound){
			if(sentenceNGrams.containsKey(phrase)){
				sentenceNGrams.put(phrase, sentenceNGrams.get(phrase) + 1);
			} else {
				sentenceNGrams.put(phrase, 1);
			}
		}
	}
	
	ArrayList<String> commonNGrams = new ArrayList<String>();
	commonNGrams.addAll(ngrams.keySet());
	Collections.sort(commonNGrams, new Comparator<String>(){
		public int compare(String a, String b) {
			return ngrams.get(b) - ngrams.get(a);
		}
	});
	
	/*
	ArrayList<ArrayList<Writable>> TfIdfVectors = new ArrayList<ArrayList<Writable>>();
	boolean printOnce = true;
	for(int i = 0; i < tableSentences.size(); i++){
		String s = tableSentences.get(i);
		ArrayList<Writable> vec = new ArrayList<Writable>();
		for(String phrase: ngrams.keySet()){
			if(sentenceNGrams.get(phrase) < 500 && sentenceNGrams.get(phrase) > 10){
				double termFrequency = getTermFrequency(s, phrase);
				double inverseDocFrequency = Math.log((double)tableSentences.size()/(double)(sentenceNGrams.get(phrase)));
				vec.add(new DoubleWritable(termFrequency*inverseDocFrequency));
				if(printOnce){
					System.out.println(phrase);
				}
			}
		}
		printOnce = false;
		vec.add(new  IntWritable(labels.get(i)));
		TfIdfVectors.add(vec);
		
	}
	*/
	try{
		
	
	/*
	File tfidfoutput=new File("tfidfvectorsnew1.csv");
	File tfidfoutput2=new File("tfidfvectorsnew2.csv");
	File senoutput = new File("sentences1.csv");
	File senoutput2= new File("sentences2.csv");
	File senoutputxt1 = new File("sentences1.txt");
	File senoutputxt2 = new File("sentences2.txt");
	PrintWriter pw=new PrintWriter(tfidfoutput);
	PrintWriter pw2=new PrintWriter(tfidfoutput2);
	PrintWriter pw3 = new PrintWriter(senoutput);
	PrintWriter pw4 = new PrintWriter(senoutput2);
	//PrintWriter pw5 = new PrintWriter(senoutputxt1);
	//PrintWriter pw6 = new PrintWriter(senoutputxt2);
	 */
	 
	/*for(int i=0; i<TfIdfVectors.size()-1; i+=2){
		for(int j = 0; j < TfIdfVectors.get(i).size()-1; j++){
			pw.print(TfIdfVectors.get(i).get(j) + ",");
			pw2.print(TfIdfVectors.get(i+1).get(j) + ",");
			
		}
		pw.println(TfIdfVectors.get(i).get(TfIdfVectors.get(i).size()-1));
		pw2.println(TfIdfVectors.get(i+1).get(TfIdfVectors.get(i).size()-1));
	}
	
	pw.close();
	pw2.close();
	int count=0;
	for(Entry<String, ArrayList<Integer>> e : sentencetable.entrySet()){
		if(count%2==0){
			pw3.println("Sentence :" + "," +e.getKey() +","+ "PMC :" + ", " + e.getValue().get(0) + ","+ "Table :" + ", " +e.getValue().get(1));
		    pw5.println(e.getKey() + " " + e.getValue().get(0) + " " + e.getValue().get(1));
		}
		else{
			pw4.println("Sentence :" + "," +e.getKey() +","+ "PMC :" + ", " + e.getValue().get(0) + ","+ "Table :" + ", " +e.getValue().get(1));
		    pw6.println(e.getKey() + " " + e.getValue().get(0) + " " + e.getValue().get(1));
		}
		count++;
	}
	pw3.close();
	pw4.close();
	
	*/
///JSON STUFF
		//System.out.println("HELLO!!");
	HashMap<String,JsonObject> jsons = new HashMap<String,JsonObject>();
	int countt=0;
	int num=0;
	System.out.println("SIZE:"+sentencetable.size());
	
	for(File f : papers){
		jsons.put(f.getName().replaceAll("PMC", "").replaceAll(".html", ""),storeData(f.getName().replaceAll("PMC", "").replaceAll(".html", ""),sentencetable,neg,data));
		FileOutputStream fos = new FileOutputStream(new File("jsons/" + f.getName().replaceAll("PMC", "").replaceAll(".html", "")+".json"));
		JsonWriter jw = Json.createWriter(fos);
		jw.writeObject(jsons.get(f.getName().replaceAll("PMC", "").replaceAll(".html", "")));
	}
	/*
	for(Entry<String,ArrayList<Integer>> e : sentencetable.entrySet()){
		
			num++;
		if(jsons.containsKey(e.getValue().get(0))){
		//System.out.print("qoqoo");
			continue;
			
		}
		else{
		
		
		jsons.put(e.getValue().get(0),storeData(e.getValue().get(0),sentencetable,neg,data));
		countt++;
		System.out.println(countt + "Papers processed");
	}
		System.out.println("Total papers:" +num);
	}
	*/
	//System.out.println("Size:" +jsons.size());
	
	//for(Entry<String,JsonObject>  e: jsons.entrySet()){
		
		
	//}
	
	
	System.out.println(jsons.size() + "Successfully Copied JSON Object to File...");
	
	//System.out.println("Caption:" + data.get(18477181).get(0) + "Header:" + data.get(18477181).get(0) );
		
		
	//outputJSons(checkUnhighlighted(checkHighlighted(jsons,sentencetable)));
	
	
	
	
	}
	catch(IOException l){
		
	}
	
	finally{
		
	}
	
}

private static boolean checkNumber(String word) {
	for(int i = 0; i < word.length(); i++){
		if(!Character.isDigit(word.charAt(i))){
			return true;
		}
	}
	return false;
}
/*public static ArrayList<JsonObject> checkHighlighted(ArrayList<JsonObject> items,HashMap<String,ArrayList<Integer>> map ){
	for(Entry<String,ArrayList<Integer>> e : map.entrySet()){
		for(int i=0;i<items.size();i++){
		//check if array contains pmc and if does check for table
		if(items.get(i).containsValue(e.getValue().get(0))){
			if(items.get(i).containsValue(e.getValue().get(1))){
			//just addd sentences in	
			}
			else{
				//create table within given json
			}
		}
		//else if the table doesnt exist for a json in the thingy add the table to the json with the matching pmc
		
		//else only add the table sentence to th sentence place 
		else{
			JsonObject json = storeData(e.getValue().get(0));
			//YY??
			
			json.getJsonObject(e.getValue().get(0) + "").getJsonObject("tables").put("table " +  e.getValue().get(1),pop);
			json.add("table_sentences", value)
			items.add(json);
			
			//add json to arraylist
			//add sentence to json
			//add table
		}
		}
		
	}
	return items;
}
*/
public static ArrayList<JsonObject> checkUnhighlighted(ArrayList<JsonObject> items){
	//go through unhigighted sen add to jsons
	
	return items;
}
public static void outputJSons(ArrayList<JsonObject> input){
	//what do you think it does??
}
public static JsonObject storeData(String pmcnum, HashMap<String,ArrayList<Integer>> map, ArrayList<String> ng, HashMap<String,ArrayList<String>> d){
	ArrayList<Integer> tables = new ArrayList<Integer>();
	JsonObjectBuilder job = Json.createObjectBuilder();
	JsonArrayBuilder job3 = Json.createArrayBuilder();
	JsonArrayBuilder job2 = Json.createArrayBuilder();
	
	
	
	//ArrayList<JsonArray> wow = new ArrayList<JsonArray>();
	for(Entry<String,ArrayList<Integer>> e: map.entrySet()){
		if((e.getValue().get(0)+"").equals(pmcnum)){
			if(!tables.contains(e.getValue().get(1))){
				
				tables.add(e.getValue().get(1));
			}
			 JsonArray value = Json.createArrayBuilder()
				     .add(Json.createObjectBuilder()
				         .add("sentence:", e.getKey())
				         .add("table:", e.getValue().get(1).toString()))
				    
				     .build();
				 job2.add(value);
			
		}
	}
ArrayList<String> test = new ArrayList<String>();
	//System.out.println("Amount of tables in paper:"+ tables.size());
	//System.out.println("FINDING PLACE IN THE JSON:");
	for(int i=0;i<tables.size();i++){
		String caption="";
		String headers="";
		String ok =pmcnum+"";
		String wow=ok+tables.get(i) +"";
		
		//System.out.println(ohh);
		if(d.containsKey(wow)){
			
			caption=d.get(wow).get(0);
			headers=d.get(wow).get(1);
			
		}
		job.add("Table" + tables.get(i), Json.createObjectBuilder().add("caption", caption).add("headers", headers));
		
	
		
	}
	//System.out.println("PMC"+pmcnum);
	//System.out.print(ng.entrySet().size());
	for(int i=0;i<ng.size();i=i+2){
	//	System.out.println("Neg:"+ng.get(i));
		
		if(("" + pmcnum).equals(ng.get(i))){
			//System.out.println("WOW CONGRATS YOU GOT A NEGTIVE" +pmcnum);
			job3.add(ng.get(i+1));
		}
		
	}
	
	
	//this creates a new json thing with the given pmc and adds t to the json storage thing
	JsonObject value = Json.createObjectBuilder()
	     .add(pmcnum, Json.createObjectBuilder()
				.add("tables", job)
				.add("sentences", Json.createObjectBuilder()
					.add("table_sentences", job2)
					.add("nontable_sentences", job3)
					)
	    	)
			.build();
	if(value==null){
		//System.out.println("null");
	}
	return value;
}
private static double getTermFrequency(String s, String phrase) {
	int occurances = 0;
	int index = 0;
	while(s.indexOf(phrase, index) != -1){
		index += phrase.split("\\W").length;
		occurances++;
	}
	return occurances;
}

private static String getNGram(ArrayList<String> s, int length, int index){
	String phrase = "";
	while(length > 1){
		phrase += s.get(index) + " ";
		length--;
		index++;
	}
	phrase += s.get(index);
	return phrase;
}

}
