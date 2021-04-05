package parser;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//import java.awt.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author mavialp
 */
public class Parser {
    static FileWriter csvWriter;
    static FileWriter output;
    
    static int Page_Height = 194 + 1;
    static int Page_Width = 31 + 2; // format => " " + " " + "31 days" = 33 length
    static int Row_combined = 48 + 1;
    static List<Integer> months = new ArrayList<Integer>(); 
    
    static String File_name = "";
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        getValues();    
    }
    
    public static void getValues() throws IOException {
        FileInputStream stream = null;
        
        control_file_syntax();
        try {
            stream = new FileInputStream("src/file_revize.txt");
        } catch (FileNotFoundException e) {
        }
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        Scanner file;
        PrintWriter writer;

        try {

            file = new Scanner(new File("src/file_revize.txt"));
            writer = new PrintWriter("src/output.txt");

            while (file.hasNext()) {
                String line = file.nextLine();

                String s1 = new String("P.01");
                if(!line.isEmpty()){
                    if(line.indexOf(s1) != -1){ // "parser P.01"
                        writer.write(parse_parenthesis_p01(line));
                    }
                    else{
                        writer.write(parser_parenthesis_regex_all_data(line));
                    }
                }
            }

            file.close();
            writer.close();
            create_filename_year_month("src/output.txt");
            find_months("src/output.txt");
            create_excel_file();
        }
        catch (IOException e) {
        }
        try {
            reader.close();
        } 
        catch (IOException e) {
        }

    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /*
    /
    /
    */    
 public static int create_excel_file() throws IOException{   
    
    int day = 0;
    int month = 0;
    int year = 0;
    int clock = 0;
    int minute = 0;
    int second = 0;
    
    int last_index = -1;

////////////////////////////////////////////////////////////////////////////    
    int previous_day = 0;
    int next_day = 0;
////////////////////////////////////////////////////////////////////////////


    int previous_month = months.get(0);
    int next_month;
    
    String [][] excel_file= new String[Page_Height][Page_Width];
    
    String [][] excel_file_combined= new String[Row_combined][Page_Width];
    String []clock_names_combined= clock_name_twenty_four_format();

    String []days= day_name();
    String []clock_names= clock_name();
    
////////////////////////////////////////////////////////////////////////////    
    for(int i=0; i< clock_names.length;i++){
        excel_file[i+1][0] = clock_names[i]; // set clock row by row
    }
    for(int i=1; i< Page_Height; i+=2){
      excel_file[i][1] = "Alis";            //  set state 
      excel_file[i+1][1] = "Veris";         //  set state

    }
////////////////////////////////////////////////////////////////////////////
    for(int i=0; i< clock_names_combined.length;i++){
        excel_file_combined[i+1][0] = clock_names_combined[i];  // set clock row-row
    }
    for(int i=1; i< Row_combined; i+=2){
      excel_file_combined[i][1] = "Alis";       //  set state
      excel_file_combined[i+1][1] = "Veris";    //  set state

    }
    
    for(int i=2; i< days.length + 2; i++)
    {   
        excel_file_combined[0][i] = days[i-2];  //  set day column by column 
    }    
////////////////////////////////////////////////////////////////////////////

    for(int i=2; i< days.length + 2; i++)
    {   
        excel_file[0][i] = days[i-2];   //  set day column by column
    }
////////////////////////////////////////////////////////////////////////////

    int count =0;
    Scanner file;
    file = new Scanner(new File("src/output.txt"));
    
    String first_data_block ="";
    String second_data_block ="";
    

            while (file.hasNext()) {
                String line = file.nextLine();
                
                count =0;
                previous_day = next_day;

                for(int i = 0; i < line.length(); i++)
                {
                    if(line.charAt(i) == ' ') {
                        count++;
                    }
                }
                if(count == 0){
                        year = Integer.parseInt(line.substring(1, 3));
                        month = Integer.parseInt(line.substring(3, 5));
                        day =  Integer.parseInt(line.substring(5, 7));
                        
                        clock =  Integer.parseInt(line.substring(7, 9));
                        minute =  Integer.parseInt(line.substring(9, 11));
                        second =  Integer.parseInt(line.substring(11, 13));
                        System.err.println("year: " + year +"mot: " + month + " day: " + day + "clock: " + clock + "minute: " + minute + "second " + second);
                        
                        if(previous_month == month){
                            if(previous_day != day){
                                last_index = 2*clock*4 + 2*(minute/15);
                            }
                            else {
                                Double test = Double.valueOf(minute);
                                test = Math.ceil(test/15);
                                last_index = (2*clock*4 + 2*test.intValue());
                            }
                        next_day = day;                            
                        }
                        else if(previous_month != month){
                            day = previous_day;
                            last_index = 192;
                        }
                }
                if(count == 2){
                    //subdata1 ="\"" + convert_to_comma(line.substring(0,8))+ "\"";
                    //subdata2 ="\"" + convert_to_comma(line.substring(9, 16)) + "\"";
                    first_data_block = "\"" + (line.substring(0,8)) +  "\"";
                    second_data_block = "\"" +(line.substring(9, 16)) +"\"";                                   
                    
                    excel_file[((last_index) +1)][day +1] = first_data_block;
                    excel_file[((last_index) +1 + 1)][day +1] = second_data_block;

                    last_index +=2;
                }
 
            }
            float toplam_alis = 0 ;
            float toplam_veris = 0;
            
            int index = 1;
            
            for(int i=2; i< Page_Width;i++){
                index = 1;

                for(int j=3; j<Page_Height; j +=8){
                    System.err.println("i: " +i + " j:" + j);
                    toplam_alis = 0;
                    toplam_veris =0;
                    DecimalFormat formatter2 = new DecimalFormat("###.###");
                          
                    toplam_alis += Float.parseFloat(convert_double_data(excel_file[j][i]));
                    toplam_alis += Float.parseFloat(convert_double_data(excel_file[j + 2][i]));
                    toplam_alis += Float.parseFloat(convert_double_data(excel_file[j + 4][i]));
                    toplam_alis += Float.parseFloat(convert_double_data(excel_file[j + 6][i]));

                    System.err.println("-------------------------");
                    toplam_veris += Float.parseFloat(convert_double_data(excel_file[j + 1][i]));
                    toplam_veris += Float.parseFloat(convert_double_data(excel_file[j + 3][i]));
                    toplam_veris += Float.parseFloat(convert_double_data(excel_file[j + 5][i]));
                    toplam_veris += Float.parseFloat(convert_double_data(excel_file[j + 7][i])); 

                    
                    toplam_alis = Float.parseFloat(formatter2.format(toplam_alis));
                    toplam_veris = Float.parseFloat(formatter2.format(toplam_veris));
                    System.err.println("toplam_alis: " + toplam_alis + "   toplam_veris :" + toplam_veris);

                    
                    excel_file_combined[index][i] = String.valueOf(toplam_alis);
                    excel_file_combined[index + 1][i] = String.valueOf(toplam_veris);
                    
                    index += 2;
                }
            }
            
            output = new FileWriter("src/"+ File_name +".csv");

            for(int i =0 ; i< Page_Height; i++){
                for(int j =0; j< Page_Width; j++){
                    if(excel_file[i][j] != null)
                        output.append(excel_file[i][j]);
                    output.append(",");
                }
                output.append("\n");
            
            }
            output.flush();
            output.close();

            FileWriter output_combined = new FileWriter("src/"+ File_name +"_combined.csv");

            for(int i =0 ; i< Row_combined; i++){
                for(int j =0; j< Page_Width; j++){
                    if(excel_file_combined[i][j] != null)
                        output_combined.append(excel_file_combined[i][j]);
                    output_combined.append(",");
                }
                output_combined.append("\n");
            
            }
            output_combined.flush();
            output_combined.close();            

    return 1;
 }
/////////////////////////////////////////////////////////////////////////// 
    public static String[] day_name(){
     String day_name[] = {"1","2", "3", "4", "5", "6","7","8","9","10",
                            "11","12", "13", "14", "15", "16","17","18","19","20",
                            "21","22", "23", "24", "25", "26","27","28","29","30","31"};
     return day_name;
 }
/////////////////////////////////////////////////////////////////////////// 
    public static String[] clock_name(){
     String clock_names[] = new String[4*24*2 +2];
     int count = 0;
     String clock = "";
     String minute = "";
     for(int i =0; i< clock_names.length; i+=2){
         
         clock = clock + (count/60);
         minute = minute + (count%60);
         
        if(clock.length() == 1)
            clock = "0" + clock;
        
        if(minute.length() == 1)
            minute = minute + "0";
        
        if(clock.equals("24"))
            minute = "15";
        
         clock_names[i] = clock + ":00" + " - " + "00:" + minute;
         clock_names[i+ 1] = clock + ":00" + " - " + "00:" + minute;
         
         count +=15;
         clock = "";
         minute = "";
    }
     return clock_names;
 }   
///////////////////////////////////////////////////////////////////////////
    public static String[] clock_name_twenty_four_format(){
        String clock_names[] = new String[24*2];
        String clocks[] = {"00:00-01:00","01:00-02:00", "02:00-03:00","03:00-04:00", "04:00-05:00","05:00-06:00",
                            "06:00-07:00","07:00-08:00", "08:00-09:00","09:00-10:00", "10:00-11:00","11:00-12:00",
                            "12:00-13:00","13:00-14:00", "14:00-15:00","15:00-16:00", "16:00-17:00","17:00-18:00",
                            "18:00-19:00","19:00-20:00", "20:00-21:00","21:00-22:00", "22:00-23:00","23:00-24:00",
        };
        int index =0;
     for(int i =0; i< clock_names.length; i+=2){
    
            
         clock_names[i] = clocks[index];
         clock_names[i+ 1] = clocks[index];
         index++;
    }
     return clock_names;        
    }
///////////////////////////////////////////////////////////////////////////    
    public static String convert_to_comma(String line){
     String data = "";
     char ch;
     for(int i=0; i< line.length();i ++){
         ch = line.charAt(i);
         if(ch == '.')
             ch =',';
         data = data + ch;
     }
     return data;
 }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/* P.01(0210201000000)(10)(15)(6)(1.9)(kWh)(2.9)(kWh)(5.9)(kvarh)(6.9)(kvarh)(7.9)(kvarh)(8.9)(kvarh)
 * result = > 0210201000000
 *    
 */    
    public static String parse_parenthesis_p01(String data){
        String parser_data = "";
        char []end_of = {'(',')'};

        for(int i=0; i< data.length(); i++){
            if(data.charAt(i) == end_of[0]){
                for(int j=i+1; j<data.length(); j++){
                    if(data.charAt(j) == end_of[1]){
                        i = data.length() + 1;
                        break;
                    }
                    parser_data = parser_data + data.charAt(j);
                }

            }
        }
        System.err.println("parser"+  parser_data );
    return parser_data + "\n";
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static String parser_parenthesis_regex_all_data(String data){
        String parser_data = "";

        int count = 0;

        Pattern p = Pattern.compile(""
            + "\\(" // opening brace
            + "([^\\)]*)" // non-brace text (group 1)
            );
        
        Matcher m = p.matcher(data);
        
        while (m.find()) {

            if(count == 2)
                break;

            parser_data = parser_data +" "+ m.group(1);
            count++;
        }
        
        return parser_data + "\n";
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////    
    public static String create_filename_year_month(String file) throws FileNotFoundException{

        Scanner file_name;
        file_name = new Scanner(new File(file));
        int month;

            while (file_name.hasNext()) {
                String line = file_name.nextLine();


                if(!line.contains(".")){
                    File_name =line.substring(1, 3) + "-" + line.substring(3, 5);
                    break;
                }

            }

        return File_name;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void control_file_syntax() throws IOException{
        
        FileWriter fw = new FileWriter("src/file_revize.txt");
        Reader fr = new FileReader("src/java_txt_file.txt");
        BufferedReader br = new BufferedReader(fr);
        
        while(br.ready()) {
            String metin = br.readLine();
            String txt = metin.replace("|", "(0");
            fw.write(txt + "\n");
        }

        fw.close();
        br.close();
        fr.close();    
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static String convert_double_data(String data){
        System.err.println(" data: " + data);
        if(data == null)
            data = "0";
        String metadata = data.replace("\"", "");
        
        return  metadata;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////    
    private static void find_months(String srcoutputtxt) throws FileNotFoundException {
    int count =0;
    int month = 0;
    Scanner file;
    file = new Scanner(new File(srcoutputtxt));
        

            while (file.hasNext()) {
                String line = file.nextLine();
                
                count =0;
                for(int i = 0; i < line.length(); i++)
                {
                    if(line.charAt(i) == ' ') {
                        count++;
                    }
                }
                if(count == 0){
                        month = Integer.parseInt(line.substring(3, 5));
                        
                        if(!months.contains(month))
                            months.add(month);
                }
            }
    }
}
