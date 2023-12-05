package pl.janczaja;

import java.util.ArrayList;

public class Utils {

    // CHYBA GOTOWE
    public static ArrayList<LogicToken> zipToList(ArrayList<LogicToken> list1, ArrayList<LogicToken> list2){
        ArrayList<LogicToken> result = new ArrayList<>();
        int mem = 0;

        for(int i = 0; i < list1.size(); i++){
            result.add(list1.get(i));

            if(i < list2.size()){
                result.add(list2.get(i));
                mem = i;
            }
        }

        if(list2.size() > list1.size()){
            for(int i = mem + 1; i < list2.size(); i++){
                result.add(list2.get(i));
            }
        }

        return result;
    }
}
