public class App {

    public static void main(String[] args) {

        String exchangeName = "EX4";

        Supplier supp1 = new Supplier(new String[]{"tlen", "buty"}, exchangeName, "TlenoBut");
        Supplier supp2 = new Supplier(new String[]{"tlen", "plecak"}, exchangeName, "PlecTlen");
        supp1.start();
        supp2.start();
    }
}
