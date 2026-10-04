import socket.util.AuctionPrice;
public class AuctionPriceTest {
    private static void check(String text, long expected) {
        long actual = AuctionPrice.parse(text);
        if (actual != expected) throw new AssertionError(text + ": expected " + expected + ", got " + actual);
    }
    public static void main(String[] args) {
        check("§aЦена: §f$1 234 567",1234567);
        check("Цена: 1,234,567",1234567);
        check("Цена: 1.234.567",1234567);
        check("Price: $1.5k",1500);
        check("Стоимость: 2,5м",2500000);
        check("Цена: 1\u00a0234",1234);
        check("Цена: 1\u202f234",1234);
        check("Цена за 1 шт: $50",-1);
        check("Продавец: player123",-1);
        check("Цена: 12.34",-1);
        check("Цена: 9999999999999999999999999",-1);
        check("Цена: 0",-1);
        check("Цена: -15",-1);
        System.out.println("PASS 13 auction price cases");
    }
}
