package pl.janczaja;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class AdditionalFeatures {

    public static Objects getRandomElement(ArrayList<Objects> array){
        Random random = new Random();
        return array.get(random.nextInt(array.size()));
    }
}
