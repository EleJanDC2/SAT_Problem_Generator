package pl.janczaja;

import java.util.ArrayList;
import java.util.Iterator;

public class Clause implements Iterable<LogicToken>{

    public ArrayList<LogicToken> var = new ArrayList<>();

    Clause(ArrayList<LogicToken> varIn){
        this.var.addAll(varIn);
    }

//    public void addAt(int index, LogicToken token){
//        var.add(index,token);
//    }

    //


    //

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        for(LogicToken token : var){
            builder.append(token.toString());
        }
        return String.valueOf(builder);
    }

    @Override
    public Iterator<LogicToken> iterator() {
        return new Iterator<LogicToken>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < var.size();
            }

            @Override
            public LogicToken next() {
                return var.get(index++);
            }
        };
    }

    //OTHER


}
