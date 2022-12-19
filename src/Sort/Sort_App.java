package Sort;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.opencsv.CSVWriter;

	//  Shell Sort:
	//      Podziel listę na n podlist składających się z co n-tego elementu
	//      Na każdej podliście przeprowadź insertion sort
	//          Zaczynając od lewej: porównaj l[0] i l[1] i jeśli 1 jest mniejsze, to zamień je miejscami.
	//          Następnie weź l[2] i przyrównaj do l[1], a jeśli jest potrzeba to do l[0]
	//          tak powtarzaj, aż podlista będzie posortowana
	//          podlista odpowiada co n-temu elementowi listy głównej, więc podlisty nie są osobnymi bytami, a pociętymi sekcjami
	//          listy właściwej
	//      Można powtarzać to ze zmniejszającym się n, najpierw sortując podlisty przykładowo co ósmego elementu, dalej co czwartego, co drugiego, itd.
	//      sekwencje 2**i - 1 podobno dobre?

	//      https://www.youtube.com/watch?v=g06hNBhoS1k

	
public class Sort_App {
	public static List<Integer> lista;

	public static void main(String[] args) throws InterruptedException {
		ThreadPoolExecutor exec = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		// {7,3,1}
		// int[] shell_steps = {
		// 	(int)Math.pow(2, 3) - 1,
		// 	(int)Math.pow(2, 2) - 1,
		// 	(int)Math.pow(2, 1) - 1};
			
		//int[] shell_steps = {7, 3, 1};
		int[] shell_steps = {9, 6, 1};


		int[] sizes = {
			(int)Math.pow(2, 10),
			(int)Math.pow(2, 12),
			(int)Math.pow(2, 14),
			(int)Math.pow(2, 16),
			(int)Math.pow(2, 18),
			(int)Math.pow(2, 20)
		};

		for(int i = 0; i < sizes.length; i++){
			System.out.println(sizes[i]);
		}


		int[] repeats = {320,160,80,40,20,10};
		// int[] repeats = {1,1,1,1,1};

		long[] times = time_it(shell_steps, sizes, repeats);

		exec.shutdown();
		exec.awaitTermination(1, TimeUnit.DAYS);

		// save_it(times, sizes, "Shell_log " + shell_steps[0] + '-' + shell_steps[1] + '-'  + shell_steps[2]);
		save_it(times, sizes, "Insert_log");
	}

	




	public static void insert_sort(int step, int shift){
		// Przejdź przez całą listę co step wartości zaczynając od indeksów 0:step

		for(int i = shift + step; i < lista.size(); i+=step){
		// przesuwaj i-tą wartość w lewo, dopóki nie znajdzie się na miejscu
			int j = i;
			while(j  >= step){
				if(lista.get(j) < lista.get(j - step)){
					Collections.swap(lista, j - step, j);
					j -= step;
				}else break;
			}
		}
	}



	public static void shell_sort(int[] shell_steps){
		for(int i = 0; i < shell_steps.length; i++){
			for(int j = 0; j < shell_steps[i]; j++){
				insert_sort(shell_steps[i], j);
			}
		}
	}

	public static void shell_sort(int[] shell_steps, ThreadPoolExecutor exec) throws InterruptedException{
		for(int i = 0; i < shell_steps.length; i++){
			int step = shell_steps[i];
			CountDownLatch cl = new CountDownLatch(step);
			for(int j = 0; j < step; j++){
				final int shift = j;
				exec.execute(() -> {
					insert_sort(step, shift);
					cl.countDown();
				});
			}
			cl.await();
		}
	}

	
	public static long[] time_it(int[] shell_steps, int[] sizes, int[] repeats, ThreadPoolExecutor exec) throws InterruptedException{

		long[] times = new long[sizes.length];
		for(int i = 0; i < sizes.length; i++){
			System.out.println(""+i);
			long stop;
			times[i] = System.nanoTime();
			for(int j = 0; j < repeats[i]; j++){
				lista = new Random().ints(sizes[i],1,sizes[i]*2).boxed().collect(Collectors.toList());
				shell_sort(shell_steps, exec);
			}
			stop = System.nanoTime();
			times[i] = (stop - times[i])/repeats[i];
		}
		return times;
	}



	public static long[] time_it(int[] shell_steps, int[] sizes, int[] repeats){
		long[] times = new long[sizes.length];
		for(int i = 0; i < sizes.length; i++){
			System.out.println(""+i);
			long stop;
			times[i] = System.nanoTime();
			for(int j = 0; j < repeats[i]; j++){
				lista = new Random().ints(sizes[i],1,sizes[i]*2).boxed().collect(Collectors.toList());
				// shell_sort(shell_steps);
				insert_sort(1, 0);
			}
			stop = System.nanoTime();
			times[i] = (stop - times[i])/repeats[i];
		}
		return times;
	}









	public static void save_it(long[] times, int[] sizes, String log_name){
		if(times.length == sizes.length){

			if(!log_name.endsWith(".csv")){
				log_name = log_name + ".csv";
			}

			File logs = new File(log_name);
			try {
				FileWriter outputfile = new FileWriter(logs);
				CSVWriter writer = new CSVWriter(outputfile);
	
				String[] header = {"list size", "time"};
				writer.writeNext(header);
	
				for(int i = 0; i < times.length; i++){
					String[] line = {"" + sizes[i], "" + times[i]};
					writer.writeNext(line);
				}
				writer.close();
	
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
