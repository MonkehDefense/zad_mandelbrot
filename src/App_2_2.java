import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.opencsv.CSVWriter;

import java.lang.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


// Drugi program ma wykorzystywać pulę wątków (ma w niej być tyle wątków, ile rdzeni jest w maszynie).
// Do puli należy wrzucać joby polegające na przeliczeniu części obrazka (szczegóły poniżej).

// funkcja generująca obrazek przy każdym wywołaniu tworzy wątki i jest to uwzględniane w uśrednianiu

public class App_2_2 {
    public static void main(String[] args) throws IOException, InterruptedException {
        
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        int[] widths = {32,64,128,256,512,1024,2048,4096,8192};
        int k = 20;
        int[] repeats = {k,k,k,k,k,k,k,k,k};

        time_it(widths, widths, repeats, 64, executor);

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.DAYS);
    }


    




    
    public static BufferedImage gen_pic(int w, int h, double cr_left, double cr_right, double ci_top, double ci_bottom, int chunkSize, ThreadPoolExecutor exec) throws InterruptedException{
        return gen_pic(w,h,cr_left,cr_right,ci_top,ci_bottom, 200, chunkSize, exec);
    }
    
    public static BufferedImage gen_pic(int w, int h, int iter, int chunkSize, ThreadPoolExecutor exec) throws InterruptedException{
        return gen_pic(w,h, -2.1,.6, 1.2, -1.2, iter, chunkSize, exec);
    }
    
    public static BufferedImage gen_pic(int w, int h, int chunkSize, ThreadPoolExecutor exec) throws InterruptedException{
        return gen_pic(w,h, -2.1,.6, 1.2, -1.2, 200, chunkSize, exec);
    }




    public static BufferedImage gen_pic(int w, int h, double cr_left, double cr_right, double ci_top, double ci_bottom, int iter, int chunkSize, ThreadPoolExecutor exec) throws InterruptedException {
        double cr_span, ci_span;
        cr_span = cr_right - cr_left;
        ci_span = ci_top - ci_bottom;
        BufferedImage img = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);

        // w razie, gdyby bok nie był podzielny przez liczbę pikseli w szerokości kolumny
        final int width = w - (w%chunkSize);

        CountDownLatch cl = new CountDownLatch((int)(w/chunkSize + 1));

        for(int i = 0; i < width; i+=chunkSize){

            final int start = i,
            stop = start + chunkSize;

            exec.execute(() -> {
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
                            z_abs = Math.sqrt(zizi + zrzr);
        
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
                cl.countDown();
            });


        }


        if(w != width){
            exec.execute(() -> {
                for(int y = 0; y < h; y++){
                    for(int x = width; x < w; x++){
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
                            z_abs = Math.sqrt(zizi + zrzr);
        
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

                cl.countDown();

            });
        }else{
            cl.countDown();
        }

        cl.await();

        return img;
    }

    

    public static void time_it(int[] w, int[] h, int[] repeat, int chunkSize, ThreadPoolExecutor exec) throws IOException, InterruptedException {

        if(w.length == h.length || repeat.length == w.length){
            long[] times = new long[repeat.length];
            long stop;
            BufferedImage[] images = new BufferedImage[repeat.length];

            for(int i = 0; i < repeat.length; i++){
                
                times[i] = System.nanoTime();
                for(int j = 0; j < repeat[i]; j++){
                    images[i] = gen_pic(w[i], h[i], chunkSize, exec);
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
        File logs = new File("logs_64px.csv");
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
