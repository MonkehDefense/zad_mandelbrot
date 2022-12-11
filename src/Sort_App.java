import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
		lista = new Random().ints((int)Math.pow(2, 18),1,10000)
		.boxed().collect(Collectors.toList());

		System.out.println(lista.size());
		
		
		int[] shell_steps = {
			(int)Math.pow(2, 3) - 1,
			(int)Math.pow(2, 2) - 1,
			(int)Math.pow(2, 1) - 1};
			
		//int[] shell_steps = {9, 6, 1};

		ThreadPoolExecutor exec = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

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

		exec.shutdown();
		exec.awaitTermination(1, TimeUnit.DAYS);
	}


	public static void insert_sort(int step, int shift){
		for(int i = shift + step; i < lista.size(); i+=step){
			// przesuwaj i-tą wartość w lewo, dopóki nie znajdzie się na miejscu
			int j = i;
			while(j - step >= 0 & lista.get(j) < lista.get(j - step)){
				Collections.swap(lista, j + step, j);
				j -= step;
			}
		}
	}

}
