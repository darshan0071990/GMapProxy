import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

@javax.servlet.annotation.WebServlet(name = "Proxy")
public class Proxy extends javax.servlet.http.HttpServlet {
    private static final int BUFFER_SIZE = 4096;
    Properties prop;

    protected void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) {

    }

    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws IOException {
        InputStream inputStream = getServletContext().getResourceAsStream("/WEB-INF/resources/proxy.properties");
        prop = new Properties();
        prop.load(inputStream);
        System.out.println("path: "+prop.getProperty("path"));

        String center = request.getParameter("center");
        String zoom = request.getParameter("zoom");

        String fileName = center+".png";
        String directoryName = prop.getProperty("path")+zoom+File.separator;

        File mapTile = fileCreation(directoryName, fileName);

        if(mapTile.exists()){
            System.out.println("File Exists");
        }else {
            System.out.println("File needs to Download");
            downloadTile(center, zoom, mapTile);
        }

        response.setContentType("image/png");
        ServletOutputStream out= response.getOutputStream();
        FileInputStream fin = new FileInputStream(mapTile);

        BufferedInputStream bin = new BufferedInputStream(fin);
        BufferedOutputStream bout = new BufferedOutputStream(out);
        int ch =0;
        while((ch=bin.read())!=-1)
        {
            bout.write(ch);
        }

        bin.close();
        fin.close();
        bout.close();
        out.close();
        inputStream.close();
    }

    private void downloadTile(String center, String zoom, File fileName) {
        try {
            URL url = new URL("https://maps.googleapis.com/maps/api/staticmap?maptype=satellite&center=" + center + "&zoom=" + zoom + "&size=512x512&key=AIzaSyBZu_poSir6dvhp4PxUPhOcZYMHrlYLldQ");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();

            // always check HTTP response code first
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // opens input stream from the HTTP connection
                InputStream inputStream = conn.getInputStream();
                writeFile(inputStream,fileName);

                System.out.println("File downloaded");
            } else {
                System.out.println("No file to download. Server replied HTTP code: " + responseCode);
            }
            conn.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File fileCreation(String directoryName, String fileName) {
        File directory = new File(directoryName);
        if (!directory.exists()){
            directory.mkdirs();
        }
        File imageDest = new File(directory.getAbsoluteFile() + "/" + fileName);
        System.out.println("Image Destination : "+imageDest.getAbsolutePath());
        return imageDest;
    }

    public void writeFile(InputStream inputStream, File fileName){

        try{
            FileOutputStream outputStream = new FileOutputStream(fileName);
            int bytesRead = -1;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}