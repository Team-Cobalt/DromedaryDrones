package testing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class DroneTest {
    public static void main(String args[]) {
        int timeOfOrder;
        int mealsPerHour;
        int index;
        final int MINUTES = 60;
        Random rand = new Random();
        ArrayList<Integer> orderTimes = new ArrayList<>();
        ArrayList<Integer> stocFlow = new ArrayList<>();
        stocFlow.add(15);
        stocFlow.add(17);
        stocFlow.add(22);
        stocFlow.add(15);

        //generates a list of random order times according to given stochastic flow
        for(index = 0; index < stocFlow.size(); index++) {
            //number of meals to be generated in specific hour
            mealsPerHour = stocFlow.get(index);

            //generates each order time for all orders in each hour slot
            for(int mealNum = 0; mealNum < mealsPerHour; mealNum++) {
                //calculates time of order using given hour (i.e. first hour, second hour, etc.)
                timeOfOrder = (rand.nextInt(MINUTES) + 1) + (MINUTES * index);
                orderTimes.add(timeOfOrder);
            }
        }

        //sorts list of order times in increasing order
        Collections.sort(orderTimes);

        for(index = 0; index < orderTimes.size(); index++) {
            System.out.print(orderTimes.get(index) + " ");

            if(index == 14 || index == 31 || index == 53) {
                System.out.println();
            }
        }
    }
}
