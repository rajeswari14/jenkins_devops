package com.app.calculator;

import com.calc.engine.Calc;
import com.calc.engine.CalcImpl;

public class MainCalculator {

    public static void main(String[] args) {
        Calc calc = new CalcImpl();
        int res = calc.add(1,2);
        System.out.println("a+b="+res);

        System.out.println("--------------------------");
        res = calc.multiply(1,2);
        System.out.println("a*b="+res);

        System.out.println("--------------------------");
        res = calc.devide(10,2);
        System.out.println("a/b="+res);

    }

}
