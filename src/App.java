import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.Math;

public class App {
//    private int h, w;

    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");
        //Czas ten należy wyznaczyć dla obrazków o bokach 32, 64, 128, 256, 512, 1024, 2048, 4096 i 8192 piksele.
        //Należy też narysować wykres
        //domyślnie max_iter = 200
        //domyślnie zakres
        //ci: [-1.2, 1.2]
        //cr: [-2.1, 0.6]
    }

    public static BufferedImage gen_pic(int w, int h, double cr_left, double cr_right, double ci_top, double ci_bottom, int iter) {
        double zr, zi, z_abs, cr, ci, cr_span, ci_span;
        cr_span = cr_right - cr_left;
        ci_span = ci_top - ci_bottom;

        //int[] palette = new int[iter];
        //for(int i = 0; i < iter; i++){
        //    palette[i] = color.getColor();
        //}

        BufferedImage img = new BufferedImage(w,h,1);

        for(int y = 0; y < h; y++){
            for(int x = 0; x < w; x++){
                zi = 0;
                zr = 0;
                z_abs = 0;

                // konwersja pikseli na ci i cr
                ci = (y * ci_span) / h + ci_top;
                cr = (x * cr_span) / w + cr_left;

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
//                   img.setRGB(x, y, color[end]);
                } else {
//                    img.setRGB(x, y, color[itr]);
                }


            }
        }
        return img;
        }

    public static double time_it(int w, int h, int repeat) {
        return 1.0;
    }
}
