public class Test {
    {
        int[] a = {};

        for (int i = 0; i < a.length; i++) {
            System.out.println(a[i]);
        } // can simplify

        int j;

        for (j = 0; j < a.length; j++) {
            System.out.println(a[j]);
        } // can't simplify
        for (int i = 0; i < a.length - 1; i++) {
            System.out.println(a[i]);
        } // can't simplify
        for (int i = 0; i < a.length; ++i) {
            a[i] = i;
        } // can't simplify

        int x = 0;

        x = x + 5; // can simplify
        x = 5 + x; // can simplify
        x = x / 5; // can simplify
        x = 5 / x; // can't simplify
    }
}
