package com.test;


import com.pdfcrowd.Pdfcrowd;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Main {
    private static  OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(10000, TimeUnit.MILLISECONDS)
            .readTimeout(10000, TimeUnit.MILLISECONDS)
            .writeTimeout(10000, TimeUnit.MILLISECONDS)
            .build();


    public static void main(String[] args) throws Exception {
        String fileName = "C:\\Users\\yunfeishen\\Downloads\\20230721pdf.txt";
        Random random = new Random();
        Stream<String> lines = Files.lines(Paths.get(fileName));

        lines.forEach(ele -> {
            if(ele!=null && ele.startsWith("[")){
                String[] strs = ele.split(": ");
                if(strs.length==2){
                    String title = strs[0].substring(1, strs[0].length()-1);
                    String url = strs[1].substring(0,strs[1].length()-1);
                    System.out.println(title);
                    System.out.println(url);
                    Main.convertToPdf(url,title);
                    try {
                        Thread.sleep(1500 + random.nextInt(800));
                    } catch (Exception ex){
                    }
                }
                else {
                    System.out.println("------------"+ele);
                }

            }
        });

//         Main.convertToPdf("","");
 //        Main.convertToPdf("http://mp.weixin.qq.com/s?__biz=MzAwMDU1MTE1OQ==&mid=2653561129&idx=1&sn=18f17ecfb8f05480dbf5874559c8b292&chksm=8139b0b1b64e39a7fba8b4aaaa0bafafc781f72bd6114babc4f674b10fe3b9ebda45445fb194&mpshare=1&scene=1&srcid=1206zzjEVoEwzqShvfBDtUFj&sharer_sharetime=1670313235501&sharer_shareid=fd35dbb7c5b50544e9272724c3f3009c#rd","newWechatOutput0330");
//        System.out.println("end");
//        Main.convertToPdf("https://mp.weixin.qq.com/s?__biz=MjM5NzMyMjAwMA==&mid=2651523268&idx=1&sn=1e9f5e5c8bada46e5e033e831539da9e&chksm=bd2464bb8a53edadce5d1abcfdebbd39e890ad6022ee13079d50a8230a78802a6b5bd0b16bbe&mpshare=1&scene=24&srcid=0224C29yw35OMcrxUpn9McGv&sharer_sharetime=1677243074102&sharer_shareid=e60cf5ee656e4dbc99e1688fe76adbf2#rd","aasa");



    }
    private static void convertToPdf2(String url, String title){
        try {
            // create the API client instance
            Pdfcrowd.HtmlToPdfClient client =
                    new Pdfcrowd.HtmlToPdfClient("demo", "ce544b6ea52a5621fb9d55f8b542d14d");

            // run the conversion and write the result to a file
            client.convertFileToFile(url, title);
        }
        catch(Pdfcrowd.Error why) {
            // report the error
            System.err.println("Pdfcrowd Error: " + why);

            // rethrow or handle the exception
            throw why;
        }
        catch(IOException why) {
            // report the error
            System.err.println("IO Error: " + why);

            // rethrow or handle the exception
        }
        }


    private static void convertToPdf(String url, String dirPath) {

        try {
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder().url(url).get().build();
            Response response = okHttpClient.newCall(request).execute();

            if (response.isSuccessful()) {
                String html = response.body().string();
                //                System.out.println(html);

                Document doc = Jsoup.parse(html);

                //找到图片标签
                Elements img = doc.select("img");
                for (int i = 0; i < img.size(); i++) {
                    // 图片地址
                    String imgUrl = img.get(i).attr("data-src");

                    if (imgUrl != null && !imgUrl.equals("")) {
                        Request request2 = new Request.Builder()
                                .url(imgUrl)
                                .get()
                                .build();

                        Response execute = okHttpClient.newCall(request2).execute();
                        if (execute.isSuccessful()) {

                            String imgPath = "D:\\temp\\" +  Main.md5(imgUrl) + ".png";
                            File imgFile = new File(imgPath);
                            if (!imgFile.exists()) {
                                // 下载图片
                                InputStream in = execute.body().byteStream();
                                FileOutputStream ot = new FileOutputStream(new File(imgPath));
                                BufferedOutputStream bos = new BufferedOutputStream(ot);
                                byte[] buf = new byte[8 * 1024];
                                int b;
                                while ((b = in.read(buf, 0, buf.length)) != -1) {
                                    bos.write(buf, 0, b);
                                    bos.flush();
                                }

                                bos.close();
                                ot.close();
                                in.close();
                            }

                            //重新赋值为本地路径
                            img.get(i).attr("data-src", imgPath);
                            img.get(i).attr("src", imgPath);

                            //导出 html
                            html = doc.outerHtml();
                        }

                        execute.close();
                    }
                }

//                Elements links = doc.select("link");
//                for (int i = 0; i < links.size(); i++) {
//                    // 图片地址
//                    String hrefUrl = links.get(i).attr("href");
//
//                    if (hrefUrl != null && hrefUrl.startsWith("//")) {
//
//                        hrefUrl = "http://"+hrefUrl;
//                        links.get(i).attr("href", hrefUrl);
//                        html = doc.outerHtml();
//                    }
//                }

                String htmlPath = "D:\\temp\\" + Main.md5(dirPath) + ".html";
                html = html.replaceAll("\"//","\"http://");
                html = html.replaceAll("D:\\\\temp\\\\","");
                final File f = new File(htmlPath);
                if (!f.exists()) {
                    Writer writer = new FileWriter(f);
                    BufferedWriter bw = new BufferedWriter(writer);
                    bw.write(html);

                    bw.close();
                    writer.close();
                }
                zip(dirPath);
                // 转换
                String fileName = "D:\\wechatOutput0721\\"+Main.md5(dirPath)+".pdf";
                Main.convertToPdf2("D:\\temp1\\"+dirPath+".zip", fileName);
                new File(fileName).renameTo(new File("D:\\wechatOutput0721\\"+dirPath+".pdf"));
                // 删除html文件
                if (f.exists()) {
                    f.delete();
                }
                response.close();
                clearTemp();
            }


        }catch (Exception ex){
            System.out.println(ex);
        }
    }

    private static void zip(String file) throws Exception{

        String sourceFolder = "D:\\temp"; // 源目录路径
        String zipFileName = "D:\\temp1\\"+file+".zip"; // 目标zip文件路径

        // 创建ZipOutputStream对象
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFileName));

        // 调用递归函数压缩文件夹中的所有文件
        zipDirectory(sourceFolder, "", zipOut);

        // 关闭ZipOutputStream
        zipOut.close();
        System.out.println("Zip file has been created!");
    }
    private static void zipDirectory(String sourceFolder, String parentFolder, ZipOutputStream zipOut) throws Exception {
        File folder = new File(sourceFolder);
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                // 如果是文件夹，递归调用
                zipDirectory(file.getAbsolutePath(), parentFolder + file.getName() + "/", zipOut);
            } else if(!file.getName().equals("temp.zip")){
                // 如果是文件，将其添加到zip文件中
                byte[] buffer = new byte[1024];
                FileInputStream fis = new FileInputStream(file);
                zipOut.putNextEntry(new ZipEntry(parentFolder + file.getName()));
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zipOut.write(buffer, 0, length);
                }
                zipOut.closeEntry();
                fis.close();
            }
        }
    }

    private static void clearTemp(){
        String directoryPath = "D:\\temp"; // 目录路径
        File directory = new File(directoryPath);
        deleteDirectory(directory);
    }
    private static void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            // 如果是文件夹，递归调用
            for (File file : directory.listFiles()) {
                file.delete();
            }
        }
    }

    public static boolean convert(String srcPath, String destPath) {

        StringBuilder cmd = new StringBuilder();
        cmd.append("wkhtmltopdf");
        cmd.append(" ");
        cmd.append("--enable-plugins");
        cmd.append(" ");
        cmd.append("--enable-forms");
        cmd.append(" ");
        cmd.append("--enable-local-file-access");
//        cmd.append(" ");
//        cmd.append("--page-size A5");
        cmd.append(" ");
        cmd.append(" \"");
        cmd.append(srcPath);
        cmd.append("\" ");
        cmd.append(" ");
        cmd.append("\""+ destPath + "\"");
        System.out.println(cmd.toString());
        boolean result = true;
        try {
            Process proc = Runtime.getRuntime().exec(cmd.toString());
            HtmlToPdfInterceptor error = new HtmlToPdfInterceptor(proc.getErrorStream());
            //HtmlToPdfInterceptor output = new HtmlToPdfInterceptor(proc.getInputStream());
            error.start();
           // output.start();
            proc.waitFor();
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    public static String md5(String text){
        MessageDigest digest = null;
        try {
            //创建加密对象
            digest = MessageDigest.getInstance("md5");
            //数组 byte[] result -> digest.digest( );  文本 text.getBytes();
            //调用加密对象，加密动作已完成
            byte[] result = digest.digest(text.getBytes());
            //创建StringBuilder对象 然后建议StringBuffer，安全性高，
            //StringBuilder 相较于 StringBuffer 有速度优势；StringBuffer 线程安全
            //接下来对加密后的结果进行优化
            StringBuffer sb = new StringBuffer();
            // result数组，digest.digest ( ); -> text.getBytes();
            // for 循环数组byte[] result;
            for (byte b:result){
                //将数据全部转换为正数
                int number = b & 0xff;//也就是255
                // 解释：为什么采用b&255
                /*
                 * b:它本来是一个byte类型的数据(1个字节) 255：是一个int类型的数据(4个字节)
                 * byte类型的数据与int类型的数据进行运算，会自动类型提升为int类型 eg: b: 1001 1100(原始数据)
                 * 运算时：
                 * b  : 0000 0000 0000 0000 0000 0000 1001 1100
                 * 255: 0000 0000 0000 0000 0000 0000 1111 1111
                 * 结果：0000 0000 0000 0000 0000 0000 1001 1100
                 * 此时的temp是一个int类型的整数
                 */
                //将所有的数据转换成16进制的形式
                String hex = Integer.toHexString(number);
                //当正数小于16时，使用Integer.toHexString(number)可能会造成缺少位数
                if(number<16&&number>=0){
                    //手动补上一个“0”
                    sb.append("0"+hex);
                }else {
                    sb.append(hex);
                }
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            //发送异常return空字符串
            return "";
        }

    }


}
