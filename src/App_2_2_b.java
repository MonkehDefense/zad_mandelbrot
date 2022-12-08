import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.opencsv.CSVWriter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


// Drugi program ma wykorzystywać pulę wątków (ma w niej być tyle wątków, ile rdzeni jest w maszynie).
// Do puli należy wrzucać joby polegające na przeliczeniu części obrazka (szczegóły poniżej).

// funkcja generująca obrazek przy każdym wywołaniu tworzy wątki i jest to uwzględniane w uśrednianiu
// bloki o rozmiarach: 4, 8, 16, 32, 64, 128
// boki bloków: {2,2}, {2,4}, {4,4}, {4,8}, {8,8}, {8,16}

public class App_2_2_b {
    public static void main(String[] args) throws IOException, InterruptedException {
        
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        int[] widths = {32,64,128,256,512,1024,2048,4096,8192};
        int k = 20;
        int[] repeats = {k,k,k,k,k,k,k,k,k};
        // {szerokość bloku, wysokość bloku}
        int[] chunk = {8,16};

        time_it(widths, widths, repeats, chunk);

    }


    




    
    public static BufferedImage gen_pic(int w, int h, double cr_left, double cr_right, double ci_top, double ci_bottom, int[] chunkSize) throws InterruptedException{
        return gen_pic(w,h,cr_left,cr_right,ci_top,ci_bottom, 200, chunkSize);
    }
    
    public static BufferedImage gen_pic(int w, int h, int iter, int[] chunkSize) throws InterruptedException{
        return gen_pic(w,h, -2.1,.6, 1.2, -1.2, iter, chunkSize);
    }
    
    public static BufferedImage gen_pic(int w, int h, int[] chunkSize) throws InterruptedException{
        return gen_pic(w,h, -2.1,.6, 1.2, -1.2, 200, chunkSize);
    }




    public static BufferedImage gen_pic(int w, int h, double cr_left, double cr_right, double ci_top, double ci_bottom, int iter, int[] chunkSize) throws InterruptedException {
        double cr_span, ci_span;
        cr_span = cr_right - cr_left;
        ci_span = ci_top - ci_bottom;
        BufferedImage img = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);

        ThreadPoolExecutor exec = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


        CountDownLatch cl = new CountDownLatch((w/chunkSize[0]) * (h/chunkSize[1]));

        for(int i = 0; i < w; i+=chunkSize[0]){
            for(int j = 0; j < h; j+=chunkSize[1]){
                final int start_x = i, stop_x, start_y = j, stop_y;

                if(start_x + 2 * chunkSize[0] > w){
                    stop_x = w;
                } else{stop_x = start_x + chunkSize[0];}

                if(start_y + 2 * chunkSize[1] > h){
                    stop_y = h;
                } else{stop_y = start_y + chunkSize[1];}

                exec.execute(() -> {
                    for(int x = start_x; x < stop_x; x++){
                        for(int y = start_y; y < stop_y; y++){
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
        }
       

        cl.await();
        
        exec.shutdown();
        exec.awaitTermination(1, TimeUnit.DAYS);

        return img;
    }

    





    

    public static void time_it(int[] w, int[] h, int[] repeat, int[] chunkSize) throws IOException, InterruptedException {
        if(w.length == h.length || repeat.length == w.length){
            long[] times = new long[repeat.length];
            long stop;
            BufferedImage[] images = new BufferedImage[repeat.length];

            for(int i = 0; i < repeat.length; i++){
                
                times[i] = System.nanoTime();
                for(int j = 0; j < repeat[i]; j++){
                    images[i] = gen_pic(w[i], h[i], chunkSize);
                }
                stop = System.nanoTime();

                times[i] = (stop - times[i])/repeat[i];
            }

            //zapisać czasy i obrazki
            save_it(images, times, chunkSize);

        }
    }

    


    

    private static void save_it(BufferedImage[] images, long[] times, int[] chunkSize) throws IOException{
        int w, h;
        for(int i = 0; i < images.length; i++){
            w = images[i].getWidth();
            h = images[i].getHeight();
            //zapisać obraz w na h pikseli i stworzony w chunkach o odpowiedniej ilości pikseli
            File outputfile = new File("MBrot"+w+'x'+h+".png");
            ImageIO.write(images[i], "png", outputfile);

        }

        //zrobić logi czas-wymiar
        File logs = new File("logs_chunk_" + chunkSize[0] + 'x' + chunkSize[1] + "_b.csv");
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
