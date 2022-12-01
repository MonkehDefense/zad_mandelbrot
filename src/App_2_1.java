import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.opencsv.CSVWriter;

import java.lang.*;

// Pierwszy program ma tworzyć tyle wątków, ile jest rdzeni w maszynie.
// Obrazek do wygenerowania należy podzielić na tyle bloków,
// ile tworzymy wątków, a każdy wątek ma przeliczać jeden blok.

// funkcja generująca obrazek przy każdym wywołaniu tworzy wątki i jest to uwzględniane w uśrednianiu

public class App_2_1 {
    public static void main(String[] args) throws IOException, InterruptedException {
        int[] widths = {32,64,128,256,512,1024,2048,4096,8192};
        int k = 20;
        int[] repeats = {k,k,k,k,k,k,k,k,k};

        time_it(widths, widths, repeats);
    }


    




    
    public static BufferedImage gen_pic(int w, int h, double cr_left, double cr_right, double ci_top, double ci_bottom) throws InterruptedException{
        return gen_pic(w,h,cr_left,cr_right,ci_top,ci_bottom, 200);
    }
    
    public static BufferedImage gen_pic(int w, int h, int iter) throws InterruptedException{
        return gen_pic(w,h, -2.1,.6, 1.2, -1.2, iter);
    }
    
    public static BufferedImage gen_pic(int w, int h) throws InterruptedException{
        return gen_pic(w,h, -2.1,.6, 1.2, -1.2, 200);
    }




    public static BufferedImage gen_pic(int w, int h, double cr_left, double cr_right, double ci_top, double ci_bottom, int iter) throws InterruptedException {
        double cr_span, ci_span;
        cr_span = cr_right - cr_left;
        ci_span = ci_top - ci_bottom;
        BufferedImage img = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);


        // generowanie wątków
        int l = Runtime.getRuntime().availableProcessors();
        Thread[] threads = new Thread[l];

        for(int i = 0; i < l; i++){
            final int j = i;
            threads[i] = new Thread(() -> {
                int start = (int)(w * j / l);
                int stop = (int)(w * (j+1) / l);

                for(int y = 0; y < h; y++){
                    for(int x = start; x < stop; x++){
                        double zi = 0, zr = 0, z_abs = 0, cr, ci;
        
                        // konwersja pikseli na ci i cr
                        ci = ci_top - y * ci_span / h;
                        cr = x * cr_span / w + cr_left;
        
                        int itr = 0;
                        while(itr < iter && z_abs < 2){
                            double zrzr = zr*zr;
                            double zizi = zi*zi;
                            
                            zi = 2.0 * zr * zi + ci;
                            zr = zrzr - zizi + cr;
                            z_abs = Math.sqrt(zi * zi + zr * zr);
        
                            itr++;
                        }
        
                        if(itr == iter){
                           img.setRGB(x, y, new Color(100,0,0).getRGB());
                        } else {
                            int clr_aux = 255 - (int)Math.floor(255.0 * (double)itr/(double)iter);
                            img.setRGB(x, y, new Color(clr_aux,255,clr_aux).getRGB());
                        }
                    }
                }

            });
        }



        for(int i = 0; i < threads.length; i++){
            threads[i].start();
        }
        for(int i = 0; i < threads.length; i++){
            threads[i].join();
        }

        
        return img;
    }

    

    public static void time_it(int[] w, int[] h, int[] repeat) throws IOException, InterruptedException {

        if(w.length == h.length || repeat.length == w.length){
            long[] times = new long[repeat.length];
            long stop;
            BufferedImage[] images = new BufferedImage[repeat.length];

            for(int i = 0; i < repeat.length; i++){
                
                times[i] = System.nanoTime();
                for(int j = 0; j < repeat[i]; j++){
                    images[i] = gen_pic(w[i], h[i]);
                }
                stop = System.nanoTime();

                times[i] = (stop - times[i])/repeat[i];
            }

            //zapisać czasy i obrazki
            save_it(images, times);

        }
    }

    private static void save_it(BufferedImage img, String filename) throws IOException{
        File outputfile = new File(filename+".png");
        ImageIO.write(img, "png", outputfile);
    }

    private static void save_it(BufferedImage[] images, long[] times) throws IOException{
        int w, h;
        for(int i = 0; i < images.length; i++){
            w = images[i].getWidth();
            h = images[i].getHeight();
            //zapisać obraz w na h pikseli
            save_it(images[i], "MBrot"+w+'x'+h);
        }

        //zrobić logi czas-wymiar
        File logs = new File("logs.csv");
        try {
            FileWriter outputfile = new FileWriter(logs);
            CSVWriter writer = new CSVWriter(outputfile);

            String[] header = {"width", "height", "time"};
            writer.writeNext(header);

            for(int i = 0; i < times.length; i++){
                w = images[i].getWidth();
                h = images[i].getHeight();
                String[] line = {"" + w, "" + h, "" + times[i]};
                writer.writeNext(line);
            }
            writer.close();

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
