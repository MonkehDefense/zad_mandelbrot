public class test {
    public static void main(String[] args) {
        int w = 20, h = 12;
        int[] chunkSize = {4, 8};
        int wynik = (w/chunkSize[0]) * (h/chunkSize[1]);
        System.out.println(wynik);
    }
}
